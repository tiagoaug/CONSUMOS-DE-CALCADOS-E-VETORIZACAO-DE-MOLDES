package com.musgo.vendaseproducao.moldevetor.ui.captura

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.musgo.vendaseproducao.moldevetor.camera.CameraXPreview
import com.musgo.vendaseproducao.moldevetor.camera.capturarComoBitmap
import com.musgo.vendaseproducao.moldevetor.domain.Contorno
import com.musgo.vendaseproducao.moldevetor.visao.ArucoMarkerDetector
import com.musgo.vendaseproducao.moldevetor.visao.ContornoExtractor
import com.musgo.vendaseproducao.moldevetor.visao.MoldeVetorIA
import com.musgo.vendaseproducao.moldevetor.visao.PerspectiveCorrector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

private sealed class EstadoCaptura {
    data object AguardandoPermissao : EstadoCaptura()
    data object PermissaoNegada : EstadoCaptura()
    data object Pronta : EstadoCaptura()
    data object Processando : EstadoCaptura()
    data class Erro(val mensagem: String) : EstadoCaptura()

    /** Marcador encontrado e perspectiva já corrigida — antes de procurar o contorno, deixa o
     * usuário marcar/arrastar sobre a área da peça (pedido do Tiago: "teria como marcar o objeto
     * ou arrastar sobre a área desejada"), guiando a detecção pra região certa em vez de
     * procurar na foto inteira (onde mesa/sombra/outros objetos podem confundir — ver
     * ContornoExtractor.refinarDentroDoGuia). Marcar é opcional: dá pra pular e detectar na
     * foto inteira, igual ao comportamento de antes. */
    data class MarcarArea(
        val imagemCorrigidaMat: Mat,
        val fotoCorrigidaBitmap: Bitmap,
        val pxPorMm: Double,
        val fotoBruta: Bitmap,
        val processando: Boolean = false,
    ) : EstadoCaptura()
}

/** Resultado de uma detecção bem-sucedida — [fotoCorrigida] é a imagem já retificada (vista de
 * cima, na escala [pxPorMm]), usada como fundo na tela de validação de vértices (Fase 4);
 * [fotoPath] é a foto BRUTA capturada, já persistida em armazenamento interno (não a corrigida —
 * essa fica só em memória, recomputável a qualquer momento a partir da bruta se precisar). */
private data class ResultadoDeteccao(
    val contorno: Contorno,
    val fotoCorrigida: Bitmap?,
    val pxPorMm: Double,
    val fotoPath: String?,
)

