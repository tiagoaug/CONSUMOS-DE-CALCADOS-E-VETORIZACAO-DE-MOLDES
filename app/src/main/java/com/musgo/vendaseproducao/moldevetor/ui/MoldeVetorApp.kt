package com.musgo.vendaseproducao.moldevetor.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.musgo.vendaseproducao.moldevetor.data.FacaMoldeRepository
import com.musgo.vendaseproducao.moldevetor.data.ContornoJsonMapper
import com.musgo.vendaseproducao.moldevetor.data.EncaixeJsonMapper
import com.musgo.vendaseproducao.moldevetor.data.db.MoldeVetorDatabase
import com.musgo.vendaseproducao.moldevetor.domain.Contorno
import com.musgo.vendaseproducao.moldevetor.domain.EncaixeConsumo
import com.musgo.vendaseproducao.moldevetor.domain.Ponto
import com.musgo.vendaseproducao.moldevetor.ui.cadastro.CadastroFacaScreen
import com.musgo.vendaseproducao.moldevetor.ui.captura.CapturaScreen
import com.musgo.vendaseproducao.moldevetor.ui.dashboard.DashboardScreen
import com.musgo.vendaseproducao.moldevetor.ui.grupos.GruposModelosScreen
import com.musgo.vendaseproducao.moldevetor.ui.lista.ListaFacasScreen
import com.musgo.vendaseproducao.moldevetor.ui.medida.MedidaReferenciaScreen
import com.musgo.vendaseproducao.moldevetor.ui.encaixe.EncaixeConsumoScreen
import com.musgo.vendaseproducao.moldevetor.ui.validacao.ValidacaoContornoScreen
import com.musgo.vendaseproducao.moldevetor.visao.ArucoMarkerDetector
import com.musgo.vendaseproducao.moldevetor.visao.PerspectiveCorrector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat

private sealed class MoldeVetorScreen {
    data object Dashboard : MoldeVetorScreen()
    data object Lista : MoldeVetorScreen()
    data class GruposModelos(
        val voltarParaCadastro: Boolean = false,
        val facaId: Long? = null,
        val cadastroVoltaAoDashboard: Boolean = false,
    ) : MoldeVetorScreen()
    data class Cadastro(val facaId: Long?, val voltarAoDashboard: Boolean = false) : MoldeVetorScreen()
    data class Captura(val facaId: Long, val corCapaArgb: Int = -16777216) : MoldeVetorScreen()
    data class Validacao(
        val facaId: Long,
        val contorno: Contorno,
        val fotoCorrigida: Bitmap?,
        val pxPorMm: Double,
        val fotoPath: String?,
        val corCapaArgb: Int = -16777216,
        val somenteVisualizacao: Boolean = false,
        val voltarParaLista: Boolean = false,
    ) : MoldeVetorScreen()
    data class Medida(val facaId: Long, val contorno: Contorno, val fotoPath: String?) : MoldeVetorScreen()
    data class Encaixe(
        val facaId: Long,
        val contorno: Contorno,
        val fotoPath: String?,
        val encaixeInicial: EncaixeConsumo?,
    ) : MoldeVetorScreen()
}

/** Orquestra a navegação entre as telas nativas do módulo (sem Navigation-Compose — mesmo
 * critério do Print Studio: poucas telas, troca simples de estado já basta). Fluxo completo de
 * projeto em três etapas independentes: vetorização, conferência de desvio e encaixe/consumo.
 * Cada etapa é salva ao ser confirmada e pode ser reaberta sem apagar as outras. */
