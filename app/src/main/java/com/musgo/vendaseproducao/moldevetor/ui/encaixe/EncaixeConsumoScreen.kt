package com.musgo.vendaseproducao.moldevetor.ui.encaixe

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AlignHorizontalLeft
import androidx.compose.material.icons.filled.AlignHorizontalRight
import androidx.compose.material.icons.filled.AlignVerticalBottom
import androidx.compose.material.icons.filled.AlignVerticalCenter
import androidx.compose.material.icons.filled.AlignVerticalTop
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musgo.vendaseproducao.moldevetor.domain.CalculoArea
import com.musgo.vendaseproducao.moldevetor.domain.Contorno
import com.musgo.vendaseproducao.moldevetor.domain.EncaixeConsumo
import com.musgo.vendaseproducao.moldevetor.domain.InstanciaEncaixe
import com.musgo.vendaseproducao.moldevetor.domain.Ponto
import com.musgo.vendaseproducao.moldevetor.domain.TipoModuloEncaixe
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val AzulDestaque = Color(0xFF2563EB)
private val Fundo = Color.White
private val Borda = Color(0xFFE2E8F0)
private val Texto = Color(0xFF1E293B)
private val CoresBlocos = listOf(Color(0xFF22C55E), Color(0xFF2563EB), Color(0xFFF59E0B), Color(0xFF0D9488))

private data class BlocoBase(val pontos: List<Offset>, val largura: Float, val altura: Float)
private data class LimitesBloco(val esquerda: Float, val topo: Float, val direita: Float, val base: Float) {
    val centroX get() = (esquerda + direita) / 2f
    val centroY get() = (topo + base) / 2f
}
private data class GuiaAlinhamento(val horizontal: Boolean, val valorMm: Float)
private data class TamanhoFolha(val nome: String, val larguraMm: Float, val alturaMm: Float)

/** Categorias do painel de ferramentas, sempre visíveis como abas — pedido do Tiago: "crie a
 * mesma tela de edição de facas, mesmo estilo de área de visualização e ferramentas, cards para
 * ferramentas, cada uma em uma linha sempre visível na tela". Mesma estrutura do editor de facas
 * (visualização grande e constante em cima, painel fixo com abas embaixo). */
// Pedido do Tiago: "coloque um botão de configurações e mova para lá configurações de tamanho,
// tipo de consumo, paralelogramo ou retângulo, coloque ao lado de resultados" — CONFIGURACOES
// reúne o que antes era a aba TAMANHO + o seletor Retângulo/Paralelogramo (que morava em
// BLOCOS), e fica logo depois de RESULTADO na ordem das abas.
// Pedido do Tiago: "gostaria que tivéssemos em consumos as opções de visualização... visualização
// e seleção" — espelha as categorias do editor de facas (Seleção/Transformação/Visualização).
private enum class CategoriaEncaixe { BLOCOS, SELECAO, TRANSFORMAR, VISUALIZACAO, RESULTADO, CONFIGURACOES }
private enum class ModoSelecaoEncaixe { MULTI_TOQUE, ARRASTO }

private val TamanhosFolha = listOf(
    TamanhoFolha("1 × 1 m", 1000f, 1000f),
    TamanhoFolha("1 × 1,5 m", 1000f, 1500f),
    TamanhoFolha("1,2 × 1,8 m", 1200f, 1800f),
    TamanhoFolha("1,4 × 2 m", 1400f, 2000f),
    TamanhoFolha("2 × 2 m", 2000f, 2000f),
)

private fun pontosTransformados(item: InstanciaEncaixe, bloco: BlocoBase): List<Offset> {
    val centro = Offset(bloco.largura / 2f, bloco.altura / 2f)
    val rad = Math.toRadians(item.rotacaoGraus)
    val cosA = cos(rad).toFloat()
    val sinA = sin(rad).toFloat()
    return bloco.pontos.map { ponto ->
        val dxOriginal = ponto.x - centro.x
        val dyOriginal = ponto.y - centro.y
        val dx = if (item.espelhadaHorizontal) -dxOriginal else dxOriginal
        val dy = if (item.invertidaVertical) -dyOriginal else dyOriginal
        Offset(
            item.xMm.toFloat() + centro.x + dx * cosA - dy * sinA,
            item.yMm.toFloat() + centro.y + dx * sinA + dy * cosA,
        )
    }
}

private fun limitesBloco(item: InstanciaEncaixe, bloco: BlocoBase): LimitesBloco {
    val girados = pontosTransformados(item, bloco)
    return LimitesBloco(
        esquerda = girados.minOf { it.x },
        topo = girados.minOf { it.y },
        direita = girados.maxOf { it.x },
        base = girados.maxOf { it.y },
    )
}

private fun cruzamento(a: Offset, b: Offset, c: Offset): Float =
    (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)

private fun pontoNoSegmento(ponto: Offset, inicio: Offset, fim: Offset): Boolean {
    val tolerancia = 0.001f
    if (abs(cruzamento(inicio, fim, ponto)) > tolerancia) return false
    return ponto.x >= min(inicio.x, fim.x) - tolerancia &&
        ponto.x <= maxOf(inicio.x, fim.x) + tolerancia &&
        ponto.y >= min(inicio.y, fim.y) - tolerancia &&
        ponto.y <= maxOf(inicio.y, fim.y) + tolerancia
}

private fun segmentosCruzam(a: Offset, b: Offset, c: Offset, d: Offset): Boolean {
    val abC = cruzamento(a, b, c)
    val abD = cruzamento(a, b, d)
    val cdA = cruzamento(c, d, a)
    val cdB = cruzamento(c, d, b)
    if ((abC > 0f) != (abD > 0f) && (cdA > 0f) != (cdB > 0f)) return true
    return pontoNoSegmento(c, a, b) || pontoNoSegmento(d, a, b) ||
        pontoNoSegmento(a, c, d) || pontoNoSegmento(b, c, d)
}

private fun pontoDentroDoPoligono(ponto: Offset, poligono: List<Offset>): Boolean {
    var dentro = false
    var j = poligono.lastIndex
    for (i in poligono.indices) {
        val a = poligono[i]
        val b = poligono[j]
        if ((a.y > ponto.y) != (b.y > ponto.y) &&
            ponto.x < (b.x - a.x) * (ponto.y - a.y) / ((b.y - a.y).takeIf { abs(it) > 0.0001f } ?: 0.0001f) + a.x
        ) dentro = !dentro
        j = i
    }
    return dentro
}