/** Captura guiada por CameraX + pipeline de detecção (ArUco → correção de perspectiva → marcação
 * opcional da área → extração de contorno) — espelha os passos 2-5 da especificação. Ao detectar
 * com sucesso, entrega o contorno (ainda não confirmado) pro chamador (MoldeVetorApp →
 * ValidacaoContornoScreen, Fase 4) em vez de salvar direto — a correção manual de vértices e a
 * validação por medida real acontecem DEPOIS desta tela. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturaScreen(
    facaId: Long,
    onDetectado: (contorno: Contorno, fotoCorrigida: Bitmap?, pxPorMm: Double, fotoPath: String?) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val executor = remember { Executors.newSingleThreadExecutor() }

    var estado by remember {
        mutableStateOf<EstadoCaptura>(
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                EstadoCaptura.Pronta
            } else {
                EstadoCaptura.AguardandoPermissao
            },
        )
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val launcherPermissao = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
        estado = if (concedida) EstadoCaptura.Pronta else EstadoCaptura.PermissaoNegada
    }

    fun finalizar(
        contorno: Contorno?,
        fotoCorrigidaBitmap: Bitmap,
        pxPorMm: Double,
        fotoBruta: Bitmap,
        mensagemFalha: String = "Não foi possível identificar o contorno da faca. Verifique o contraste entre a faca e a mesa e tente novamente.",
    ) {
        if (contorno == null) {
            estado = EstadoCaptura.Erro(mensagemFalha)
            return
        }
        scope.launch {
            val fotoPath = withContext(Dispatchers.IO) { salvarFotoInterna(context, facaId, fotoBruta) }
            onDetectado(contorno, fotoCorrigidaBitmap, pxPorMm, fotoPath)
        }
    }

    fun detectarNaFotoInteira(atual: EstadoCaptura.MarcarArea) {
        estado = atual.copy(processando = true)
        scope.launch {
            val contorno = withContext(Dispatchers.Default) {
                ContornoExtractor.extrair(atual.imagemCorrigidaMat, atual.pxPorMm)
            }
            finalizar(contorno, atual.fotoCorrigidaBitmap, atual.pxPorMm, atual.fotoBruta)
        }
    }

    fun detectarDentroDaArea(atual: EstadoCaptura.MarcarArea, cantosPx: List<org.opencv.core.Point>) {
        estado = atual.copy(processando = true)
        scope.launch {
            val contorno = withContext(Dispatchers.Default) {
                val contornoPx = ContornoExtractor.refinarDentroDoGuia(atual.imagemCorrigidaMat, cantosPx)
                contornoPx?.let { pontos ->
                    Contorno(externo = pontos.map { p -> com.musgo.vendaseproducao.moldevetor.domain.Ponto(xMm = p.x / atual.pxPorMm, yMm = p.y / atual.pxPorMm) })
                }
            }
            finalizar(contorno, atual.fotoCorrigidaBitmap, atual.pxPorMm, atual.fotoBruta)
        }
    }

    // Pedido do Tiago: "a opção de contorno por IA deve ficar disponível antes, deve ser feita
    // nesse momento em que eu capturar a área de interesse" — mesma área marcada acima com o
    // retângulo, só troca o motor (Claude em vez de OpenCV), igual ao toggle que já existe no
    // Recalcular da tela de validação.
    fun detectarDentroDaAreaComIA(atual: EstadoCaptura.MarcarArea, guiaPx: List<Offset>) {
        estado = atual.copy(processando = true)
        scope.launch {
            var mensagemErroIA = "A IA não conseguiu identificar um contorno nessa área. Ajuste o guia, tente com mais contraste/luz, ou desative o toggle de IA."
            val contorno = runCatching { MoldeVetorIA.extrairContorno(atual.fotoCorrigidaBitmap, guiaPx) }
                .onFailure { e -> mensagemErroIA = "Erro ao consultar a IA: ${e.message ?: "tente novamente"}." }
                .getOrNull()
                ?.let { pontos ->
                    Contorno(externo = pontos.map { p -> com.musgo.vendaseproducao.moldevetor.domain.Ponto(xMm = p.x / atual.pxPorMm, yMm = p.y / atual.pxPorMm) })
                }
            finalizar(contorno, atual.fotoCorrigidaBitmap, atual.pxPorMm, atual.fotoBruta, mensagemFalha = mensagemErroIA)
        }
    }

    fun processar(bitmap: Bitmap) {
        estado = EstadoCaptura.Processando
        scope.launch {
            val resultado = withContext(Dispatchers.Default) {
                try {
                    val imagem = Mat()
                    Utils.bitmapToMat(bitmap, imagem)

                    val marcador = ArucoMarkerDetector.detectar(imagem)
                        ?: return@withContext Result.failure<EstadoCaptura.MarcarArea>(
                            IllegalStateException("Marcador ArUco não encontrado. Aproxime a câmera e garanta que o marcador de 5 cm esteja visível e bem iluminado."),
                        )

                    val corrigida = PerspectiveCorrector.corrigir(imagem, marcador.cantosPx)
                    val fotoCorrigidaBitmap = Bitmap.createBitmap(
                        corrigida.imagemCorrigida.cols(),
                        corrigida.imagemCorrigida.rows(),
                        Bitmap.Config.ARGB_8888,
                    )
                    Utils.matToBitmap(corrigida.imagemCorrigida, fotoCorrigidaBitmap)

                    Result.success(
                        EstadoCaptura.MarcarArea(
                            imagemCorrigidaMat = corrigida.imagemCorrigida,
                            fotoCorrigidaBitmap = fotoCorrigidaBitmap,
                            pxPorMm = corrigida.pxPorMm,
                            fotoBruta = bitmap,
                        ),
                    )
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            estado = resultado.fold(
                onSuccess = { it },
                onFailure = { e -> EstadoCaptura.Erro(e.message ?: "Falha ao processar a foto.") },
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vetorizar faca") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val atual = estado) {
                is EstadoCaptura.AguardandoPermissao -> {
                    SolicitarPermissao(onSolicitar = { launcherPermissao.launch(Manifest.permission.CAMERA) })
                }

                is EstadoCaptura.PermissaoNegada -> {
                    MensagemCentral(
                        texto = "Sem acesso à câmera, não é possível vetorizar a faca. Conceda a permissão nas configurações do app.",
                        acaoTexto = "Tentar novamente",
                        onAcao = { launcherPermissao.launch(Manifest.permission.CAMERA) },
                    )
                }

                is EstadoCaptura.Pronta -> {
                    CameraXPreview(
                        onCapturaPronta = { imageCapture = it },
                        onErro = { msg -> estado = EstadoCaptura.Erro(msg) },
                    )
                    GuiaAlinhamento()
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(24.dp),
                    ) {
                        Text(
                            text = "Posicione o marcador ArUco (5 cm) e a faca dentro da guia, com boa iluminação, e toque em Capturar.",
                            color = Color.White,
                        )
                        Button(
                            onClick = {
                                imageCapture?.let { ic ->
                                    capturarComoBitmap(
                                        imageCapture = ic,
                                        executor = executor,
                                        onSucesso = { bitmap -> processar(bitmap) },
                                        onErro = { msg -> estado = EstadoCaptura.Erro(msg) },
                                    )
                                }
                            },
                            enabled = imageCapture != null,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        ) {
                            Text("Capturar")
                        }
                    }
                }

                is EstadoCaptura.Processando -> {
                    Column(modifier = Modifier.align(Alignment.Center)) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text(
                            text = "Processando imagem...",
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }

                is EstadoCaptura.Erro -> {
                    MensagemCentral(
                        texto = atual.mensagem,
                        acaoTexto = "Tentar novamente",
                        onAcao = { estado = EstadoCaptura.Pronta },
                    )
                }

                is EstadoCaptura.MarcarArea -> {
                    MarcarAreaConteudo(
                        estado = atual,
                        onConfirmarArea = { cantosPx, usarIA ->
                            if (usarIA) {
                                detectarDentroDaAreaComIA(atual, cantosPx.map { Offset(it.x.toFloat(), it.y.toFloat()) })
                            } else {
                                detectarDentroDaArea(atual, cantosPx)
                            }
                        },
                        onPular = { detectarNaFotoInteira(atual) },
                    )
                }
            }
        }
    }
}

/** Mostra a foto já corrigida e deixa o usuário arrastar um retângulo sobre a peça — esse
 * retângulo vira a "área de busca" de ContornoExtractor.refinarDentroDoGuia, em vez da detecção
 * procurar a borda na foto inteira (onde mesa, sombra ou outros objetos podem confundir). */
