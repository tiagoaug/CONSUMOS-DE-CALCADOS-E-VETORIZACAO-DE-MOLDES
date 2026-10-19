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
    data class Captura(val facaId: Long) : MoldeVetorScreen()
    data class Validacao(
        val facaId: Long,
        val contorno: Contorno,
        val fotoCorrigida: Bitmap?,
        val pxPorMm: Double,
        val fotoPath: String?,
        val somenteVisualizacao: Boolean = false,
        val voltarParaLista: Boolean = false,
    ) : MoldeVetorScreen()
    data class Medida(val facaId: Long, val contorno: Contorno, val fotoPath: String?) : MoldeVetorScreen()
    data class Encaixe(val facaId: Long, val contorno: Contorno, val fotoPath: String?) : MoldeVetorScreen()
}

/** Orquestra a navegação entre as telas nativas do módulo (sem Navigation-Compose — mesmo
 * critério do Print Studio: poucas telas, troca simples de estado já basta). Fluxo completo de
 * vetorização (Fases 3-4): Captura → Validação (vértices arrastáveis) → Medida (desvio opcional
 * por paquímetro) → salva via [FacaMoldeRepository.salvarResultadoVetorizacao]. */
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

    fun salvarResultado(facaId: Long, contorno: Contorno, encaixe: EncaixeConsumo, fotoPath: String?) {
        salvandoMedida = true
        erroMedida = null
        scope.launch {
            try {
                repository.salvarResultadoVetorizacao(facaId, contorno, encaixe, fotoPath)
                salvandoMedida = false
                // No aplicativo independente, o projeto concluído volta para a lista. Ali o
                // usuário exporta o pacote .bloco que será importado no sistema principal.
                screen = MoldeVetorScreen.Lista
            } catch (e: Exception) {
                salvandoMedida = false
                erroMedida = "Erro ao salvar: ${e.message}"
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
                somenteVisualizacao = somenteVisualizacao,
                voltarParaLista = true,
            )
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
            onVisualizarBloco = { id -> abrirBlocoSalvo(id, somenteVisualizacao = true) },
            onEditarBloco = { id -> abrirBlocoSalvo(id, somenteVisualizacao = false) },
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
            onVetorizar = { id -> screen = MoldeVetorScreen.Captura(id) },
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
                screen = MoldeVetorScreen.Validacao(
                    facaId = id,
                    contorno = retangulo,
                    fotoCorrigida = null,
                    pxPorMm = PerspectiveCorrector.PX_POR_MM,
                    fotoPath = null,
                    voltarParaLista = true,
                )
            },
        )

        is MoldeVetorScreen.Captura -> CapturaScreen(
            facaId = atual.facaId,
            onDetectado = { contorno, fotoCorrigida, pxPorMm, fotoPath ->
                screen = MoldeVetorScreen.Validacao(atual.facaId, contorno, fotoCorrigida, pxPorMm, fotoPath)
            },
            onBack = { screen = MoldeVetorScreen.Cadastro(atual.facaId) },
        )

        is MoldeVetorScreen.Validacao -> ValidacaoContornoScreen(
            contornoInicial = atual.contorno,
            fotoCorrigida = atual.fotoCorrigida,
            pxPorMm = atual.pxPorMm,
            somenteVisualizacao = atual.somenteVisualizacao,
            onConfirmado = { contornoConfirmado ->
                screen = MoldeVetorScreen.Medida(atual.facaId, contornoConfirmado, atual.fotoPath)
            },
            onBack = {
                screen = if (atual.voltarParaLista) MoldeVetorScreen.Lista else MoldeVetorScreen.Captura(atual.facaId)
            },
        )

        is MoldeVetorScreen.Medida -> MedidaReferenciaScreen(
            contorno = atual.contorno,
            salvando = salvandoMedida,
            erro = erroMedida,
            onSalvar = { screen = MoldeVetorScreen.Encaixe(atual.facaId, atual.contorno, atual.fotoPath) },
            onBack = { screen = MoldeVetorScreen.Cadastro(atual.facaId) },
            onAplicarCorrecao = { contornoCorrigido -> screen = atual.copy(contorno = contornoCorrigido) },
        )

        is MoldeVetorScreen.Encaixe -> EncaixeConsumoScreen(
            contorno = atual.contorno,
            salvando = salvandoMedida,
            erro = erroMedida,
            onConfirmado = { encaixe -> salvarResultado(atual.facaId, atual.contorno, encaixe, atual.fotoPath) },
            onBack = { screen = MoldeVetorScreen.Medida(atual.facaId, atual.contorno, atual.fotoPath) },
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