@Composable
fun MoldeVetorApp(onClose: () -> Unit) {
    val context = LocalContext.current
    val repository = remember {
        val database = MoldeVetorDatabase.getInstance(context)
        FacaMoldeRepository(
            dao = database.facaMoldeDao(),
            grupoDao = database.grupoModeloDao(),
            filesDir = context.filesDir,
        )
    }
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf<MoldeVetorScreen>(MoldeVetorScreen.Dashboard) }
    var salvandoMedida by remember { mutableStateOf(false) }
    var erroMedida by remember { mutableStateOf<String?>(null) }

    fun salvarContornoDaEtapa(facaId: Long, contorno: Contorno, fotoPath: String?, corCapaArgb: Int? = null) {
        salvandoMedida = true
        erroMedida = null
        scope.launch {
            try {
                repository.salvarEtapaContorno(facaId, contorno, fotoPath, corCapaArgb)
                salvandoMedida = false
                screen = MoldeVetorScreen.Cadastro(facaId)
            } catch (e: Exception) {
                salvandoMedida = false
                erroMedida = "Erro ao salvar: ${e.message}"
            }
        }
    }

    fun abrirCaptura(facaId: Long) {
        scope.launch {
            val cor = withContext(Dispatchers.IO) { repository.getById(facaId)?.corCapaArgb } ?: -16777216
            screen = MoldeVetorScreen.Captura(facaId, cor)
        }
    }

    fun salvarEncaixeDaEtapa(facaId: Long, encaixe: EncaixeConsumo) {
        salvandoMedida = true
        erroMedida = null
        scope.launch {
            try {
                repository.salvarEtapaEncaixe(facaId, encaixe)
                salvandoMedida = false
                screen = MoldeVetorScreen.Cadastro(facaId)
            } catch (e: Exception) {
                salvandoMedida = false
                erroMedida = "Erro ao salvar encaixe: ${e.message}"
            }
        }
    }

    fun abrirBlocoSalvo(facaId: Long, somenteVisualizacao: Boolean) {
        scope.launch {
            val faca = withContext(Dispatchers.IO) { repository.getById(facaId) } ?: return@launch
            val contorno = faca.contornoJson?.let { runCatching { ContornoJsonMapper.deJson(it) }.getOrNull() }
            if (contorno == null) {
                screen = MoldeVetorScreen.Cadastro(facaId)
                return@launch
            }
            val fotoCorrigida = withContext(Dispatchers.Default) { reconstruirFotoCorrigida(faca.fotoPath) }
            screen = MoldeVetorScreen.Validacao(
                facaId = facaId,
                contorno = contorno,
                fotoCorrigida = fotoCorrigida,
                pxPorMm = PerspectiveCorrector.PX_POR_MM,
                fotoPath = faca.fotoPath,
                corCapaArgb = faca.corCapaArgb,
                somenteVisualizacao = somenteVisualizacao,
                voltarParaLista = somenteVisualizacao,
            )
        }
    }

    fun abrirConferenciaSalva(facaId: Long) {
        scope.launch {
            erroMedida = null
            val faca = withContext(Dispatchers.IO) { repository.getById(facaId) } ?: return@launch
            val contorno = faca.contornoJson?.let { runCatching { ContornoJsonMapper.deJson(it) }.getOrNull() }
                ?: return@launch
            screen = MoldeVetorScreen.Medida(facaId, contorno, faca.fotoPath)
        }
    }

    fun abrirEncaixeSalvo(facaId: Long) {
        scope.launch {
            erroMedida = null
            val faca = withContext(Dispatchers.IO) { repository.getById(facaId) } ?: return@launch
            val contorno = faca.contornoJson?.let { runCatching { ContornoJsonMapper.deJson(it) }.getOrNull() }
                ?: return@launch
            val encaixe = faca.encaixeJson?.let { runCatching { EncaixeJsonMapper.deJson(it) }.getOrNull() }
            screen = MoldeVetorScreen.Encaixe(facaId, contorno, faca.fotoPath, encaixe)
        }
    }

    when (val atual = screen) {
        is MoldeVetorScreen.Dashboard -> DashboardScreen(
            repository = repository,
            onAbrirBlocos = { screen = MoldeVetorScreen.Lista },
            onAbrirGrupos = { screen = MoldeVetorScreen.GruposModelos() },
            onNovaFaca = { screen = MoldeVetorScreen.Cadastro(null, voltarAoDashboard = true) },
            onBack = onClose,
        )

        is MoldeVetorScreen.Lista -> ListaFacasScreen(
            repository = repository,
            onFacaSelecionada = { id -> screen = MoldeVetorScreen.Cadastro(id) },
            onEditarBloco = { id -> screen = MoldeVetorScreen.Cadastro(id) },
            onNovaFaca = { screen = MoldeVetorScreen.Cadastro(null) },
            onBack = { screen = MoldeVetorScreen.Dashboard },
        )

        is MoldeVetorScreen.GruposModelos -> GruposModelosScreen(
            repository = repository,
            onBack = {
                screen = if (atual.voltarParaCadastro) {
                    MoldeVetorScreen.Cadastro(atual.facaId, atual.cadastroVoltaAoDashboard)
                } else {
                    MoldeVetorScreen.Dashboard
                }
            },
        )

        is MoldeVetorScreen.Cadastro -> CadastroFacaScreen(
            repository = repository,
            facaId = atual.facaId,
            onSaved = { screen = MoldeVetorScreen.Lista },
            onBack = {
                screen = if (atual.voltarAoDashboard) MoldeVetorScreen.Dashboard else MoldeVetorScreen.Lista
            },
            onGerenciarGrupos = {
                screen = MoldeVetorScreen.GruposModelos(
                    voltarParaCadastro = true,
                    facaId = atual.facaId,
                    cadastroVoltaAoDashboard = atual.voltarAoDashboard,
                )
            },
            onVetorizar = { id -> abrirCaptura(id) },
            onEditarVetorizacao = { id -> abrirBlocoSalvo(id, somenteVisualizacao = false) },
            onConferirDesvio = { id -> abrirConferenciaSalva(id) },
            onEditarEncaixe = { id -> abrirEncaixeSalvo(id) },
            // Pedido do Tiago: "opção de criar geometrias quadradas ou retangulares sem precisar
            // de capturas de fotografias, inserindo seu lado 1 e lado 2" — pula Captura por
            // completo, monta um retângulo com os lados digitados e abre direto na Validação
            // (mesmas ferramentas de ajuste de sempre, só sem foto de fundo).
            onCriarManual = { id, larguraMm, alturaMm ->
                val retangulo = Contorno(
                    externo = listOf(
                        Ponto(xMm = 0.0, yMm = 0.0),
                        Ponto(xMm = larguraMm, yMm = 0.0),
                        Ponto(xMm = larguraMm, yMm = alturaMm),
                        Ponto(xMm = 0.0, yMm = alturaMm),
                    ),
                )
                scope.launch {
                    val cor = withContext(Dispatchers.IO) { repository.getById(id)?.corCapaArgb } ?: -16777216
                    screen = MoldeVetorScreen.Validacao(
                        facaId = id,
                        contorno = retangulo,
                        fotoCorrigida = null,
                        pxPorMm = PerspectiveCorrector.PX_POR_MM,
                        fotoPath = null,
                        corCapaArgb = cor,
                        voltarParaLista = false,
                    )
                }
            },
        )

        is MoldeVetorScreen.Captura -> CapturaScreen(
            facaId = atual.facaId,
            onDetectado = { contorno, fotoCorrigida, pxPorMm, fotoPath ->
                screen = MoldeVetorScreen.Validacao(
                    atual.facaId,
                    contorno,
                    fotoCorrigida,
                    pxPorMm,
                    fotoPath,
                    atual.corCapaArgb,
                )
            },
            onBack = { screen = MoldeVetorScreen.Cadastro(atual.facaId) },
        )

        is MoldeVetorScreen.Validacao -> ValidacaoContornoScreen(
            contornoInicial = atual.contorno,
            fotoCorrigida = atual.fotoCorrigida,
            pxPorMm = atual.pxPorMm,
            corCapaArgbInicial = atual.corCapaArgb,
            somenteVisualizacao = atual.somenteVisualizacao,
            onConfirmado = { contornoConfirmado, corCapaArgb ->
                if (atual.somenteVisualizacao) {
                    screen = MoldeVetorScreen.Lista
                } else {
                    salvarContornoDaEtapa(atual.facaId, contornoConfirmado, atual.fotoPath, corCapaArgb)
                }
            },
            onBack = {
                screen = if (atual.voltarParaLista) MoldeVetorScreen.Lista else MoldeVetorScreen.Cadastro(atual.facaId)
            },
        )

        is MoldeVetorScreen.Medida -> MedidaReferenciaScreen(
            contorno = atual.contorno,
            salvando = salvandoMedida,
            erro = erroMedida,
            onSalvar = { salvarContornoDaEtapa(atual.facaId, atual.contorno, atual.fotoPath) },
            onBack = { screen = MoldeVetorScreen.Cadastro(atual.facaId) },
            onAplicarCorrecao = { contornoCorrigido -> screen = atual.copy(contorno = contornoCorrigido) },
        )

        is MoldeVetorScreen.Encaixe -> EncaixeConsumoScreen(
            contorno = atual.contorno,
            encaixeInicial = atual.encaixeInicial,
            salvando = salvandoMedida,
            erro = erroMedida,
            onConfirmado = { encaixe -> salvarEncaixeDaEtapa(atual.facaId, encaixe) },
            onBack = { screen = MoldeVetorScreen.Cadastro(atual.facaId) },
        )
    }
}

private fun reconstruirFotoCorrigida(fotoPath: String?): Bitmap? {
    val fotoBruta = fotoPath?.let(BitmapFactory::decodeFile) ?: return null
    val imagemOriginal = Mat()
    return runCatching {
        Utils.bitmapToMat(fotoBruta, imagemOriginal)
        val marcador = ArucoMarkerDetector.detectar(imagemOriginal) ?: return@runCatching null
        val resultado = PerspectiveCorrector.corrigir(imagemOriginal, marcador.cantosPx)
        try {
            Bitmap.createBitmap(
                resultado.imagemCorrigida.cols(),
                resultado.imagemCorrigida.rows(),
                Bitmap.Config.ARGB_8888,
            ).also { Utils.matToBitmap(resultado.imagemCorrigida, it) }
        } finally {
            resultado.imagemCorrigida.release()
        }
    }.getOrNull().also {
        imagemOriginal.release()
        fotoBruta.recycle()
    }
}