@Composable
private fun MarcarAreaConteudo(
    estado: EstadoCaptura.MarcarArea,
    onConfirmarArea: (List<org.opencv.core.Point>, usarIA: Boolean) -> Unit,
    onPular: () -> Unit,
) {
    var tamanhoCanvas by remember { mutableStateOf(IntSize.Zero) }
    var cantoInicial by remember { mutableStateOf<Offset?>(null) }
    var cantoAtual by remember { mutableStateOf<Offset?>(null) }
    // Pedido do Tiago: "a opção de contorno por IA deve ficar disponível antes, nesse momento em
    // que eu capturar a área de interesse" — toggle aqui, antes mesmo de chegar na validação.
    var usarIA by remember { mutableStateOf(false) }

    val ladoImagemPx = remember(estado.pxPorMm) { (PerspectiveCorrector.LADO_SAIDA_MM * estado.pxPorMm).toFloat() }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Marque a área da peça (opcional): arraste um retângulo em volta dela pra ajudar a detecção a achar a borda certa.",
            modifier = Modifier.padding(16.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .onSizeChanged { tamanhoCanvas = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { posicao ->
                            cantoInicial = posicao
                            cantoAtual = posicao
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            cantoAtual = change.position
                        },
                    )
                },
        ) {
            val escala = if (ladoImagemPx > 0f && tamanhoCanvas.width > 0) tamanhoCanvas.width / ladoImagemPx else 1f

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawImage(
                    image = estado.fotoCorrigidaBitmap.asImageBitmap(),
                    dstSize = IntSize(
                        (estado.fotoCorrigidaBitmap.width * escala).toInt(),
                        (estado.fotoCorrigidaBitmap.height * escala).toInt(),
                    ),
                )
                val inicio = cantoInicial
                val fim = cantoAtual
                if (inicio != null && fim != null) {
                    val topLeft = Offset(minOf(inicio.x, fim.x), minOf(inicio.y, fim.y))
                    val tamanho = androidx.compose.ui.geometry.Size(kotlin.math.abs(fim.x - inicio.x), kotlin.math.abs(fim.y - inicio.y))
                    drawRect(color = Color(0xFFEAB308), topLeft = topLeft, size = tamanho, style = Stroke(width = 5f))
                    drawRect(color = Color(0xFFEAB308).copy(alpha = 0.15f), topLeft = topLeft, size = tamanho)
                }
            }
        }
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            val inicio = cantoInicial
            val fim = cantoAtual
            val areaMarcadaValida = inicio != null && fim != null &&
                kotlin.math.abs(fim.x - inicio.x) > 20f && kotlin.math.abs(fim.y - inicio.y) > 20f
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Usar IA (Claude) pra reconhecer o contorno", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(
                        "Em vez do OpenCV, manda a área marcada pro Claude identificar só o contorno externo, ignorando textura/furos internos.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                    )
                }
                Switch(checked = usarIA, onCheckedChange = { usarIA = it }, enabled = !estado.processando)
            }
            Button(
                onClick = {
                    if (inicio == null || fim == null || !areaMarcadaValida) return@Button
                    val escala = if (ladoImagemPx > 0f && tamanhoCanvas.width > 0) tamanhoCanvas.width / ladoImagemPx else 1f
                    val minX = minOf(inicio.x, fim.x) / escala
                    val maxX = maxOf(inicio.x, fim.x) / escala
                    val minY = minOf(inicio.y, fim.y) / escala
                    val maxY = maxOf(inicio.y, fim.y) / escala
                    val cantos = listOf(
                        org.opencv.core.Point(minX.toDouble(), minY.toDouble()),
                        org.opencv.core.Point(maxX.toDouble(), minY.toDouble()),
                        org.opencv.core.Point(maxX.toDouble(), maxY.toDouble()),
                        org.opencv.core.Point(minX.toDouble(), maxY.toDouble()),
                    )
                    onConfirmarArea(cantos, usarIA)
                },
                enabled = areaMarcadaValida && !estado.processando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (estado.processando) {
                        if (usarIA) "Consultando IA..." else "Detectando..."
                    } else {
                        if (usarIA) "Usar esta área (IA)" else "Usar esta área"
                    },
                )
            }
            OutlinedButton(
                onClick = onPular,
                enabled = !estado.processando,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text("Pular (detectar na foto inteira)")
            }
        }
    }
}