// Pedido do Tiago: "o afastamento mínimo entre blocos deve ter valor para as laterais e acima e
// abaixo" — valores separados por eixo em vez de uma distância única em qualquer direção. Usa a
// caixa delimitadora de cada peça pra achar o vão horizontal (gapX) e vertical (gapY) entre elas:
// se estão lado a lado (uma acima da outra se sobrepondo em X), o vão relevante é vertical, e
// vice-versa; na diagonal (sem sobreposição em nenhum eixo), basta UM dos dois vãos já atender ao
// mínimo daquele eixo — a distância real na diagonal é sempre maior que qualquer um dos dois vãos
// isolados, então essa peça nunca fica mais perto do que o eixo que já passou permite.
private fun blocosRespeitamAfastamento(
    a: List<Offset>,
    b: List<Offset>,
    afastamentoHorizontalMm: Float,
    afastamentoVerticalMm: Float,
): Boolean {
    if (a.size < 3 || b.size < 3) return true
    for (i in a.indices) {
        val a1 = a[i]
        val a2 = a[(i + 1) % a.size]
        for (j in b.indices) {
            val b1 = b[j]
            val b2 = b[(j + 1) % b.size]
            if (segmentosCruzam(a1, a2, b1, b2)) return false
        }
    }
    if (pontoDentroDoPoligono(a.first(), b) || pontoDentroDoPoligono(b.first(), a)) return false

    val minXA = a.minOf { it.x }; val maxXA = a.maxOf { it.x }
    val minYA = a.minOf { it.y }; val maxYA = a.maxOf { it.y }
    val minXB = b.minOf { it.x }; val maxXB = b.maxOf { it.x }
    val minYB = b.minOf { it.y }; val maxYB = b.maxOf { it.y }
    val gapX = max(0f, max(minXA, minXB) - min(maxXA, maxXB))
    val gapY = max(0f, max(minYA, minYB) - min(maxYA, maxYB))
    val tolerancia = 0.001f
    return when {
        gapX > tolerancia && gapY <= tolerancia -> gapX + tolerancia >= afastamentoHorizontalMm
        gapY > tolerancia && gapX <= tolerancia -> gapY + tolerancia >= afastamentoVerticalMm
        gapX > tolerancia && gapY > tolerancia -> gapX + tolerancia >= afastamentoHorizontalMm || gapY + tolerancia >= afastamentoVerticalMm
        else -> true
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EncaixeConsumoScreen(
    contorno: Contorno,
    salvando: Boolean,
    erro: String?,
    onConfirmado: (EncaixeConsumo) -> Unit,
    onBack: () -> Unit,
) {
    // Pedido do Tiago: "opção de girar a tela clicando em uma seta na visualização e a barra de
    // ferramentas ficando lateral" (em Consumo e em Encaixes) — gira a Activity pra paisagem e
    // troca o empilhamento vertical (prancheta em cima, ferramentas embaixo) por lado a lado.
    val context = LocalContext.current
    val activity = remember(context) { context.paraActivityEncaixe() }
    var telaGirada by remember { mutableStateOf(false) }
    LaunchedEffect(telaGirada, activity) {
        activity?.requestedOrientation = if (telaGirada) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    DisposableEffect(Unit) {
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }
    val blocos = remember(contorno) {
        (listOf(contorno.externo) + contorno.blocos).filter { it.size >= 3 }.map { pontos ->
            val minX = pontos.minOf { it.xMm }
            val minY = pontos.minOf { it.yMm }
            val normalizados = pontos.map { Offset((it.xMm - minX).toFloat(), (it.yMm - minY).toFloat()) }
            BlocoBase(
                pontos = normalizados,
                largura = normalizados.maxOf { it.x }.coerceAtLeast(1f),
                altura = normalizados.maxOf { it.y }.coerceAtLeast(1f),
            )
        }
    }
    var tipo by remember { mutableStateOf(TipoModuloEncaixe.RETANGULO) }
    var larguraMm by remember { mutableStateOf(1000f) }
    var alturaMm by remember { mutableStateOf(1000f) }
    var larguraTexto by remember { mutableStateOf("1000") }
    var alturaTexto by remember { mutableStateOf("1000") }
    var deslocamentoTexto by remember { mutableStateOf("0") }
    var tamanhoFolhaSelecionado by remember { mutableStateOf("1 × 1 m") }
    // Pedido do Tiago: "o afastamento mínimo entre blocos deve ter valor para as laterais e
    // acima e abaixo" — dois valores em vez de um só, cada um aplicado ao seu eixo (ver
    // blocosRespeitamAfastamento acima).
    var afastamentoHorizontalMm by remember { mutableStateOf(5f) }
    var afastamentoVerticalMm by remember { mutableStateOf(5f) }
    // Pedido do Tiago: "distância mínima da linha do retângulo ou paralelogramo aos blocos" —
    // diferente do afastamento ENTRE blocos acima: esta é a margem entre a borda do módulo
    // (retângulo/paralelogramo) e qualquer peça encostada nela.
    var margemModuloMm by remember { mutableStateOf(0f) }
    var deslocamentoMm by remember { mutableStateOf(0f) }
    var blocoParaAdicionar by remember { mutableStateOf(0) }
    var selecionado by remember { mutableStateOf(0) }
    // Pedido do Tiago: "crie em transformar/blocos, selecionar blocos, crie um popup ao clicar
    // para selecionar o bloco desejado... editar selecionados, podendo rotacionar, mover,
    // resetar posições, para poder fazer os encaixes livremente". Seleção múltipla separada do
    // "selecionado" de sempre (que continua sendo o alvo único pra rotacionar/duplicar/excluir
    // um bloco só, e pra escolher a referência de alinhamento) — quando [selecionados] tem mais
    // de 1 item, a aba Transformar troca pro painel de edição em grupo.
    var selecionados by remember { mutableStateOf(setOf<Int>()) }
    var mostrarPopupSelecionarBlocos by remember { mutableStateOf(false) }
    var modoSelecaoEncaixe by remember { mutableStateOf(ModoSelecaoEncaixe.MULTI_TOQUE) }
    var selecaoArrastoInicio by remember { mutableStateOf<Offset?>(null) }
    var selecaoArrastoAtual by remember { mutableStateOf<Offset?>(null) }
    // Pedido do Tiago (ajuste fino de selecionados): "poderemos escolher nos botões, opção de
    // mover e rotacionar usando as setas, mudando apenas a função de rotação ou mover" — as
    // MESMAS 4 setas fazem coisas diferentes dependendo deste toggle.
    var modoAjusteFinoGrupo by remember { mutableStateOf("MOVER") }
    var passoMovimentoMm by remember { mutableStateOf(0.2f) }
    var passoRotacaoGraus by remember { mutableStateOf(1f) }
    var referenciaAlinhamento by remember { mutableStateOf(if (blocos.size > 1) 1 else 0) }
    var guiasAlinhamento by remember { mutableStateOf(listOf<GuiaAlinhamento>()) }
    var mensagemPosicionamento by remember { mutableStateOf<String?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var instancias by remember {
        var cursor = 5f
        mutableStateOf(blocos.mapIndexed { indice, bloco ->
            InstanciaEncaixe(indice, cursor.toDouble(), 5.0).also { cursor += bloco.largura + 5f }
        })
    }

    val areaRealMm2 = remember(contorno) { CalculoArea.calcularAreaMm2(contorno) }
    val areaModuloMm2 = larguraMm.toDouble() * alturaMm.toDouble()
    val consumoMm2 = if (instancias.isEmpty()) 0.0 else areaModuloMm2 / instancias.size

    fun instanciaValida(candidata: InstanciaEncaixe, indicesIgnorados: Set<Int>): Boolean {
        val bloco = blocos.getOrNull(candidata.blocoIndice) ?: return false
        val pontosCandidata = pontosTransformados(candidata, bloco)
        val limites = limitesBloco(candidata, bloco)
        if (limites.esquerda < margemModuloMm || limites.topo < margemModuloMm || limites.direita > larguraMm - margemModuloMm || limites.base > alturaMm - margemModuloMm) return false
        return instancias.indices.all { indice ->
            if (indice in indicesIgnorados) return@all true
            val outra = instancias[indice]
            val baseOutra = blocos.getOrNull(outra.blocoIndice) ?: return@all true
            blocosRespeitamAfastamento(pontosCandidata, pontosTransformados(outra, baseOutra), afastamentoHorizontalMm, afastamentoVerticalMm)
        }
    }
    fun instanciaValida(candidata: InstanciaEncaixe, indiceIgnorado: Int): Boolean = instanciaValida(candidata, setOf(indiceIgnorado))

    val indicesComConflito = buildSet {
        instancias.forEachIndexed { indice, item ->
            val base = blocos.getOrNull(item.blocoIndice) ?: return@forEachIndexed
            val pontosA = pontosTransformados(item, base)
            val a = limitesBloco(item, base)
            if (a.esquerda < margemModuloMm || a.topo < margemModuloMm || a.direita > larguraMm - margemModuloMm || a.base > alturaMm - margemModuloMm) add(indice)
            for (outroIndice in indice + 1 until instancias.size) {
                val outro = instancias[outroIndice]
                val baseOutra = blocos.getOrNull(outro.blocoIndice) ?: continue
                if (!blocosRespeitamAfastamento(pontosA, pontosTransformados(outro, baseOutra), afastamentoHorizontalMm, afastamentoVerticalMm)) {
                    add(indice)
                    add(outroIndice)
                }
            }
        }
    }

    fun aplicarTamanhoFolha(tamanho: TamanhoFolha) {
        larguraMm = tamanho.larguraMm
        alturaMm = tamanho.alturaMm
        larguraTexto = tamanho.larguraMm.roundToInt().toString()
        alturaTexto = tamanho.alturaMm.roundToInt().toString()
        tamanhoFolhaSelecionado = tamanho.nome
        guiasAlinhamento = emptyList()
    }

    fun encontrarPosicaoLivre(modelo: InstanciaEncaixe): InstanciaEncaixe? {
        val passoBusca = max(10f, max(afastamentoHorizontalMm, afastamentoVerticalMm))
        var y = margemModuloMm
        while (y <= alturaMm) {
            var x = margemModuloMm
            while (x <= larguraMm) {
                val candidato = modelo.copy(xMm = x.toDouble(), yMm = y.toDouble(), travada = false)
                if (instanciaValida(candidato, -1)) return candidato
                x += passoBusca
            }
            y += passoBusca
        }
        return null
    }

    fun alinharSelecionado(alinhamento: String) {
        val atual = instancias.getOrNull(selecionado) ?: return
        if (atual.travada) {
            mensagemPosicionamento = "Destrave o bloco para alinhá-lo."
            return
        }
        val referencia = referenciaAlinhamento.takeIf { it in instancias.indices && it != selecionado }
            ?: instancias.indices.firstOrNull { it != selecionado }
        if (referencia == null) {
            mensagemPosicionamento = "Adicione outro bloco para usar como referência."
            return
        }
        referenciaAlinhamento = referencia
        val baseAtual = blocos.getOrNull(atual.blocoIndice) ?: return
        val itemReferencia = instancias[referencia]
        val baseReferencia = blocos.getOrNull(itemReferencia.blocoIndice) ?: return
        val a = limitesBloco(atual, baseAtual)
        val r = limitesBloco(itemReferencia, baseReferencia)
        var dx = 0f
        var dy = 0f
        val guia = when (alinhamento) {
            "ESQUERDA" -> { dx = r.esquerda - a.esquerda; GuiaAlinhamento(false, r.esquerda) }
            "CENTRO_X" -> { dx = r.centroX - a.centroX; GuiaAlinhamento(false, r.centroX) }
            "DIREITA" -> { dx = r.direita - a.direita; GuiaAlinhamento(false, r.direita) }
            "TOPO" -> { dy = r.topo - a.topo; GuiaAlinhamento(true, r.topo) }
            "CENTRO_Y" -> { dy = r.centroY - a.centroY; GuiaAlinhamento(true, r.centroY) }
            else -> { dy = r.base - a.base; GuiaAlinhamento(true, r.base) }
        }
        val candidato = atual.copy(xMm = atual.xMm + dx, yMm = atual.yMm + dy)
        if (instanciaValida(candidato, selecionado)) {
            instancias = instancias.mapIndexed { indice, item -> if (indice == selecionado) candidato else item }
            guiasAlinhamento = guiasAlinhamento.filter { it.horizontal != guia.horizontal } + guia
            mensagemPosicionamento = "Bloco alinhado pela referência selecionada."
        } else {
            mensagemPosicionamento = "Esse alinhamento ultrapassa a folha ou viola o afastamento mínimo."
        }
    }

    // Edição em GRUPO — pedido do Tiago: "editar selecionados, podendo rotacionar, mover,
    // resetar posições, para poder fazer os encaixes livremente". As três funções abaixo
    // ignoram os próprios blocos do grupo ao validar (preservam a disposição relativa deles
    // entre si, só checam contra quem está FORA do grupo).
    fun moverGrupo(dxMm: Float, dyMm: Float) {
        if (selecionados.isEmpty()) return
        val travados = selecionados.filter { instancias.getOrNull(it)?.travada == true }
        if (travados.isNotEmpty()) {
            mensagemPosicionamento = "Destrave todos os blocos selecionados antes de mover em grupo."
            return
        }
        val candidatos = instancias.mapIndexed { indice, item ->
            if (indice in selecionados) item.copy(xMm = item.xMm + dxMm, yMm = item.yMm + dyMm) else item
        }
        if (selecionados.all { indice -> instanciaValida(candidatos[indice], selecionados) }) {
            instancias = candidatos
            mensagemPosicionamento = null
        } else {
            mensagemPosicionamento = "Esse movimento tiraria algum bloco da folha ou do afastamento mínimo."
        }
    }

    fun rotacionarGrupo(deltaGraus: Double) {
        if (selecionados.isEmpty()) return
        val travados = selecionados.filter { instancias.getOrNull(it)?.travada == true }
        if (travados.isNotEmpty()) {
            mensagemPosicionamento = "Destrave todos os blocos selecionados antes de girar em grupo."
            return
        }
        val candidatos = instancias.mapIndexed { indice, item ->
            if (indice in selecionados) item.copy(rotacaoGraus = ((item.rotacaoGraus + deltaGraus) % 360.0 + 360.0) % 360.0) else item
        }
        if (selecionados.all { indice -> instanciaValida(candidatos[indice], selecionados) }) {
            instancias = candidatos
            mensagemPosicionamento = null
        } else {
            mensagemPosicionamento = "Não há espaço pra girar o grupo mantendo o afastamento mínimo."
        }
    }

    // Reseta cada bloco selecionado de volta a uma posição livre (rotação zerada), um de cada
    // vez — útil quando o grupo ficou emaranhado e é mais fácil recomeçar o posicionamento dele
    // do que desfazer manualmente.
    fun resetarPosicoesGrupo() {
        if (selecionados.isEmpty()) return
        var atualInstancias = instancias
        selecionados.sorted().forEach { indice ->
            val item = atualInstancias.getOrNull(indice) ?: return@forEach
            instancias = atualInstancias
            val livre = encontrarPosicaoLivre(item.copy(rotacaoGraus = 0.0, espelhadaHorizontal = false, invertidaVertical = false))
            if (livre != null) {
                atualInstancias = atualInstancias.mapIndexed { i, original -> if (i == indice) livre else original }
            }
        }
        instancias = atualInstancias
        mensagemPosicionamento = "Posições dos blocos selecionados foram reorganizadas."
    }

    fun rotacionarSelecionado(delta: Double) {
        if (selecionado !in instancias.indices || instancias[selecionado].travada) return
        val atual = instancias[selecionado]
        val candidato = atual.copy(rotacaoGraus = ((atual.rotacaoGraus + delta) % 360.0 + 360.0) % 360.0)
        if (instanciaValida(candidato, selecionado)) {
            instancias = instancias.mapIndexed { indice, item -> if (indice == selecionado) candidato else item }
            mensagemPosicionamento = null
        } else {
            mensagemPosicionamento = "Não há espaço para girar mantendo o afastamento mínimo entre os blocos."
        }
    }

    fun moverSelecionado(dxMm: Float, dyMm: Float) {
        if (selecionado !in instancias.indices) return
        val atual = instancias[selecionado]
        if (atual.travada) {
            mensagemPosicionamento = "Destrave o bloco antes de mover."
            return
        }
        val candidato = atual.copy(xMm = atual.xMm + dxMm, yMm = atual.yMm + dyMm)
        if (instanciaValida(candidato, selecionado)) {
            instancias = instancias.mapIndexed { indice, item -> if (indice == selecionado) candidato else item }
            mensagemPosicionamento = null
        } else {
            mensagemPosicionamento = "Esse movimento tiraria o bloco da folha ou do afastamento mínimo."
        }
    }

    fun aplicarTransformacaoRapida(
        nome: String,
        transformar: (InstanciaEncaixe) -> InstanciaEncaixe,
    ) {
        val alvos = if (selecionados.size > 1) {
            selecionados
        } else {
            setOf(selecionado).filter { it in instancias.indices }.toSet()
        }
        if (alvos.isEmpty()) return
        if (alvos.any { instancias.getOrNull(it)?.travada == true }) {
            mensagemPosicionamento = "Destrave os blocos antes de $nome."
            return
        }
        val candidatos = instancias.mapIndexed { indice, item ->
            if (indice in alvos) transformar(item) else item
        }
        val todosValidos = alvos.all alvo@ { indice ->
            val candidata = candidatos.getOrNull(indice) ?: return@alvo false
            val base = blocos.getOrNull(candidata.blocoIndice) ?: return@alvo false
            val limites = limitesBloco(candidata, base)
            val dentroDaFolha = limites.esquerda >= margemModuloMm &&
                limites.topo >= margemModuloMm &&
                limites.direita <= larguraMm - margemModuloMm &&
                limites.base <= alturaMm - margemModuloMm
            dentroDaFolha && candidatos.indices.all outro@ { outroIndice ->
                if (outroIndice == indice) return@outro true
                val outra = candidatos[outroIndice]
                val baseOutra = blocos.getOrNull(outra.blocoIndice) ?: return@outro true
                blocosRespeitamAfastamento(
                    pontosTransformados(candidata, base),
                    pontosTransformados(outra, baseOutra),
                    afastamentoHorizontalMm,
                    afastamentoVerticalMm,
                )
            }
        }
        if (todosValidos) {
            instancias = candidatos
            guiasAlinhamento = emptyList()
            mensagemPosicionamento = null
        } else {
            mensagemPosicionamento = "Nao ha espaco para $nome mantendo os limites e o afastamento minimo."
        }
    }

    fun espelharSelecionados() = aplicarTransformacaoRapida("espelhar") { item ->
        item.copy(espelhadaHorizontal = !item.espelhadaHorizontal)
    }

    fun inverterSelecionados() = aplicarTransformacaoRapida("inverter") { item ->
        item.copy(invertidaVertical = !item.invertidaVertical)
    }

    fun definirRotacaoSelecionados(angulo: Double) = aplicarTransformacaoRapida("girar para ${angulo.roundToInt()} graus") { item ->
        item.copy(rotacaoGraus = ((angulo % 360.0) + 360.0) % 360.0)
    }

    fun duplicarSelecionados() {
        val indicesOriginais = (if (selecionados.isNotEmpty()) selecionados else setOf(selecionado))
            .filter { it in instancias.indices }
            .sorted()
        if (indicesOriginais.isEmpty()) return

        val originais = indicesOriginais.map { instancias[it] }
        val pontosOriginais = originais.mapNotNull { item ->
            blocos.getOrNull(item.blocoIndice)?.let { base -> pontosTransformados(item, base) }
        }
        if (pontosOriginais.size != originais.size) return

        val esquerdaGrupo = pontosOriginais.minOf { poligono -> poligono.minOf { it.x } }
        val topoGrupo = pontosOriginais.minOf { poligono -> poligono.minOf { it.y } }
        val direitaGrupo = pontosOriginais.maxOf { poligono -> poligono.maxOf { it.x } }
        val baseGrupo = pontosOriginais.maxOf { poligono -> poligono.maxOf { it.y } }
        val larguraGrupo = direitaGrupo - esquerdaGrupo
        val alturaGrupo = baseGrupo - topoGrupo

        fun criarCopias(dx: Float, dy: Float): List<InstanciaEncaixe> = originais.map { item ->
            item.copy(
                xMm = item.xMm + dx,
                yMm = item.yMm + dy,
                travada = false,
            )
        }

        fun grupoDuplicadoValido(copias: List<InstanciaEncaixe>): Boolean {
            val poligonos = copias.map { item ->
                val base = blocos.getOrNull(item.blocoIndice) ?: return false
                pontosTransformados(item, base)
            }
            poligonos.forEach { poligono ->
                val esquerda = poligono.minOf { it.x }
                val topo = poligono.minOf { it.y }
                val direita = poligono.maxOf { it.x }
                val base = poligono.maxOf { it.y }
                if (esquerda < margemModuloMm || topo < margemModuloMm ||
                    direita > larguraMm - margemModuloMm || base > alturaMm - margemModuloMm
                ) return false
            }
            poligonos.forEach { candidata ->
                instancias.forEach { existente ->
                    val baseExistente = blocos.getOrNull(existente.blocoIndice) ?: return@forEach
                    if (!blocosRespeitamAfastamento(
                            candidata,
                            pontosTransformados(existente, baseExistente),
                            afastamentoHorizontalMm,
                            afastamentoVerticalMm,
                        )
                    ) return false
                }
            }
            for (indice in poligonos.indices) {
                for (outroIndice in indice + 1 until poligonos.size) {
                    if (!blocosRespeitamAfastamento(
                            poligonos[indice],
                            poligonos[outroIndice],
                            afastamentoHorizontalMm,
                            afastamentoVerticalMm,
                        )
                    ) return false
                }
            }
            return true
        }

        // Mantém exatamente o desenho formado pela multiseleção. Primeiro tenta colocar a cópia
        // ao lado do original; se não couber, procura outra posição livre dentro da folha.
        val deslocamentosPreferidos = listOf(
            Offset(larguraGrupo + afastamentoHorizontalMm, 0f),
            Offset(0f, alturaGrupo + afastamentoVerticalMm),
            Offset(-(larguraGrupo + afastamentoHorizontalMm), 0f),
            Offset(0f, -(alturaGrupo + afastamentoVerticalMm)),
        )
        var copiasEncontradas = deslocamentosPreferidos
            .asSequence()
            .map { deslocamento -> criarCopias(deslocamento.x, deslocamento.y) }
            .firstOrNull { grupoDuplicadoValido(it) }

        if (copiasEncontradas == null) {
            val passoBusca = max(10f, max(afastamentoHorizontalMm, afastamentoVerticalMm))
            val limiteX = larguraMm - margemModuloMm - larguraGrupo
            val limiteY = alturaMm - margemModuloMm - alturaGrupo
            var y = margemModuloMm
            busca@ while (y <= limiteY + 0.001f) {
                var x = margemModuloMm
                while (x <= limiteX + 0.001f) {
                    val candidatas = criarCopias(x - esquerdaGrupo, y - topoGrupo)
                    if (grupoDuplicadoValido(candidatas)) {
                        copiasEncontradas = candidatas
                        break@busca
                    }
                    x += passoBusca
                }
                y += passoBusca
            }
        }

        val novas = copiasEncontradas
        if (novas != null) {
            val primeiroNovoIndice = instancias.size
            instancias = instancias + novas
            selecionados = (primeiroNovoIndice until instancias.size).toSet()
            selecionado = primeiroNovoIndice
            guiasAlinhamento = emptyList()
            mensagemPosicionamento = if (novas.size == 1) {
                "Bloco duplicado e selecionado."
            } else {
                "${novas.size} blocos duplicados e selecionados."
            }
        } else {
            mensagemPosicionamento = "Não existe espaço livre para duplicar a seleção mantendo o afastamento configurado."
        }
    }

    fun adicionarBloco() {
        if (blocos.isEmpty()) return
        val novo = encontrarPosicaoLivre(InstanciaEncaixe(blocoParaAdicionar, 0.0, 0.0))
        if (novo != null) {
            instancias = instancias + novo
            selecionado = instancias.lastIndex
            mensagemPosicionamento = null
        } else {
            mensagemPosicionamento = "Não existe espaço livre na folha para adicionar esse bloco."
        }
    }

    // Pedido do Tiago: "crie uma opção em blocos de preencher área de consumo após adicionar, e
    // segue o padrão que está disposto até preencher a área da folha com blocos". Usa o conjunto
    // de blocos JÁ posicionados como um "padrão" (a disposição relativa deles) e repete essa
    // disposição inteira, lado a lado, até não caber mais nenhuma repetição dentro da folha —
    // mantendo o mesmo afastamento já configurado entre repetições do padrão.
    fun preencherFolha() {
        if (instancias.isEmpty()) {
            mensagemPosicionamento = "Posicione ao menos um bloco antes de preencher a folha."
            return
        }
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        instancias.forEach { item ->
            val base = blocos.getOrNull(item.blocoIndice) ?: return@forEach
            pontosTransformados(item, base).forEach { p ->
                minX = min(minX, p.x)
                minY = min(minY, p.y)
                maxX = max(maxX, p.x)
                maxY = max(maxY, p.y)
            }
        }
        if (minX > maxX) return
        val padraoOriginal = instancias.toList()
        val tileLargura = (maxX - minX) + afastamentoHorizontalMm
        val tileAltura = (maxY - minY) + afastamentoVerticalMm
        if (tileLargura <= 0f || tileAltura <= 0f) return
        var adicionados = 0
        var yBase = minY
        while (yBase + (maxY - minY) <= alturaMm - margemModuloMm + 0.01f) {
            var xBase = minX
            while (xBase + (maxX - minX) <= larguraMm - margemModuloMm + 0.01f) {
                val deslocX = xBase - minX
                val deslocY = yBase - minY
                if (abs(deslocX) > 0.01f || abs(deslocY) > 0.01f) {
                    val candidatos = padraoOriginal.map { it.copy(xMm = it.xMm + deslocX, yMm = it.yMm + deslocY, travada = false) }
                    if (candidatos.all { instanciaValida(it, -1) }) {
                        instancias = instancias + candidatos
                        adicionados += candidatos.size
                    }
                }
                xBase += tileLargura
            }
            yBase += tileAltura
        }
        mensagemPosicionamento = if (adicionados > 0) {
            "Folha preenchida: +$adicionados bloco${if (adicionados == 1) "" else "s"} seguindo o padrão."
        } else {
            "Não havia espaço pra repetir o padrão dentro da folha."
        }
    }

    // "Ajustar ao conteúdo" — pedido do Tiago: "ao clicar em consumo retângulo, o retângulo
    // circule as peças dispostas na área de consumo". Calcula o retângulo delimitador (bounding
    // box) de TODAS as peças já posicionadas, desloca as peças pra encostar no afastamento mínimo
    // e redimensiona largura/altura pra abraçar exatamente esse conjunto.
    fun ajustarRetanguloAoConteudo() {
        if (instancias.isEmpty()) return
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        instancias.forEach { item ->
            val base = blocos.getOrNull(item.blocoIndice) ?: return@forEach
            pontosTransformados(item, base).forEach { p ->
                minX = min(minX, p.x)
                minY = min(minY, p.y)
                maxX = max(maxX, p.x)
                maxY = max(maxY, p.y)
            }
        }
        if (minX > maxX) return
        val deslocX = margemModuloMm - minX
        val deslocY = margemModuloMm - minY
        instancias = instancias.map { it.copy(xMm = it.xMm + deslocX, yMm = it.yMm + deslocY) }
        larguraMm = (maxX - minX) + 2 * margemModuloMm
        alturaMm = (maxY - minY) + 2 * margemModuloMm
        larguraTexto = larguraMm.roundToInt().toString()
        alturaTexto = alturaMm.roundToInt().toString()
        tamanhoFolhaSelecionado = "Personalizada"
        guiasAlinhamento = emptyList()
        mensagemPosicionamento = "Retângulo ajustado para circular as peças posicionadas."
    }

    var categoria by remember { mutableStateOf(CategoriaEncaixe.BLOCOS) }
    // Zoom/pan da visualização — pedido do Tiago: "esse retângulo pode ser editado com ajuste
    // fino e zoom, com as ferramentas já existentes" — mesmo mecanismo do editor de facas
    // (Pan/Zoom alternando o que o arraste de um dedo faz na prancheta).
    var zoomCanvas by remember { mutableStateOf(1f) }
    var panCanvas by remember { mutableStateOf(Offset.Zero) }
    var modoZoomCanvas by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Fundo,
        topBar = {
            if (!telaGirada) {
            // statusBarsPadding — pedido do Tiago: "abaixar um pouco o cabeçalho, estou com
            // dificuldades de clique". O app alvo API 36 (Android 15+), que ativa edge-to-edge
            // por padrão: sem esse padding o cabeçalho desenha por baixo da barra de status/
            // notch, e o botão de voltar fica parcialmente encoberto, difícil de acertar o toque.
            Surface(color = Color.White, shadowElevation = 2.dp, modifier = Modifier.statusBarsPadding()) {
                Row(modifier = Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Encaixe e consumo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Texto)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("A área real permanece separada", fontSize = 10.sp, color = Texto.copy(alpha = 0.58f), maxLines = 1)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "• ${"%.1f".format(consumoMm2)} mm²/bloco",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Pré-visualização — extraída numa lambda parametrizada por Modifier (pedido do
            // Tiago: "girar a tela... barra de ferramentas ficando lateral", tanto em Consumo
            // quanto em Encaixes) pra poder virar lado a lado com a barra de ferramentas em vez
            // de sempre empilhada em cima dela, sem duplicar a lógica de gestos abaixo.
            val conteudoPrancheta: @Composable (Modifier) -> Unit = { modPrancheta ->
            Box(
                modifier = modPrancheta
                    .padding(10.dp)
                    .background(Fundo, RoundedCornerShape(14.dp))
                    .border(1.dp, Borda, RoundedCornerShape(14.dp))
                    .onSizeChanged { canvasSize = it },
            ) {
                val margem = 20f
                val larguraVisualMm = larguraMm + if (tipo == TipoModuloEncaixe.PARALELOGRAMO) abs(deslocamentoMm) else 0f
                val escala = if (canvasSize.width > 0) min((canvasSize.width - 2 * margem) / larguraVisualMm.coerceAtLeast(1f), (canvasSize.height - 2 * margem) / alturaMm.coerceAtLeast(1f)) else 1f
                val larguraDesenhada = larguraVisualMm * escala
                val alturaDesenhada = alturaMm * escala
                val origem = Offset(
                    (canvasSize.width - larguraDesenhada) / 2f - min(0f, deslocamentoMm) * escala,
                    (canvasSize.height - alturaDesenhada) / 2f,
                )

                Canvas(
                    // Nao use instancias/zoomCanvas como chaves deste pointerInput: ambos mudam
                    // durante o proprio gesto e, se forem chaves, o Compose cancela e recria o
                    // detector a cada pequeno deslocamento. Mantendo apenas as configuracoes que
                    // realmente trocam o tipo/escala do gesto, o bloco acompanha o dedo sem parar.
                    modifier = Modifier.fillMaxSize().pointerInput(escala, origem, larguraMm, alturaMm, afastamentoHorizontalMm, afastamentoVerticalMm, modoZoomCanvas, categoria, modoSelecaoEncaixe) {
                        fun paraPontoMm(toqueBruto: Offset): Offset {
                            val toqueSemZoom = (toqueBruto - panCanvas) / zoomCanvas
                            return (toqueSemZoom - origem) / escala
                        }

                        fun blocoNoPonto(pontoMm: Offset): Int? = instancias.indices.reversed().firstOrNull { indice ->
                            val item = instancias[indice]
                            val base = blocos.getOrNull(item.blocoIndice) ?: return@firstOrNull false
                            val poligono = pontosTransformados(item, base)
                            val limites = limitesBloco(item, base)
                            pontoDentroDoPoligono(pontoMm, poligono) ||
                                (pontoMm.x >= limites.esquerda - 8f && pontoMm.x <= limites.direita + 8f &&
                                    pontoMm.y >= limites.topo - 8f && pontoMm.y <= limites.base + 8f)
                        }

                        when {
                            modoZoomCanvas -> {
                            detectTransformGestures { _, panChange, zoomChange, _ ->
                                val novoZoom = (zoomCanvas * zoomChange).coerceIn(1f, 6f)
                                zoomCanvas = novoZoom
                                panCanvas += panChange
                            }
                            }

                            categoria == CategoriaEncaixe.SELECAO && modoSelecaoEncaixe == ModoSelecaoEncaixe.MULTI_TOQUE -> {
                                detectTapGestures { toqueBruto ->
                                    val indice = blocoNoPonto(paraPontoMm(toqueBruto)) ?: return@detectTapGestures
                                    selecionado = indice
                                    selecionados = if (indice in selecionados) selecionados - indice else selecionados + indice
                                }
                            }

                            categoria == CategoriaEncaixe.SELECAO && modoSelecaoEncaixe == ModoSelecaoEncaixe.ARRASTO -> {
                                detectDragGestures(
                                    onDragStart = { toqueBruto ->
                                        selecaoArrastoInicio = toqueBruto
                                        selecaoArrastoAtual = toqueBruto
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        selecaoArrastoAtual = change.position
                                    },
                                    onDragEnd = {
                                        val inicio = selecaoArrastoInicio
                                        val fim = selecaoArrastoAtual
                                        if (inicio != null && fim != null) {
                                            val a = paraPontoMm(inicio)
                                            val b = paraPontoMm(fim)
                                            val esquerda = min(a.x, b.x)
                                            val direita = maxOf(a.x, b.x)
                                            val topo = min(a.y, b.y)
                                            val baseSelecao = maxOf(a.y, b.y)
                                            val encontrados = instancias.indices.filter { indice ->
                                                val item = instancias[indice]
                                                val bloco = blocos.getOrNull(item.blocoIndice) ?: return@filter false
                                                val limites = limitesBloco(item, bloco)
                                                limites.direita >= esquerda && limites.esquerda <= direita &&
                                                    limites.base >= topo && limites.topo <= baseSelecao
                                            }.toSet()
                                            selecionados = encontrados
                                            encontrados.lastOrNull()?.let { selecionado = it }
                                        }
                                        selecaoArrastoInicio = null
                                        selecaoArrastoAtual = null
                                    },
                                    onDragCancel = {
                                        selecaoArrastoInicio = null
                                        selecaoArrastoAtual = null
                                    },
                                )
                            }

                            else -> {
                            // Pedido do Tiago: "editar selecionados, podendo... mover... pra
                            // poder fazer os encaixes livremente" — se o bloco tocado faz parte
                            // da seleção múltipla, arrastar move o GRUPO inteiro junto (mantendo
                            // a disposição relativa entre eles).
                            var grupoArrasto = setOf<Int>()
                            // Pedido do Tiago: "ajuste fino para as linhas do retângulo, podendo
                            // passar por dentro de blocos" — arrastar a própria linha da folha
                            // redimensiona largura/altura direto no canvas, sem nenhuma validação
                            // contra a posição dos blocos (a linha pode cruzar por cima deles
                            // livremente; só o afastamento ENTRE blocos continua validado).
                            var arrastandoBorda: String? = null
                            detectDragGestures(
                                onDragStart = { toqueBruto ->
                                    val toque = (toqueBruto - panCanvas) / zoomCanvas
                                    val pontoMm = (toque - origem) / escala
                                    val tocouBlocoDentro = instancias.indices.reversed().firstOrNull { indice ->
                                        val item = instancias[indice]
                                        val base = blocos.getOrNull(item.blocoIndice) ?: return@firstOrNull false
                                        pontoDentroDoPoligono(pontoMm, pontosTransformados(item, base))
                                    }
                                    val toleranciaMm = 24f / escala
                                    val pertoBordaDireita = tocouBlocoDentro == null &&
                                        abs(pontoMm.x - larguraMm) <= toleranciaMm &&
                                        pontoMm.y >= -toleranciaMm && pontoMm.y <= alturaMm + toleranciaMm
                                    val pertoBordaInferior = tocouBlocoDentro == null &&
                                        abs(pontoMm.y - alturaMm) <= toleranciaMm &&
                                        pontoMm.x >= -toleranciaMm && pontoMm.x <= larguraMm + toleranciaMm
                                    if (pertoBordaDireita || pertoBordaInferior) {
                                        arrastandoBorda = if (pertoBordaDireita) "DIREITA" else "INFERIOR"
                                        grupoArrasto = emptySet()
                                        return@detectDragGestures
                                    }
                                    arrastandoBorda = null
                                    val novoSelecionado = tocouBlocoDentro ?: instancias.indices.reversed().firstOrNull { indice ->
                                        val item = instancias[indice]
                                        val base = blocos.getOrNull(item.blocoIndice) ?: return@firstOrNull false
                                        val limites = limitesBloco(item, base)
                                        pontoMm.x >= limites.esquerda - 8f && pontoMm.x <= limites.direita + 8f &&
                                            pontoMm.y >= limites.topo - 8f && pontoMm.y <= limites.base + 8f
                                    }
                                    if (novoSelecionado == null) {
                                        grupoArrasto = emptySet()
                                        return@detectDragGestures
                                    }
                                    selecionado = novoSelecionado
                                    grupoArrasto = if (novoSelecionado in selecionados && selecionados.size > 1) selecionados else setOf(novoSelecionado)
                                    if (referenciaAlinhamento == novoSelecionado) {
                                        referenciaAlinhamento = instancias.indices.firstOrNull { it != novoSelecionado } ?: novoSelecionado
                                    }
                                },
                                onDrag = { change, deltaBruto ->
                                    change.consume()
                                    val delta = deltaBruto / zoomCanvas
                                    when (arrastandoBorda) {
                                        "DIREITA" -> {
                                            larguraMm = (larguraMm + delta.x / escala).coerceIn(100f, 5000f)
                                            larguraTexto = larguraMm.roundToInt().toString()
                                            return@detectDragGestures
                                        }
                                        "INFERIOR" -> {
                                            alturaMm = (alturaMm + delta.y / escala).coerceIn(100f, 5000f)
                                            alturaTexto = alturaMm.roundToInt().toString()
                                            return@detectDragGestures
                                        }
                                    }
                                    if (grupoArrasto.isEmpty()) return@detectDragGestures
                                    if (grupoArrasto.any { instancias.getOrNull(it)?.travada == true }) return@detectDragGestures
                                    val candidatos = instancias.mapIndexed { indice, item ->
                                        if (indice in grupoArrasto) item.copy(xMm = item.xMm + delta.x / escala, yMm = item.yMm + delta.y / escala) else item
                                    }
                                    if (grupoArrasto.all { indice -> instanciaValida(candidatos[indice], grupoArrasto) }) {
                                        instancias = candidatos
                                        mensagemPosicionamento = null
                                    } else {
                                        mensagemPosicionamento = "Afastamento mínimo entre blocos atingido."
                                    }
                                },
                            )
                        }
                        }
                    },
                ) {
                    clipRect {
                        translate(left = panCanvas.x, top = panCanvas.y) {
                            scale(scale = zoomCanvas, pivot = Offset.Zero) {
                                val skew = if (tipo == TipoModuloEncaixe.PARALELOGRAMO) deslocamentoMm * escala else 0f
                                val moldura = Path().apply {
                                    moveTo(origem.x, origem.y)
                                    lineTo(origem.x + larguraMm * escala, origem.y)
                                    lineTo(origem.x + larguraMm * escala + skew, origem.y + alturaMm * escala)
                                    lineTo(origem.x + skew, origem.y + alturaMm * escala)
                                    close()
                                }
                                drawPath(moldura, color = AzulDestaque.copy(alpha = 0.06f))
                                drawPath(moldura, color = AzulDestaque, style = Stroke(width = 3f / zoomCanvas))
                                guiasAlinhamento.forEach { guia ->
                                    val inicio = if (guia.horizontal) {
                                        origem + Offset(0f, guia.valorMm * escala)
                                    } else {
                                        origem + Offset(guia.valorMm * escala, 0f)
                                    }
                                    val fim = if (guia.horizontal) {
                                        origem + Offset(larguraMm * escala, guia.valorMm * escala)
                                    } else {
                                        origem + Offset(guia.valorMm * escala, alturaMm * escala)
                                    }
                                    drawLine(
                                        color = Color(0xFF06B6D4),
                                        start = inicio,
                                        end = fim,
                                        strokeWidth = 2f / zoomCanvas,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f)),
                                    )
                                }
                                instancias.forEachIndexed { indice, item ->
                                    val base = blocos.getOrNull(item.blocoIndice) ?: return@forEachIndexed
                                    val caminho = Path().apply {
                                        pontosTransformados(item, base).forEachIndexed { pontoIndice, ponto ->
                                            val tela = origem + Offset(ponto.x * escala, ponto.y * escala)
                                            if (pontoIndice == 0) moveTo(tela.x, tela.y) else lineTo(tela.x, tela.y)
                                        }
                                        close()
                                    }
                                    val cor = CoresBlocos[item.blocoIndice % CoresBlocos.size]
                                    drawPath(caminho, color = cor.copy(alpha = 0.35f))
                                    val selecionadoVisualmente = indice in selecionados ||
                                        (categoria != CategoriaEncaixe.SELECAO && indice == selecionado)
                                    val corContorno = when {
                                        indice in indicesComConflito -> Color(0xFFDC2626)
                                        selecionadoVisualmente -> AzulDestaque
                                        else -> cor
                                    }
                                    val destacado = selecionadoVisualmente || indice in indicesComConflito
                                    drawPath(caminho, color = corContorno, style = Stroke(width = (if (destacado) 5f else 2.5f) / zoomCanvas))
                                }
                            }
                        }
                    }
                    val inicioSelecao = selecaoArrastoInicio
                    val fimSelecao = selecaoArrastoAtual
                    if (categoria == CategoriaEncaixe.SELECAO && modoSelecaoEncaixe == ModoSelecaoEncaixe.ARRASTO && inicioSelecao != null && fimSelecao != null) {
                        val esquerda = min(inicioSelecao.x, fimSelecao.x)
                        val topo = min(inicioSelecao.y, fimSelecao.y)
                        val largura = abs(fimSelecao.x - inicioSelecao.x)
                        val altura = abs(fimSelecao.y - inicioSelecao.y)
                        drawRect(AzulDestaque.copy(alpha = 0.14f), topLeft = Offset(esquerda, topo), size = Size(largura, altura))
                        drawRect(AzulDestaque, topLeft = Offset(esquerda, topo), size = Size(largura, altura), style = Stroke(width = 2.5f))
                    }
                }

            }
            }

            // Painel de ferramentas fixo, com abas sempre visíveis — pedido do Tiago: "cards
            // para ferramentas, cada uma em uma linha sempre visível na tela". Troca de aba não
            // redimensiona a prancheta acima (altura fixa aqui, igual ao editor de facas). Também
            // extraído numa lambda parametrizada por Modifier (ver conteudoPrancheta acima) pra
            // poder virar a coluna lateral de ferramentas no modo "tela girada".
            val conteudoFerramentasEncaixe: @Composable (Modifier) -> Unit = { modFerramentas ->
            Column(modifier = modFerramentas) {
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Borda),
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Duas linhas fixas de categorias: não há rolagem horizontal nem títulos
                    // cortados nas laterais.
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        maxItemsInEachRow = 3,
                    ) {
                        CategoriaEncaixe.entries.forEach { cat ->
                            val titulo = when (cat) {
                                CategoriaEncaixe.BLOCOS -> "Blocos"
                                CategoriaEncaixe.SELECAO -> "Seleção"
                                CategoriaEncaixe.TRANSFORMAR -> "Transformar"
                                CategoriaEncaixe.VISUALIZACAO -> "Visualização"
                                CategoriaEncaixe.RESULTADO -> "Resultado"
                                CategoriaEncaixe.CONFIGURACOES -> "Config."
                            }
                            val icone = if (cat == CategoriaEncaixe.CONFIGURACOES) Icons.Filled.Settings else null
                            Row(
                                modifier = Modifier.weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (categoria == cat) AzulDestaque else Fundo)
                                    .clickable {
                                        categoria = cat
                                        // Ao abrir Transformar, o dedo volta a atuar sobre os
                                        // blocos (e nao sobre a prancheta), como indica Mover.
                                        if (cat == CategoriaEncaixe.TRANSFORMAR || cat == CategoriaEncaixe.SELECAO) modoZoomCanvas = false
                                        if (cat != CategoriaEncaixe.SELECAO) {
                                            selecaoArrastoInicio = null
                                            selecaoArrastoAtual = null
                                        }
                                    }
                                    .padding(horizontal = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                if (icone != null) {
                                    Icon(icone, contentDescription = null, tint = if (categoria == cat) Color.White else Texto, modifier = Modifier.size(14.dp))
                                }
                                Text(titulo, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (categoria == cat) Color.White else Texto, maxLines = 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        when (categoria) {
                            CategoriaEncaixe.BLOCOS -> {
                                Text("Arraste os blocos para montar a repetição. Peças podem cruzar a borda quando o módulo for repetido.", fontSize = 10.sp, color = Texto.copy(alpha = 0.7f))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    maxItemsInEachRow = 3,
                                ) {
                                    blocos.indices.forEach { indice ->
                                        CartaoFerramenta("Bloco ${indice + 1}", blocoParaAdicionar == indice, Modifier) { blocoParaAdicionar = indice }
                                    }
                                    CartaoFerramenta("Adicionar", false, icone = Icons.Filled.Add) { adicionarBloco() }
                                }
                                CartaoFerramenta(
                                    if (selecionados.size > 1) "Duplicar ${selecionados.size} blocos selecionados" else "Duplicar bloco selecionado",
                                    false,
                                    Modifier.fillMaxWidth(),
                                    icone = Icons.Filled.ContentCopy,
                                ) { duplicarSelecionados() }
                                // Pedido do Tiago: "crie uma opção em blocos de preencher área de
                                // consumo após adicionar, e segue o padrão que está disposto até
                                // preencher a área da folha com blocos".
                                CartaoFerramenta("Preencher folha com este padrão", false, Modifier.fillMaxWidth(), icone = Icons.Filled.OpenInFull) { preencherFolha() }
                            }
                            // Pedido do Tiago: "crie em transformar/blocos, selecionar blocos,
                            // crie um popup ao clicar para selecionar o bloco desejado... para
                            // poder fazer os encaixes livremente" + "visualização e seleção" —
                            // aba própria, espelhando a mesma categoria do editor de facas.
                            CategoriaEncaixe.SELECAO -> {
                                Text(
                                    if (modoSelecaoEncaixe == ModoSelecaoEncaixe.MULTI_TOQUE) {
                                        "Toque nos blocos para adicionar ou remover da multiseleção."
                                    } else {
                                        "Arraste um retângulo na área acima para selecionar vários blocos."
                                    },
                                    fontSize = 10.sp,
                                    color = Texto.copy(alpha = 0.7f),
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CartaoFerramenta(
                                        "Multiseleção",
                                        modoSelecaoEncaixe == ModoSelecaoEncaixe.MULTI_TOQUE,
                                        Modifier.weight(1f),
                                        icone = Icons.Filled.TouchApp,
                                    ) {
                                        modoSelecaoEncaixe = ModoSelecaoEncaixe.MULTI_TOQUE
                                        modoZoomCanvas = false
                                        selecaoArrastoInicio = null
                                        selecaoArrastoAtual = null
                                    }
                                    CartaoFerramenta(
                                        "Selecionar por arrasto",
                                        modoSelecaoEncaixe == ModoSelecaoEncaixe.ARRASTO,
                                        Modifier.weight(1f),
                                        icone = Icons.Filled.CropFree,
                                    ) {
                                        modoSelecaoEncaixe = ModoSelecaoEncaixe.ARRASTO
                                        modoZoomCanvas = false
                                    }
                                }
                                CartaoFerramenta(
                                    if (selecionados.isNotEmpty()) "${selecionados.size} selecionado${if (selecionados.size == 1) "" else "s"} — abrir lista" else "Selecionar pela lista",
                                    selecionados.isNotEmpty(),
                                    Modifier.fillMaxWidth(),
                                    icone = Icons.Filled.CheckBox,
                                ) { mostrarPopupSelecionarBlocos = true }
                                if (selecionados.isNotEmpty()) {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        maxItemsInEachRow = 3,
                                    ) {
                                        selecionados.sorted().forEach { indice ->
                                            CartaoFerramenta("Bloco ${indice + 1}", true, icone = Icons.Filled.CheckBox) { selecionados = selecionados - indice }
                                        }
                                    }
                                    CartaoFerramenta("Limpar seleção", false, Modifier.fillMaxWidth(), icone = Icons.Filled.CheckBoxOutlineBlank) { selecionados = emptySet() }
                                }
                            }
                            CategoriaEncaixe.VISUALIZACAO -> {
                                Text("Mova a prancheta livremente ou amplie pra ajustar com mais precisão — pedido do Tiago: ferramentas de visualização iguais às do editor de facas.", fontSize = 10.sp, color = Texto.copy(alpha = 0.7f))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CartaoFerramenta("Mover peça", !modoZoomCanvas, Modifier.weight(1f), icone = Icons.Filled.OpenWith) { modoZoomCanvas = false }
                                    CartaoFerramenta("Pan / Zoom", modoZoomCanvas, Modifier.weight(1f), icone = Icons.Filled.PanTool) { modoZoomCanvas = true }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CartaoFerramenta("Diminuir", false, Modifier.weight(1f), icone = Icons.Filled.ZoomOut) { zoomCanvas = (zoomCanvas - 0.5f).coerceAtLeast(1f) }
                                    Text("${(zoomCanvas * 100).roundToInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Texto)
                                    CartaoFerramenta("Aumentar", false, Modifier.weight(1f), icone = Icons.Filled.ZoomIn) { zoomCanvas = (zoomCanvas + 0.5f).coerceAtMost(6f) }
                                }
                                CartaoFerramenta("Resetar zoom e posição", false, Modifier.fillMaxWidth(), icone = Icons.Filled.RestartAlt) { zoomCanvas = 1f; panCanvas = Offset.Zero }
                            }
                            CategoriaEncaixe.TRANSFORMAR -> {
                                if (selecionados.size > 1) {
                                    // Edição em GRUPO — ver moverGrupo/rotacionarGrupo acima.
                                    Text("${selecionados.size} BLOCOS SELECIONADOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Texto.copy(alpha = 0.55f))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        CartaoFerramenta("Mover/arrastar", modoAjusteFinoGrupo == "MOVER", Modifier.weight(1f), icone = Icons.Filled.OpenWith) {
                                            modoAjusteFinoGrupo = "MOVER"
                                            modoZoomCanvas = false
                                        }
                                        CartaoFerramenta("Rotacionar", modoAjusteFinoGrupo == "ROTACIONAR", Modifier.weight(1f), icone = Icons.Filled.RotateRight) { modoAjusteFinoGrupo = "ROTACIONAR" }
                                    }
                                    OpcoesTransformacaoRapida(
                                        onEspelhar = { espelharSelecionados() },
                                        onInverter = { inverterSelecionados() },
                                        onDefinirAngulo = { definirRotacaoSelecionados(it) },
                                    )
                                    if (modoAjusteFinoGrupo == "MOVER") {
                                        Text("Arraste qualquer bloco selecionado diretamente na area acima. Use as setas para o ajuste fino.", fontSize = 9.sp, color = Texto.copy(alpha = 0.68f))
                                    }
                                    SeletorPassoTransformacao(
                                        moverAtivo = modoAjusteFinoGrupo == "MOVER",
                                        passoMovimentoMm = passoMovimentoMm,
                                        passoRotacaoGraus = passoRotacaoGraus,
                                        onPassoMovimentoChange = { passoMovimentoMm = it },
                                        onPassoRotacaoChange = { passoRotacaoGraus = it },
                                    )
                                    ControleAjusteFinoBloco(
                                        moverAtivo = modoAjusteFinoGrupo == "MOVER",
                                        onMover = { dx, dy -> moverGrupo(dx * passoMovimentoMm, dy * passoMovimentoMm) },
                                        onRotacionar = { direcao -> rotacionarGrupo((direcao * passoRotacaoGraus).toDouble()) },
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        CartaoFerramenta("Resetar posições", false, Modifier.weight(1f), icone = Icons.Filled.RestartAlt) { resetarPosicoesGrupo() }
                                        CartaoFerramenta("Duplicar selecionados", false, Modifier.weight(1f), icone = Icons.Filled.ContentCopy) { duplicarSelecionados() }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        CartaoFerramenta("Excluir selecionados", false, Modifier.weight(1f), icone = Icons.Filled.Delete, cor = Color(0xFFDC2626)) {
                                            instancias = instancias.filterIndexed { indice, _ -> indice !in selecionados }
                                            selecionados = emptySet()
                                            selecionado = instancias.lastIndex.coerceAtLeast(0)
                                        }
                                        CartaoFerramenta("Limpar seleção", false, Modifier.weight(1f), icone = Icons.Filled.CheckBoxOutlineBlank) { selecionados = emptySet() }
                                    }
                                } else if (selecionado in instancias.indices) {
                                    val atual = instancias[selecionado]
                                    Text("BLOCO ${selecionado + 1} SELECIONADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Texto.copy(alpha = 0.55f))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        CartaoFerramenta("Mover/arrastar", modoAjusteFinoGrupo == "MOVER", Modifier.weight(1f), icone = Icons.Filled.OpenWith) {
                                            modoAjusteFinoGrupo = "MOVER"
                                            modoZoomCanvas = false
                                        }
                                        CartaoFerramenta("Rotacionar", modoAjusteFinoGrupo == "ROTACIONAR", Modifier.weight(1f), icone = Icons.Filled.RotateRight) { modoAjusteFinoGrupo = "ROTACIONAR" }
                                    }
                                    OpcoesTransformacaoRapida(
                                        onEspelhar = { espelharSelecionados() },
                                        onInverter = { inverterSelecionados() },
                                        onDefinirAngulo = { definirRotacaoSelecionados(it) },
                                    )
                                    if (modoAjusteFinoGrupo == "MOVER") {
                                        Text("Arraste o bloco diretamente na area acima. Use as setas para o ajuste fino.", fontSize = 9.sp, color = Texto.copy(alpha = 0.68f))
                                    }
                                    SeletorPassoTransformacao(
                                        moverAtivo = modoAjusteFinoGrupo == "MOVER",
                                        passoMovimentoMm = passoMovimentoMm,
                                        passoRotacaoGraus = passoRotacaoGraus,
                                        onPassoMovimentoChange = { passoMovimentoMm = it },
                                        onPassoRotacaoChange = { passoRotacaoGraus = it },
                                    )
                                    ControleAjusteFinoBloco(
                                        moverAtivo = modoAjusteFinoGrupo == "MOVER",
                                        anguloAtual = atual.rotacaoGraus,
                                        onMover = { dx, dy -> moverSelecionado(dx * passoMovimentoMm, dy * passoMovimentoMm) },
                                        onRotacionar = { direcao -> rotacionarSelecionado((direcao * passoRotacaoGraus).toDouble()) },
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        CartaoFerramenta(
                                            if (atual.travada) "Travado" else "Travar",
                                            atual.travada,
                                            Modifier.weight(1f),
                                            icone = if (atual.travada) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                        ) { instancias = instancias.mapIndexed { indice, item -> if (indice == selecionado) item.copy(travada = !item.travada) else item } }
                                        CartaoFerramenta("Duplicar", false, Modifier.weight(1f), icone = Icons.Filled.ContentCopy) { duplicarSelecionados() }
                                        CartaoFerramenta("Excluir", false, Modifier.weight(1f), icone = Icons.Filled.Delete, cor = Color(0xFFDC2626)) {
                                            if (instancias.size > 1) {
                                                instancias = instancias.filterIndexed { indice, _ -> indice != selecionado }
                                                selecionado = selecionado.coerceAtMost(instancias.lastIndex)
                                            }
                                        }
                                    }
                                    if (instancias.size > 1) {
                                        Text("ALINHAMENTO POR REFERÊNCIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Texto.copy(alpha = 0.55f))
                                        Text("Bloco ${selecionado + 1} será alinhado com:", fontSize = 10.sp, color = Texto)
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            maxItemsInEachRow = 4,
                                        ) {
                                            instancias.indices.filter { it != selecionado }.forEach { indice ->
                                                CartaoFerramenta("Bloco ${indice + 1}", referenciaAlinhamento == indice, Modifier) { referenciaAlinhamento = indice }
                                            }
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            CartaoFerramenta("Esquerda", false, Modifier.weight(1f), icone = Icons.Filled.AlignHorizontalLeft) { alinharSelecionado("ESQUERDA") }
                                            CartaoFerramenta("Centro X", false, Modifier.weight(1f), icone = Icons.Filled.AlignHorizontalCenter) { alinharSelecionado("CENTRO_X") }
                                            CartaoFerramenta("Direita", false, Modifier.weight(1f), icone = Icons.Filled.AlignHorizontalRight) { alinharSelecionado("DIREITA") }
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            CartaoFerramenta("Topo", false, Modifier.weight(1f), icone = Icons.Filled.AlignVerticalTop) { alinharSelecionado("TOPO") }
                                            CartaoFerramenta("Centro Y", false, Modifier.weight(1f), icone = Icons.Filled.AlignVerticalCenter) { alinharSelecionado("CENTRO_Y") }
                                            CartaoFerramenta("Base", false, Modifier.weight(1f), icone = Icons.Filled.AlignVerticalBottom) { alinharSelecionado("BASE") }
                                        }
                                        CartaoFerramenta("Limpar linhas de alinhamento", false, Modifier.fillMaxWidth(), icone = Icons.Filled.Remove) { guiasAlinhamento = emptyList() }
                                    }
                                } else {
                                    Text("Toque numa peça na prancheta pra selecionar.", fontSize = 11.sp, color = Texto.copy(alpha = 0.6f))
                                }
                            }
                            CategoriaEncaixe.CONFIGURACOES -> {
                                CartaoFerramenta(
                                    if (telaGirada) "Voltar ao retrato" else "Girar tela",
                                    telaGirada,
                                    Modifier.fillMaxWidth(),
                                    icone = Icons.Filled.ScreenRotation,
                                ) { telaGirada = !telaGirada }
                                CabecalhoSecaoEncaixe(Icons.Filled.ViewModule, "Tipo de módulo", "Retângulo ou paralelogramo")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CartaoFerramenta("Retângulo", tipo == TipoModuloEncaixe.RETANGULO, Modifier.weight(1f), icone = Icons.Filled.CropSquare) { tipo = TipoModuloEncaixe.RETANGULO }
                                    CartaoFerramenta("Paralelogramo", tipo == TipoModuloEncaixe.PARALELOGRAMO, Modifier.weight(1f), icone = Icons.Filled.CropSquare) { tipo = TipoModuloEncaixe.PARALELOGRAMO }
                                }
                                CabecalhoSecaoEncaixe(
                                    Icons.Filled.Straighten,
                                    if (tipo == TipoModuloEncaixe.RETANGULO) "Dimensões do retângulo" else "Dimensões do paralelogramo",
                                    "Largura, altura e tamanho da folha",
                                )
                                LinhaDimensao("Largura", larguraTexto, onAjusteFino = { delta ->
                                    val novo = (larguraMm + delta).coerceIn(100f, 5000f)
                                    larguraMm = novo
                                    larguraTexto = novo.roundToInt().toString()
                                    tamanhoFolhaSelecionado = "Personalizada"
                                }) { texto ->
                                    larguraTexto = texto
                                    texto.replace(',', '.').toFloatOrNull()?.coerceIn(100f, 5000f)?.let { larguraMm = it }
                                    tamanhoFolhaSelecionado = "Personalizada"
                                }
                                LinhaDimensao("Altura", alturaTexto, onAjusteFino = { delta ->
                                    val novo = (alturaMm + delta).coerceIn(100f, 5000f)
                                    alturaMm = novo
                                    alturaTexto = novo.roundToInt().toString()
                                    tamanhoFolhaSelecionado = "Personalizada"
                                }) { texto ->
                                    alturaTexto = texto
                                    texto.replace(',', '.').toFloatOrNull()?.coerceIn(100f, 5000f)?.let { alturaMm = it }
                                    tamanhoFolhaSelecionado = "Personalizada"
                                }
                                if (tipo == TipoModuloEncaixe.PARALELOGRAMO) {
                                    LinhaDimensao("Deslocamento", deslocamentoTexto, onAjusteFino = { delta ->
                                        val novo = (deslocamentoMm + delta).coerceIn(-500f, 500f)
                                        deslocamentoMm = novo
                                        deslocamentoTexto = novo.roundToInt().toString()
                                    }) { texto ->
                                        deslocamentoTexto = texto
                                        texto.replace(',', '.').toFloatOrNull()?.coerceIn(-500f, 500f)?.let { deslocamentoMm = it }
                                    }
                                }
                                if (tipo == TipoModuloEncaixe.RETANGULO) {
                                    CartaoFerramenta("Ajustar retângulo às peças posicionadas", false, Modifier.fillMaxWidth(), icone = Icons.Filled.OpenInFull) { ajustarRetanguloAoConteudo() }
                                }
                                CabecalhoSecaoEncaixe(Icons.Filled.CropSquare, "Tamanho da folha", "Dimensões prontas ou personalizada")
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    maxItemsInEachRow = 3,
                                ) {
                                    TamanhosFolha.forEach { tamanho ->
                                        CartaoFerramenta(tamanho.nome, tamanhoFolhaSelecionado == tamanho.nome, Modifier) { aplicarTamanhoFolha(tamanho) }
                                    }
                                    CartaoFerramenta("Personalizada", tamanhoFolhaSelecionado == "Personalizada", Modifier) { tamanhoFolhaSelecionado = "Personalizada" }
                                }
                                // Pedido do Tiago: "o afastamento mínimo entre blocos deve ter
                                // valor para as laterais e acima e abaixo" — dois sliders em vez
                                // de um só, cada um no eixo que o nome já diz.
                                CabecalhoSecaoEncaixe(Icons.Filled.OpenInFull, "Afastamentos", "Entre blocos e da borda da folha")
                                Text("LATERAIS (ESQUERDA/DIREITA)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Texto.copy(alpha = 0.55f))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Slider(
                                        value = afastamentoHorizontalMm,
                                        onValueChange = { afastamentoHorizontalMm = it.roundToInt().toFloat(); guiasAlinhamento = emptyList() },
                                        valueRange = 0f..50f,
                                        steps = 49,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text("${afastamentoHorizontalMm.roundToInt()} mm", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Texto)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(2f, 5f, 10f, 20f).forEach { valor ->
                                        CartaoFerramenta("${valor.roundToInt()} mm", afastamentoHorizontalMm == valor, Modifier) { afastamentoHorizontalMm = valor; guiasAlinhamento = emptyList() }
                                    }
                                }
                                Text("ACIMA/ABAIXO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Texto.copy(alpha = 0.55f))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Slider(
                                        value = afastamentoVerticalMm,
                                        onValueChange = { afastamentoVerticalMm = it.roundToInt().toFloat(); guiasAlinhamento = emptyList() },
                                        valueRange = 0f..50f,
                                        steps = 49,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text("${afastamentoVerticalMm.roundToInt()} mm", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Texto)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(2f, 5f, 10f, 20f).forEach { valor ->
                                        CartaoFerramenta("${valor.roundToInt()} mm", afastamentoVerticalMm == valor, Modifier) { afastamentoVerticalMm = valor; guiasAlinhamento = emptyList() }
                                    }
                                }
                                // Pedido do Tiago: "distância mínima da linha do retângulo ou
                                // paralelogramo aos blocos" — margem entre a borda do módulo e
                                // qualquer peça encostada nela (diferente do afastamento ENTRE
                                // blocos acima).
                                Text("DISTÂNCIA MÍNIMA DA BORDA DA FOLHA AOS BLOCOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Texto.copy(alpha = 0.55f))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Slider(
                                        value = margemModuloMm,
                                        onValueChange = { margemModuloMm = it.roundToInt().toFloat() },
                                        valueRange = 0f..50f,
                                        steps = 49,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text("${margemModuloMm.roundToInt()} mm", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Texto)
                                }
                                if (indicesComConflito.isNotEmpty()) {
                                    Text("Existem blocos fora da folha ou mais próximos que o permitido. Eles estão marcados em vermelho.", fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                }
                            }
                            CategoriaEncaixe.RESULTADO -> {
                                Text("Área real dos blocos: ${"%.1f".format(areaRealMm2)} mm²", color = Texto, fontSize = 12.sp)
                                Text("Folha: ${larguraMm.roundToInt()} × ${alturaMm.roundToInt()} mm", color = Texto, fontSize = 12.sp)
                                Text("Área da folha: ${"%.1f".format(areaModuloMm2)} mm²", color = Texto, fontSize = 12.sp)
                                Text("Afastamento mínimo — laterais: ${afastamentoHorizontalMm.roundToInt()} mm, acima/abaixo: ${afastamentoVerticalMm.roundToInt()} mm", color = Texto, fontSize = 12.sp)
                                Text("${instancias.size} blocos por folha", color = Texto, fontSize = 12.sp)
                                Text("Consumo: ${"%.1f".format(consumoMm2)} mm²/bloco (${"%.6f".format(consumoMm2 / 1_000_000.0)} m²)", color = Color(0xFF047857), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            mensagemPosicionamento?.let { mensagem ->
                Text(
                    mensagem,
                    fontSize = 10.sp,
                    color = if (mensagem.startsWith("Bloco alinhado") || mensagem.startsWith("Retângulo ajustado") || mensagem.startsWith("Folha preenchida") || mensagem.contains("duplicad")) Color(0xFF047857) else Color(0xFFB91C1C),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                )
            }
            erro?.let { Text(it, color = Color(0xFFB91C1C), modifier = Modifier.padding(horizontal = 12.dp)) }
            Button(
                onClick = {
                    onConfirmado(
                        EncaixeConsumo(
                            tipo = tipo,
                            larguraMm = larguraMm.toDouble(),
                            alturaMm = alturaMm.toDouble(),
                            deslocamentoMm = if (tipo == TipoModuloEncaixe.PARALELOGRAMO) deslocamentoMm.toDouble() else 0.0,
                            afastamentoHorizontalMm = afastamentoHorizontalMm.toDouble(),
                            afastamentoVerticalMm = afastamentoVerticalMm.toDouble(),
                            instancias = instancias,
                        ),
                    )
                },
                enabled = !salvando && instancias.isNotEmpty() && indicesComConflito.isEmpty(),
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AzulDestaque),
            ) { Text(if (salvando) "Salvando..." else "✓ Salvar encaixe e consumo", fontWeight = FontWeight.Bold) }
            }
            }

            if (telaGirada) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Surface(color = Color.White, shadowElevation = 2.dp) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Encaixe e consumo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Texto)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("A área real permanece separada", fontSize = 10.sp, color = Texto.copy(alpha = 0.58f), maxLines = 1)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "• ${"%.1f".format(consumoMm2)} mm²/bloco",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF047857),
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                        conteudoPrancheta(Modifier.weight(1f).fillMaxWidth())
                    }
                    conteudoFerramentasEncaixe(Modifier.width(300.dp).fillMaxHeight())
                }
            } else {
                conteudoPrancheta(Modifier.height(280.dp).fillMaxWidth())
                conteudoFerramentasEncaixe(Modifier.weight(1f).fillMaxWidth())
            }
        }
    }

    // Pedido do Tiago: "crie um popup ao clicar para selecionar o bloco desejado" — seleção
    // múltipla por checkbox, em vez da grade de referência de alinhamento tomando conta da aba
    // Transformar o tempo todo.
    if (mostrarPopupSelecionarBlocos) {
        AlertDialog(
            onDismissRequest = { mostrarPopupSelecionarBlocos = false },
            title = { Text("Selecionar blocos") },
            text = {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                        CartaoFerramenta("Selecionar todos", false, Modifier.weight(1f)) { selecionados = instancias.indices.toSet() }
                        CartaoFerramenta("Limpar", false, Modifier.weight(1f)) { selecionados = emptySet() }
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        maxItemsInEachRow = 3,
                    ) {
                        instancias.indices.forEach { indice ->
                            CartaoFerramenta(
                                "Bloco ${indice + 1}",
                                indice in selecionados,
                                icone = if (indice in selecionados) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                            ) {
                                selecionados = if (indice in selecionados) selecionados - indice else selecionados + indice
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarPopupSelecionarBlocos = false
                    if (selecionados.size == 1) selecionado = selecionados.first()
                }) { Text("OK") }
            },
        )
    }
}

// Pedido do Tiago: "use esse print dessa tela para copiar os estilos de tela" (Expedição Bling)
// — cabeçalho de seção com círculo de ícone + título em negrito + subtítulo cinza, igual aos
// cards "Criar lista de separação"/"Vincular Produtos" daquela tela.
@Composable
private fun CabecalhoSecaoEncaixe(icone: ImageVector, titulo: String, subtitulo: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(AzulDestaque.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = AzulDestaque, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Texto)
            Text(subtitulo, fontSize = 10.sp, color = Texto.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun OpcoesTransformacaoRapida(
    onEspelhar: () -> Unit,
    onInverter: () -> Unit,
    onDefinirAngulo: (Double) -> Unit,
) {
    Text(
        "TRANSFORMACOES RAPIDAS",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = Texto.copy(alpha = 0.55f),
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CartaoFerramenta("Espelhar", false, Modifier.weight(1f), icone = Icons.Filled.Flip, onClick = onEspelhar)
        CartaoFerramenta("Inverter", false, Modifier.weight(1f), icone = Icons.Filled.SwapVert, onClick = onInverter)
    }
    Text(
        "GIRAR PARA",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = Texto.copy(alpha = 0.55f),
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        listOf(45.0, 90.0, 180.0, 270.0).forEach { angulo ->
            CartaoFerramenta(
                "${angulo.roundToInt()}°",
                false,
                Modifier.weight(1f),
                icone = Icons.Filled.RotateRight,
            ) { onDefinirAngulo(angulo) }
        }
    }
}

/** Define quanto cada toque do ajuste fino move ou rotaciona o bloco. */
@Composable
private fun SeletorPassoTransformacao(
    moverAtivo: Boolean,
    passoMovimentoMm: Float,
    passoRotacaoGraus: Float,
    onPassoMovimentoChange: (Float) -> Unit,
    onPassoRotacaoChange: (Float) -> Unit,
) {
    val valor = if (moverAtivo) passoMovimentoMm else passoRotacaoGraus
    val incremento = if (moverAtivo) 0.1f else 1f
    val minimo = if (moverAtivo) 0.1f else 1f
    val maximo = if (moverAtivo) 100f else 90f
    val rotuloValor = if (moverAtivo) {
        "${"%.1f".format(valor).replace('.', ',')} mm"
    } else {
        "${valor.roundToInt()}°"
    }
    val atualizar: (Float) -> Unit = { novo ->
        val ajustado = novo.coerceIn(minimo, maximo)
        if (moverAtivo) onPassoMovimentoChange(ajustado) else onPassoRotacaoChange(ajustado)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            if (moverAtivo) "Distância por toque" else "Graus por toque",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Texto.copy(alpha = 0.68f),
            modifier = Modifier.width(108.dp),
        )
        IconButton(
            onClick = { atualizar(valor - incremento) },
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White).border(1.dp, Borda, CircleShape),
        ) { Icon(Icons.Filled.Remove, contentDescription = "Diminuir passo", tint = Texto, modifier = Modifier.size(16.dp)) }
        Surface(
            modifier = Modifier.padding(horizontal = 6.dp).width(78.dp).height(36.dp),
            shape = RoundedCornerShape(12.dp),
            color = Fundo,
            border = androidx.compose.foundation.BorderStroke(1.dp, Borda),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(rotuloValor, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Texto)
            }
        }
        IconButton(
            onClick = { atualizar(valor + incremento) },
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White).border(1.dp, Borda, CircleShape),
        ) { Icon(Icons.Filled.Add, contentDescription = "Aumentar passo", tint = Texto, modifier = Modifier.size(16.dp)) }
    }
}