@Composable
private fun SolicitarPermissao(onSolicitar: () -> Unit) {
    MensagemCentral(
        texto = "Pra vetorizar a faca, o app precisa de acesso à câmera.",
        acaoTexto = "Permitir acesso à câmera",
        onAcao = onSolicitar,
    )
}

@Composable
private fun MensagemCentral(texto: String, acaoTexto: String, onAcao: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text = texto, modifier = Modifier.align(Alignment.CenterHorizontally))
        Button(onClick = onAcao, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)) {
            Text(acaoTexto)
        }
    }
}

/** Guia visual de alinhamento — um quadrado semitransparente sugerindo onde posicionar o
 * marcador + a faca, igual ao pedido da especificação ("guia visual ao vivo"). Puramente
 * estético: a detecção real de marcador/contorno não depende de o usuário acertar essa guia,
 * só ajuda a enquadrar bem antes de capturar. */
@Composable
private fun GuiaAlinhamento() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val margemHorizontal = size.width * 0.1f
        val margemVertical = size.height * 0.2f
        drawRoundRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = Offset(margemHorizontal, margemVertical),
            size = androidx.compose.ui.geometry.Size(size.width - margemHorizontal * 2, size.height - margemVertical * 2),
            cornerRadius = CornerRadius(24f, 24f),
            style = Stroke(width = 6f),
        )
    }
}

private fun salvarFotoInterna(context: android.content.Context, facaId: Long, bitmap: Bitmap): String {
    val dir = File(context.filesDir, "moldevetor_fotos").apply { mkdirs() }
    val arquivo = File(dir, "faca_${facaId}_${System.currentTimeMillis()}.jpg")
    FileOutputStream(arquivo).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return arquivo.absolutePath
}