/** Mesmo controle compacto de ajuste fino usado no editor de facas. */
@Composable
private fun ControleAjusteFinoBloco(
    moverAtivo: Boolean,
    anguloAtual: Double? = null,
    onMover: (Float, Float) -> Unit,
    onRotacionar: (Float) -> Unit,
) {
    if (moverAtivo) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BotaoAjusteFino("↖") { onMover(-1f, -1f) }
                BotaoAjusteFino("↑") { onMover(0f, -1f) }
                BotaoAjusteFino("↗") { onMover(1f, -1f) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BotaoAjusteFino("←") { onMover(-1f, 0f) }
                BotaoAjusteFino("•", habilitado = false) {}
                BotaoAjusteFino("→") { onMover(1f, 0f) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BotaoAjusteFino("↙") { onMover(-1f, 1f) }
                BotaoAjusteFino("↓") { onMover(0f, 1f) }
                BotaoAjusteFino("↘") { onMover(1f, 1f) }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BotaoAjusteFino(icone = Icons.Filled.RotateLeft, descricao = "Rotacionar à esquerda") { onRotacionar(-1f) }
            Surface(
                modifier = Modifier.padding(horizontal = 8.dp).width(72.dp).height(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = Fundo,
                border = androidx.compose.foundation.BorderStroke(1.dp, Borda),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(anguloAtual?.let { "${it.roundToInt()}°" } ?: "Grupo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Texto)
                }
            }
            BotaoAjusteFino(icone = Icons.Filled.RotateRight, descricao = "Rotacionar à direita") { onRotacionar(1f) }
        }
    }
}

@Composable
private fun BotaoAjusteFino(
    simbolo: String? = null,
    icone: ImageVector? = null,
    descricao: String = simbolo.orEmpty(),
    habilitado: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (habilitado) Color.White else Fundo)
            .border(1.dp, Borda, RoundedCornerShape(10.dp))
            .clickable(enabled = habilitado, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (icone != null) {
            Icon(icone, contentDescription = descricao, tint = if (habilitado) AzulDestaque else Texto.copy(alpha = 0.25f), modifier = Modifier.size(20.dp))
        } else {
            Text(simbolo.orEmpty(), fontSize = 17.sp, color = if (habilitado) Texto else Texto.copy(alpha = 0.25f))
        }
    }
}

// Pedido do Tiago: "ajuste todas as ferramentas para ficarem no estilo de criador de etiquetas...
// não quero essas cápsulas arredondadas, quero todas no estilo do print ferramentas de edição" —
// mesmo visual do popup "Ferramentas de Edição" do editor de etiquetas (LabelEditorView.tsx):
// cantos quadrados-arredondados (não cápsula/pill), fundo cinza-claro neutro, ícone (quando tem)
// em cima + rótulo embaixo, compacto pra mobile.
@Composable
private fun CartaoFerramenta(
    titulo: String,
    ativo: Boolean,
    modifier: Modifier = Modifier,
    icone: ImageVector? = null,
    cor: Color = AzulDestaque,
    habilitado: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (ativo) cor.copy(alpha = 0.16f) else Fundo)
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (icone != null) {
            Icon(
                icone,
                contentDescription = null,
                tint = if (!habilitado) Texto.copy(alpha = 0.3f) else if (ativo) cor else cor.copy(alpha = 0.85f),
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            titulo,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (!habilitado) Texto.copy(alpha = 0.3f) else if (ativo) cor else Color(0xFF1E3A8A),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/** Uma linha da "calculadora" de dimensões — pedido do Tiago: "opção de editar valores cada um
 * em sua linha dentro de um card, uma calculadora de inserção representando os lados". Rótulo do
 * lado à esquerda, campo de texto grande o bastante pra digitar com o dedo à direita, e (pedido
 * nesta leva: "ajuste fino") botões −1/+1mm pra quem prefere afinar sem digitar. */
@Composable
private fun LinhaDimensao(titulo: String, valorTexto: String, onAjusteFino: (Float) -> Unit = {}, onValorChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Fundo)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(titulo, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Texto, modifier = Modifier.weight(1f))
        IconButton(onClick = { onAjusteFino(-1f) }, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = "Diminuir 1mm", modifier = Modifier.size(14.dp))
        }
        OutlinedTextField(
            value = valorTexto,
            onValueChange = { texto -> onValorChange(texto.filter { it.isDigit() || it == ',' || it == '.' || it == '-' }) },
            trailingIcon = { Text("mm", fontSize = 9.sp, color = Texto.copy(alpha = 0.6f), modifier = Modifier.padding(end = 6.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Texto),
            modifier = Modifier.width(108.dp).height(44.dp),
        )
        IconButton(onClick = { onAjusteFino(1f) }, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar 1mm", modifier = Modifier.size(14.dp))
        }
    }
}

// Cópia local do helper já usado em ValidacaoContornoScreen.kt (é privado lá, sem compartilhar
// entre arquivos) — sobe pela cadeia de ContextWrapper até achar a Activity de verdade, já que
// LocalContext.current às vezes vem envolvido num ContextWrapper.
private tailrec fun android.content.Context.paraActivityEncaixe(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.paraActivityEncaixe()
    else -> null
}
