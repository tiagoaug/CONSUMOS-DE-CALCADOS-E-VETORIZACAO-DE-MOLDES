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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
private val LocalFerramentasCompactas = staticCompositionLocalOf { false }

private data class BlocoBase(val pontos: List<Offset>, val largura: Float, val altura: Float)
private data class LimitesBloco(val esquerda: Float, val topo: Float, val direita: Float, val base: Float) {
    val centroX get() = (esquerda + direita) / 2f
    val centroY get() = (topo + base) / 2f
}
private data class GuiaAlinhamento(val horizontal: Boolean, val valorMm: Float)
private data class TamanhoFolha(val nome: String, val larguraMm: Float, val alturaMm: Float)
private data class PoligonoPosicionado(val pontos: List<Offset>, val limites: LimitesBloco)
private data class ResultadoPreenchimento(
    val instancias: List<InstanciaEncaixe>,
    val adicionados: Int,
    val limiteAtingido: Boolean,
)
private data class ResultadoAjusteRetangulo(
    val instancias: List<InstanciaEncaixe>,
    val larguraMm: Float,
    val alturaMm: Float,
    val afastamentosCorrigidos: Boolean,
)

private const val LIMITE_INSTANCIAS_PREENCHIMENTO = 600
private const val LIMITE_TENTATIVAS_PREENCHIMENTO = 20_000
private const val LIMITE_TENTATIVAS_BUSCA = 10_000
private const val FOLGA_AJUSTE_RETANGULO_MM = 0.05f

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

private fun tituloCategoriaEncaixe(categoria: CategoriaEncaixe) = when (categoria) {
    CategoriaEncaixe.BLOCOS -> "Blocos"
    CategoriaEncaixe.SELECAO -> "Seleção"
    CategoriaEncaixe.TRANSFORMAR -> "Transformar"
    CategoriaEncaixe.VISUALIZACAO -> "Visualização"
    CategoriaEncaixe.RESULTADO -> "Resultado"
    CategoriaEncaixe.CONFIGURACOES -> "Configurações"
}

private fun iconeCategoriaEncaixe(categoria: CategoriaEncaixe) = when (categoria) {
    CategoriaEncaixe.BLOCOS -> Icons.Filled.ViewModule
    CategoriaEncaixe.SELECAO -> Icons.Filled.CropFree
    CategoriaEncaixe.TRANSFORMAR -> Icons.Filled.OpenWith
    CategoriaEncaixe.VISUALIZACAO -> Icons.Filled.PanTool
    CategoriaEncaixe.RESULTADO -> Icons.Filled.CheckBox
    CategoriaEncaixe.CONFIGURACOES -> Icons.Filled.Settings
}

private fun corCategoriaEncaixe(categoria: CategoriaEncaixe) = when (categoria) {
    CategoriaEncaixe.BLOCOS -> Color(0xFF16A34A)
    CategoriaEncaixe.SELECAO -> Color(0xFF2563EB)
    CategoriaEncaixe.TRANSFORMAR -> Color(0xFFF97316)
    CategoriaEncaixe.VISUALIZACAO -> Color(0xFF0891B2)
    CategoriaEncaixe.RESULTADO -> Color(0xFF059669)
    CategoriaEncaixe.CONFIGURACOES -> Color(0xFF0F6CBD)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BarraCategoriasEncaixe(
    categoria: CategoriaEncaixe,
    compacta: Boolean,
    modifier: Modifier = Modifier,
    onSelecionar: (CategoriaEncaixe) -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Borda, RoundedCornerShape(14.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CategoriaEncaixe.entries.toList().chunked(3).forEach { categoriasDaLinha ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compacta) 28.dp else 34.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                categoriasDaLinha.forEach { cat ->
                    val titulo = if (cat == CategoriaEncaixe.CONFIGURACOES) "Config." else tituloCategoriaEncaixe(cat)
                    val icone = iconeCategoriaEncaixe(cat)
                    val corCategoria = corCategoriaEncaixe(cat)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (categoria == cat) corCategoria else Fundo)
                            .clickable { onSelecionar(cat) }
                            .padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (!compacta) {
                            Icon(
                                icone,
                                contentDescription = null,
                                tint = if (categoria == cat) Color.White else corCategoria,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                        Text(
                            titulo,
                            fontSize = if (compacta) 8.sp else 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (categoria == cat) Color.White else Texto,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

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

private fun limitesPontos(pontos: List<Offset>): LimitesBloco {
    return LimitesBloco(
        esquerda = pontos.minOf { it.x },
        topo = pontos.minOf { it.y },
        direita = pontos.maxOf { it.x },
        base = pontos.maxOf { it.y },
    )
}

private fun limitesBloco(item: InstanciaEncaixe, bloco: BlocoBase): LimitesBloco =
    limitesPontos(pontosTransformados(item, bloco))

private fun poligonoPosicionado(item: InstanciaEncaixe, bloco: BlocoBase): PoligonoPosicionado {
    val pontos = pontosTransformados(item, bloco)
    return PoligonoPosicionado(pontos, limitesPontos(pontos))
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
    limitesA: LimitesBloco = limitesPontos(a),
    limitesB: LimitesBloco = limitesPontos(b),
): Boolean {
    if (a.size < 3 || b.size < 3) return true

    // A caixa delimitadora elimina quase todas as comparações sem percorrer cada par de
    // arestas. A verificação detalhada O(vértices²) só é necessária quando as caixas se
    // sobrepõem nos dois eixos.
    val gapX = max(0f, max(limitesA.esquerda, limitesB.esquerda) - min(limitesA.direita, limitesB.direita))
    val gapY = max(0f, max(limitesA.topo, limitesB.topo) - min(limitesA.base, limitesB.base))
    val tolerancia = 0.001f
    if (gapX > tolerancia || gapY > tolerancia) {
        return when {
            gapX > tolerancia && gapY <= tolerancia -> gapX + tolerancia >= afastamentoHorizontalMm
            gapY > tolerancia && gapX <= tolerancia -> gapY + tolerancia >= afastamentoVerticalMm
            else -> gapX + tolerancia >= afastamentoHorizontalMm || gapY + tolerancia >= afastamentoVerticalMm
        }
    }

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
    return true
}

private suspend fun calcularPreenchimentoFolha(
    padraoOriginal: List<InstanciaEncaixe>,
    blocos: List<BlocoBase>,
    larguraMm: Float,
    alturaMm: Float,
    margemModuloMm: Float,
    afastamentoHorizontalMm: Float,
    afastamentoVerticalMm: Float,
): ResultadoPreenchimento {
    val posicionadosOriginais = padraoOriginal.mapNotNull { item ->
        blocos.getOrNull(item.blocoIndice)?.let { base -> poligonoPosicionado(item, base) }
    }
    if (posicionadosOriginais.size != padraoOriginal.size || posicionadosOriginais.isEmpty()) {
        return ResultadoPreenchimento(padraoOriginal, 0, false)
    }

    val minX = posicionadosOriginais.minOf { it.limites.esquerda }
    val minY = posicionadosOriginais.minOf { it.limites.topo }
    val maxX = posicionadosOriginais.maxOf { it.limites.direita }
    val maxY = posicionadosOriginais.maxOf { it.limites.base }
    val larguraPadrao = maxX - minX
    val alturaPadrao = maxY - minY
    val tileLargura = larguraPadrao + afastamentoHorizontalMm
    val tileAltura = alturaPadrao + afastamentoVerticalMm
    if (tileLargura <= 0f || tileAltura <= 0f) {
        return ResultadoPreenchimento(padraoOriginal, 0, false)
    }

    val resultado = padraoOriginal.toMutableList()
    val existentes = posicionadosOriginais.toMutableList()
    var adicionados = 0
    var tentativas = 0
    var limiteAtingido = padraoOriginal.size >= LIMITE_INSTANCIAS_PREENCHIMENTO
    var yBase = minY

    busca@ while (!limiteAtingido && yBase + alturaPadrao <= alturaMm - margemModuloMm + 0.01f) {
        var xBase = minX
        while (xBase + larguraPadrao <= larguraMm - margemModuloMm + 0.01f) {
            tentativas++
            if (tentativas > LIMITE_TENTATIVAS_PREENCHIMENTO) {
                limiteAtingido = true
                break@busca
            }
            if (tentativas % 64 == 0) currentCoroutineContext().ensureActive()

            val deslocX = xBase - minX
            val deslocY = yBase - minY
            if (abs(deslocX) > 0.01f || abs(deslocY) > 0.01f) {
                if (resultado.size + padraoOriginal.size > LIMITE_INSTANCIAS_PREENCHIMENTO) {
                    limiteAtingido = true
                    break@busca
                }
                val candidatos = padraoOriginal.map { item ->
                    item.copy(
                        xMm = item.xMm + deslocX,
                        yMm = item.yMm + deslocY,
                        travada = false,
                    )
                }
                val poligonosCandidatos = candidatos.mapNotNull { item ->
                    blocos.getOrNull(item.blocoIndice)?.let { base -> poligonoPosicionado(item, base) }
                }
                val cabemNaFolha = poligonosCandidatos.size == candidatos.size &&
                    poligonosCandidatos.all { candidato ->
                        candidato.limites.esquerda >= margemModuloMm &&
                            candidato.limites.topo >= margemModuloMm &&
                            candidato.limites.direita <= larguraMm - margemModuloMm &&
                            candidato.limites.base <= alturaMm - margemModuloMm
                    }
                val respeitamExistentes = cabemNaFolha && poligonosCandidatos.all { candidata ->
                    existentes.all { existente ->
                        blocosRespeitamAfastamento(
                            candidata.pontos,
                            existente.pontos,
                            afastamentoHorizontalMm,
                            afastamentoVerticalMm,
                            candidata.limites,
                            existente.limites,
                        )
                    }
                }
                if (respeitamExistentes) {
                    resultado.addAll(candidatos)
                    existentes.addAll(poligonosCandidatos)
                    adicionados += candidatos.size
                }
            }
            xBase += tileLargura
        }
        yBase += tileAltura
    }

    return ResultadoPreenchimento(resultado, adicionados, limiteAtingido)
}

private suspend fun calcularAjusteRetangulo(
    originais: List<InstanciaEncaixe>,
    blocos: List<BlocoBase>,
    margemModuloMm: Float,
    afastamentoHorizontalMm: Float,
    afastamentoVerticalMm: Float,
    corrigirAfastamentos: Boolean,
): ResultadoAjusteRetangulo? {
    if (originais.isEmpty()) return null
    val ancoraX = originais.minOf { it.xMm }.toFloat()
    val ancoraY = originais.minOf { it.yMm }.toFloat()

    fun instanciasComFator(fator: Float): List<InstanciaEncaixe> = originais.map { item ->
        item.copy(
            xMm = (ancoraX + (item.xMm.toFloat() - ancoraX) * fator).toDouble(),
            yMm = (ancoraY + (item.yMm.toFloat() - ancoraY) * fator).toDouble(),
        )
    }

    fun posicionar(itens: List<InstanciaEncaixe>): List<PoligonoPosicionado>? {
        val posicionados = itens.mapNotNull { item ->
            blocos.getOrNull(item.blocoIndice)?.let { base -> poligonoPosicionado(item, base) }
        }
        return posicionados.takeIf { it.size == itens.size }
    }

    fun afastamentosValidos(posicionados: List<PoligonoPosicionado>): Boolean {
        for (indice in posicionados.indices) {
            val a = posicionados[indice]
            for (outroIndice in indice + 1 until posicionados.size) {
                val b = posicionados[outroIndice]
                if (!blocosRespeitamAfastamento(
                        a.pontos,
                        b.pontos,
                        afastamentoHorizontalMm,
                        afastamentoVerticalMm,
                        a.limites,
                        b.limites,
                    )
                ) return false
            }
        }
        return true
    }

    var fator = 1f
    var ajustadas = originais
    var posicionadas = posicionar(ajustadas) ?: return null
    val precisavaCorrigir = corrigirAfastamentos && !afastamentosValidos(posicionadas)
    if (precisavaCorrigir) {
        var limiteInferior = 1f
        var limiteSuperior = 1f
        var encontrou = false
        while (limiteSuperior < 64f) {
            currentCoroutineContext().ensureActive()
            limiteSuperior *= 2f
            val candidatas = instanciasComFator(limiteSuperior)
            val poligonos = posicionar(candidatas) ?: return null
            if (afastamentosValidos(poligonos)) {
                encontrou = true
                break
            }
            limiteInferior = limiteSuperior
        }
        if (!encontrou) return null

        repeat(18) {
            currentCoroutineContext().ensureActive()
            val meio = (limiteInferior + limiteSuperior) / 2f
            val candidatas = instanciasComFator(meio)
            val poligonos = posicionar(candidatas) ?: return null
            if (afastamentosValidos(poligonos)) limiteSuperior = meio else limiteInferior = meio
        }
        fator = limiteSuperior
        ajustadas = instanciasComFator(fator)
        posicionadas = posicionar(ajustadas) ?: return null
    }

    val minX = posicionadas.minOf { it.limites.esquerda }
    val minY = posicionadas.minOf { it.limites.topo }
    val maxX = posicionadas.maxOf { it.limites.direita }
    val maxY = posicionadas.maxOf { it.limites.base }
    val margemSegura = margemModuloMm + FOLGA_AJUSTE_RETANGULO_MM
    val deslocX = margemSegura - minX
    val deslocY = margemSegura - minY
    val reposicionadas = ajustadas.map { item ->
        item.copy(xMm = item.xMm + deslocX, yMm = item.yMm + deslocY)
    }
    return ResultadoAjusteRetangulo(
        instancias = reposicionadas,
        larguraMm = (maxX - minX) + 2f * margemSegura,
        alturaMm = (maxY - minY) + 2f * margemSegura,
        afastamentosCorrigidos = precisavaCorrigir && fator > 1f,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EncaixeConsumoScreen(
    contorno: Contorno,
    encaixeInicial: EncaixeConsumo? = null,
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
    val density = LocalDensity.current
    val escopoCalculo = rememberCoroutineScope()
    var telaGirada by remember { mutableStateOf(false) }
    var fracaoVisualizacaoPaisagem by remember { mutableStateOf(0.68f) }
    var tamanhoAreaPaisagem by remember { mutableStateOf(IntSize.Zero) }
    var painelFerramentasCompacto by remember { mutableStateOf(false) }
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
    val encaixeRestaurado = remember(contorno, encaixeInicial) {
        encaixeInicial?.takeIf { it.larguraMm > 0.0 && it.alturaMm > 0.0 }
    }
    var tipo by remember(contorno, encaixeRestaurado) {
        mutableStateOf(encaixeRestaurado?.tipo ?: TipoModuloEncaixe.RETANGULO)
    }
    var larguraMm by remember(contorno, encaixeRestaurado) {
        mutableStateOf(encaixeRestaurado?.larguraMm?.toFloat() ?: 1000f)
    }
    var alturaMm by remember(contorno, encaixeRestaurado) {
        mutableStateOf(encaixeRestaurado?.alturaMm?.toFloat() ?: 1000f)
    }
    var larguraTexto by remember(contorno, encaixeRestaurado) {
        mutableStateOf((encaixeRestaurado?.larguraMm?.roundToInt() ?: 1000).toString())
    }
    var alturaTexto by remember(contorno, encaixeRestaurado) {
        mutableStateOf((encaixeRestaurado?.alturaMm?.roundToInt() ?: 1000).toString())
    }
    var deslocamentoTexto by remember(contorno, encaixeRestaurado) {
        mutableStateOf((encaixeRestaurado?.deslocamentoMm?.roundToInt() ?: 0).toString())
    }
    var tamanhoFolhaSelecionado by remember(contorno, encaixeRestaurado) {
        val salvo = encaixeRestaurado
        val tamanho = salvo?.let {
            TamanhosFolha.firstOrNull { folha ->
                abs(folha.larguraMm - it.larguraMm.toFloat()) < 0.01f &&
                    abs(folha.alturaMm - it.alturaMm.toFloat()) < 0.01f
            }?.nome
        }
        mutableStateOf(tamanho ?: if (salvo == null) "1 × 1 m" else "Personalizada")
    }
    // Pedido do Tiago: "o afastamento mínimo entre blocos deve ter valor para as laterais e
    // acima e abaixo" — dois valores em vez de um só, cada um aplicado ao seu eixo (ver
    // blocosRespeitamAfastamento acima).
    var afastamentoHorizontalMm by remember(contorno, encaixeRestaurado) {
        mutableStateOf(encaixeRestaurado?.afastamentoHorizontalMm?.toFloat() ?: 5f)
    }
    var afastamentoVerticalMm by remember(contorno, encaixeRestaurado) {
        mutableStateOf(encaixeRestaurado?.afastamentoVerticalMm?.toFloat() ?: 5f)
    }
    // Pedido do Tiago: "distância mínima da linha do retângulo ou paralelogramo aos blocos" —
    // diferente do afastamento ENTRE blocos acima: esta é a margem entre a borda do módulo
    // (retângulo/paralelogramo) e qualquer peça encostada nela.
    var margemModuloMm by remember(contorno, encaixeRestaurado) {
        mutableStateOf(encaixeRestaurado?.margemModuloMm?.toFloat() ?: 0f)
    }
    var deslocamentoMm by remember(contorno, encaixeRestaurado) {
        mutableStateOf(encaixeRestaurado?.deslocamentoMm?.toFloat() ?: 0f)
    }
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
    var calculandoEncaixe by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var instancias by remember(contorno, encaixeRestaurado) {
        val salvas = encaixeRestaurado?.instancias.orEmpty().filter { it.blocoIndice in blocos.indices }
        var cursor = 5f
        val iniciais = salvas.ifEmpty {
            blocos.mapIndexed { indice, bloco ->
                InstanciaEncaixe(indice, cursor.toDouble(), 5.0).also { cursor += bloco.largura + 5f }
            }
        }
        mutableStateOf(iniciais)
    }

    val areaRealMm2 = remember(contorno) { CalculoArea.calcularAreaMm2(contorno) }
    val areaModuloMm2 = larguraMm.toDouble() * alturaMm.toDouble()
    val consumoMm2 = if (instancias.isEmpty()) 0.0 else areaModuloMm2 / instancias.size

    fun instanciaValida(candidata: InstanciaEncaixe, indicesIgnorados: Set<Int>): Boolean {
        val bloco = blocos.getOrNull(candidata.blocoIndice) ?: return false
        val poligonoCandidato = poligonoPosicionado(candidata, bloco)
        val limites = poligonoCandidato.limites
        if (limites.esquerda < margemModuloMm || limites.topo < margemModuloMm || limites.direita > larguraMm - margemModuloMm || limites.base > alturaMm - margemModuloMm) return false
        return instancias.indices.all { indice ->
            if (indice in indicesIgnorados) return@all true
            val outra = instancias[indice]
            val baseOutra = blocos.getOrNull(outra.blocoIndice) ?: return@all true
            val outroPoligono = poligonoPosicionado(outra, baseOutra)
            blocosRespeitamAfastamento(
                poligonoCandidato.pontos,
                outroPoligono.pontos,
                afastamentoHorizontalMm,
                afastamentoVerticalMm,
                poligonoCandidato.limites,
                outroPoligono.limites,
            )
        }
    }
    fun instanciaValida(candidata: InstanciaEncaixe, indiceIgnorado: Int): Boolean = instanciaValida(candidata, setOf(indiceIgnorado))

    val indicesComConflito = remember(
        instancias,
        blocos,
        larguraMm,
        alturaMm,
        margemModuloMm,
        afastamentoHorizontalMm,
        afastamentoVerticalMm,
    ) {
        val posicionados = instancias.map { item ->
            blocos.getOrNull(item.blocoIndice)?.let { base -> poligonoPosicionado(item, base) }
        }
        buildSet {
            posicionados.forEachIndexed { indice, poligonoA ->
                poligonoA ?: return@forEachIndexed
                val a = poligonoA.limites
                if (a.esquerda < margemModuloMm || a.topo < margemModuloMm || a.direita > larguraMm - margemModuloMm || a.base > alturaMm - margemModuloMm) add(indice)
                for (outroIndice in indice + 1 until posicionados.size) {
                    val poligonoB = posicionados[outroIndice] ?: continue
                    if (!blocosRespeitamAfastamento(
                            poligonoA.pontos,
                            poligonoB.pontos,
                            afastamentoHorizontalMm,
                            afastamentoVerticalMm,
                            poligonoA.limites,
                            poligonoB.limites,
                        )
                    ) {
                        add(indice)
                        add(outroIndice)
                    }
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
        var tentativas = 0
        var y = margemModuloMm
        while (y <= alturaMm) {
            var x = margemModuloMm
            while (x <= larguraMm) {
                if (++tentativas > LIMITE_TENTATIVAS_BUSCA) return null
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
            var tentativas = 0
            busca@ while (y <= limiteY + 0.001f) {
                var x = margemModuloMm
                while (x <= limiteX + 0.001f) {
                    if (++tentativas > LIMITE_TENTATIVAS_BUSCA) break@busca
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

    fun excluirSelecionados() {
        val indicesExcluir = (if (selecionados.isNotEmpty()) selecionados else setOf(selecionado))
            .filter { it in instancias.indices }
            .toSet()
        if (indicesExcluir.isEmpty()) return

        instancias = instancias.filterIndexed { indice, _ -> indice !in indicesExcluir }
        selecionados = emptySet()
        selecionado = instancias.lastIndex.coerceAtLeast(0)
        referenciaAlinhamento = if (instancias.size > 1) 1 else 0
        guiasAlinhamento = emptyList()
        mensagemPosicionamento = null
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
        if (calculandoEncaixe) return
        if (instancias.isEmpty()) {
            mensagemPosicionamento = "Posicione ao menos um bloco antes de preencher a folha."
            return
        }
        val padraoOriginal = instancias.toList()
        val blocosOriginais = blocos.toList()
        val larguraOriginal = larguraMm
        val alturaOriginal = alturaMm
        val margemOriginal = margemModuloMm
        val afastamentoHorizontalOriginal = afastamentoHorizontalMm
        val afastamentoVerticalOriginal = afastamentoVerticalMm
        calculandoEncaixe = true
        mensagemPosicionamento = "Calculando preenchimento..."

        escopoCalculo.launch {
            try {
                val resultado = withContext(Dispatchers.Default) {
                    calcularPreenchimentoFolha(
                        padraoOriginal = padraoOriginal,
                        blocos = blocosOriginais,
                        larguraMm = larguraOriginal,
                        alturaMm = alturaOriginal,
                        margemModuloMm = margemOriginal,
                        afastamentoHorizontalMm = afastamentoHorizontalOriginal,
                        afastamentoVerticalMm = afastamentoVerticalOriginal,
                    )
                }
                if (instancias != padraoOriginal) {
                    mensagemPosicionamento = "O encaixe mudou durante o cálculo. Toque em preencher novamente."
                } else {
                    instancias = resultado.instancias
                    mensagemPosicionamento = when {
                        resultado.limiteAtingido && resultado.adicionados > 0 ->
                            "Folha preenchida com +${resultado.adicionados} blocos. O limite de segurança de $LIMITE_INSTANCIAS_PREENCHIMENTO peças foi aplicado."
                        resultado.limiteAtingido ->
                            "O limite de segurança do cálculo foi atingido. Reduza a folha ou aumente o padrão."
                        resultado.adicionados > 0 ->
                            "Folha preenchida: +${resultado.adicionados} bloco${if (resultado.adicionados == 1) "" else "s"} seguindo o padrão."
                        else -> "Não havia espaço pra repetir o padrão dentro da folha."
                    }
                }
            } catch (cancelamento: CancellationException) {
                throw cancelamento
            } catch (_: Throwable) {
                mensagemPosicionamento = "Não foi possível concluir o preenchimento. Ajuste o padrão e tente novamente."
            } finally {
                calculandoEncaixe = false
            }
        }
    }

    // "Ajustar ao conteúdo" — pedido do Tiago: "ao clicar em consumo retângulo, o retângulo
    // circule as peças dispostas na área de consumo". Calcula o retângulo delimitador (bounding
    // box) de TODAS as peças já posicionadas, desloca as peças pra encostar no afastamento mínimo
    // e redimensiona largura/altura pra abraçar exatamente esse conjunto.
    fun ajustarRetanguloAoConteudo(corrigirAfastamentos: Boolean) {
        if (instancias.isEmpty() || calculandoEncaixe) return
        val instanciasOriginais = instancias.toList()
        val blocosOriginais = blocos.toList()
        val margemOriginal = margemModuloMm
        val afastamentoHorizontalOriginal = afastamentoHorizontalMm
        val afastamentoVerticalOriginal = afastamentoVerticalMm
        calculandoEncaixe = true
        mensagemPosicionamento = if (corrigirAfastamentos) {
            "Corrigindo afastamentos e ajustando o retângulo..."
        } else {
            "Ajustando o retângulo às peças..."
        }

        escopoCalculo.launch {
            try {
                val resultado = withContext(Dispatchers.Default) {
                    calcularAjusteRetangulo(
                        originais = instanciasOriginais,
                        blocos = blocosOriginais,
                        margemModuloMm = margemOriginal,
                        afastamentoHorizontalMm = afastamentoHorizontalOriginal,
                        afastamentoVerticalMm = afastamentoVerticalOriginal,
                        corrigirAfastamentos = corrigirAfastamentos,
                    )
                }
                val configuracaoMudou = instancias != instanciasOriginais ||
                    margemModuloMm != margemOriginal ||
                    afastamentoHorizontalMm != afastamentoHorizontalOriginal ||
                    afastamentoVerticalMm != afastamentoVerticalOriginal
                if (configuracaoMudou) {
                    mensagemPosicionamento = "A configuração mudou durante o ajuste. Tente novamente."
                } else if (resultado == null) {
                    mensagemPosicionamento = "Não foi possível corrigir automaticamente essa disposição."
                } else {
                    instancias = resultado.instancias
                    larguraMm = resultado.larguraMm
                    alturaMm = resultado.alturaMm
                    larguraTexto = resultado.larguraMm.roundToInt().toString()
                    alturaTexto = resultado.alturaMm.roundToInt().toString()
                    tamanhoFolhaSelecionado = "Personalizada"
                    guiasAlinhamento = emptyList()
                    mensagemPosicionamento = if (resultado.afastamentosCorrigidos) {
                        "Afastamentos corrigidos e retângulo ajustado. O encaixe está pronto para salvar."
                    } else {
                        "Retângulo ajustado às peças. Os afastamentos já estavam corretos."
                    }
                }
            } catch (cancelamento: CancellationException) {
                throw cancelamento
            } catch (_: Throwable) {
                mensagemPosicionamento = "Não foi possível concluir o ajuste automático."
            } finally {
                calculandoEncaixe = false
            }
        }
    }

    var categoria by remember { mutableStateOf(CategoriaEncaixe.BLOCOS) }
    // Zoom/pan da visualização — pedido do Tiago: "esse retângulo pode ser editado com ajuste
    // fino e zoom, com as ferramentas já existentes" — mesmo mecanismo do editor de facas
    // (Pan/Zoom alternando o que o arraste de um dedo faz na prancheta).
    var zoomCanvas by remember { mutableStateOf(1f) }
    var panCanvas by remember { mutableStateOf(Offset.Zero) }
    var modoZoomCanvas by remember { mutableStateOf(false) }

    val salvamentoHabilitado = !salvando && instancias.isNotEmpty() && indicesComConflito.isEmpty()
    fun salvarEncaixe() {
        if (!salvamentoHabilitado) return
        onConfirmado(
            EncaixeConsumo(
                tipo = tipo,
                larguraMm = larguraMm.toDouble(),
                alturaMm = alturaMm.toDouble(),
                deslocamentoMm = if (tipo == TipoModuloEncaixe.PARALELOGRAMO) deslocamentoMm.toDouble() else 0.0,
                afastamentoHorizontalMm = afastamentoHorizontalMm.toDouble(),
                afastamentoVerticalMm = afastamentoVerticalMm.toDouble(),
                instancias = instancias,
                margemModuloMm = margemModuloMm.toDouble(),
            ),
        )
    }

    Scaffold(
        containerColor = Fundo,
        contentWindowInsets = if (telaGirada) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
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
                    Button(
                        onClick = { salvarEncaixe() },
                        enabled = salvamentoHabilitado,
                        modifier = Modifier.width(76.dp).height(38.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulDestaque),
                    ) {
                        Text(if (salvando) "..." else "Salvar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEFF6FF),
                    ) {
                        IconButton(onClick = { telaGirada = true }) {
                            Icon(
                                Icons.Filled.ScreenRotation,
                                contentDescription = "Girar para horizontal",
                                tint = AzulDestaque,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
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
            CompositionLocalProvider(LocalFerramentasCompactas provides painelFerramentasCompacto) {
            Column(
                modifier = modFerramentas.onSizeChanged { tamanho ->
                    painelFerramentasCompacto = tamanho.width < with(density) { 280.dp.roundToPx() }
                },
            ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 4.dp, end = 4.dp, top = 4.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Borda),
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = if (painelFerramentasCompacto) 6.dp else 12.dp,
                        vertical = if (painelFerramentasCompacto) 4.dp else 8.dp,
                    ),
                ) {
                    if (!telaGirada) {
                        BarraCategoriasEncaixe(
                            categoria = categoria,
                            compacta = painelFerramentasCompacto,
                            modifier = Modifier.fillMaxWidth(),
                            onSelecionar = { cat ->
                                categoria = cat
                                if (cat == CategoriaEncaixe.TRANSFORMAR || cat == CategoriaEncaixe.SELECAO) modoZoomCanvas = false
                                if (cat != CategoriaEncaixe.SELECAO) {
                                    selecaoArrastoInicio = null
                                    selecaoArrastoAtual = null
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(if (painelFerramentasCompacto) 5.dp else 8.dp))
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(if (painelFerramentasCompacto) 5.dp else 8.dp),
                    ) {
                        CardCategoriaEncaixe(categoria) {
                        when (categoria) {
                            CategoriaEncaixe.BLOCOS -> {
                                Text("Arraste os blocos para montar a repetição. Peças podem cruzar a borda quando o módulo for repetido.", fontSize = 10.sp, color = Texto.copy(alpha = 0.7f))
                                CardSubconfiguracaoEncaixe(
                                    Icons.Filled.ViewModule,
                                    "Peças do encaixe",
                                    "Escolha o bloco e adicione novas cópias",
                                    cor = Color(0xFF16A34A),
                                ) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    maxItemsInEachRow = if (painelFerramentasCompacto) 1 else 3,
                                ) {
                                    blocos.indices.forEach { indice ->
                                        CartaoFerramenta("Bloco ${indice + 1}", blocoParaAdicionar == indice, Modifier, icone = Icons.Filled.ViewModule, cor = Color(0xFF16A34A)) { blocoParaAdicionar = indice }
                                    }
                                    CartaoFerramenta("Adicionar", false, icone = Icons.Filled.Add) { adicionarBloco() }
                                }
                                }
                                CardSubconfiguracaoEncaixe(
                                    Icons.Filled.OpenInFull,
                                    "Ações de preenchimento",
                                    "Duplicar a seleção ou completar a folha",
                                    cor = Color(0xFF0F6CBD),
                                ) {
                                CartaoFerramenta(
                                    if (selecionados.size > 1) "Duplicar ${selecionados.size} blocos selecionados" else "Duplicar bloco selecionado",
                                    false,
                                    Modifier.fillMaxWidth(),
                                    icone = Icons.Filled.ContentCopy,
                                ) { duplicarSelecionados() }
                                CartaoFerramenta(
                                    titulo = "Excluir blocos selecionados",
                                    ativo = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    icone = Icons.Filled.Delete,
                                    cor = Color(0xFFDC2626),
                                    habilitado = instancias.isNotEmpty(),
                                ) { excluirSelecionados() }
                                // Pedido do Tiago: "crie uma opção em blocos de preencher área de
                                // consumo após adicionar, e segue o padrão que está disposto até
                                // preencher a área da folha com blocos".
                                CartaoFerramenta(
                                    titulo = if (calculandoEncaixe) "Calculando preenchimento..." else "Preencher folha com este padrão",
                                    ativo = calculandoEncaixe,
                                    modifier = Modifier.fillMaxWidth(),
                                    icone = Icons.Filled.OpenInFull,
                                    habilitado = !calculandoEncaixe,
                                ) { preencherFolha() }
                                }
                                CardAlinhamentoEncaixe(
                                    quantidadeBlocos = instancias.size,
                                    selecionado = selecionado,
                                    selecaoMultipla = selecionados.size > 1,
                                    referencia = referenciaAlinhamento,
                                    onReferenciaChange = { referenciaAlinhamento = it },
                                    onAlinhar = { alinharSelecionado(it) },
                                    onLimparGuias = { guiasAlinhamento = emptyList() },
                                )
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
                                CardSubconfiguracaoEncaixe(
                                    Icons.Filled.CropFree,
                                    "Modo de seleção",
                                    "Selecionar pelo toque, arrasto ou lista",
                                    cor = Color(0xFF2563EB),
                                ) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    maxItemsInEachRow = if (painelFerramentasCompacto) 1 else 2,
                                ) {
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
                                }
                                if (selecionados.isNotEmpty()) {
                                    CardSubconfiguracaoEncaixe(
                                        Icons.Filled.CheckBox,
                                        "Itens selecionados",
                                        "Toque em uma peça para removê-la da seleção",
                                        cor = Color(0xFF16A34A),
                                    ) {
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
                            }
                            CategoriaEncaixe.VISUALIZACAO -> {
                                Text("Mova a prancheta livremente ou amplie pra ajustar com mais precisão — pedido do Tiago: ferramentas de visualização iguais às do editor de facas.", fontSize = 10.sp, color = Texto.copy(alpha = 0.7f))
                                CardSubconfiguracaoEncaixe(
                                    Icons.Filled.PanTool,
                                    "Modo de interação",
                                    "Escolha entre mover peças ou navegar na folha",
                                    cor = Color(0xFF0891B2),
                                ) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    maxItemsInEachRow = if (painelFerramentasCompacto) 1 else 2,
                                ) {
                                    CartaoFerramenta("Mover peça", !modoZoomCanvas, Modifier.weight(1f), icone = Icons.Filled.OpenWith) { modoZoomCanvas = false }
                                    CartaoFerramenta("Pan / Zoom", modoZoomCanvas, Modifier.weight(1f), icone = Icons.Filled.PanTool) { modoZoomCanvas = true }
                                }
                                }
                                CardSubconfiguracaoEncaixe(
                                    Icons.Filled.ZoomIn,
                                    "Controle de zoom",
                                    "Aumente, diminua ou restaure a visualização",
                                    cor = Color(0xFF0F6CBD),
                                ) {
                                if (painelFerramentasCompacto) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(5.dp),
                                    ) {
                                        CartaoFerramenta("Diminuir zoom", false, Modifier.fillMaxWidth(), icone = Icons.Filled.ZoomOut) { zoomCanvas = (zoomCanvas - 0.5f).coerceAtLeast(1f) }
                                        Text("${(zoomCanvas * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Texto)
                                        CartaoFerramenta("Aumentar zoom", false, Modifier.fillMaxWidth(), icone = Icons.Filled.ZoomIn) { zoomCanvas = (zoomCanvas + 0.5f).coerceAtMost(6f) }
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        CartaoFerramenta("Diminuir", false, Modifier.weight(1f), icone = Icons.Filled.ZoomOut) { zoomCanvas = (zoomCanvas - 0.5f).coerceAtLeast(1f) }
                                        Text("${(zoomCanvas * 100).roundToInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Texto)
                                        CartaoFerramenta("Aumentar", false, Modifier.weight(1f), icone = Icons.Filled.ZoomIn) { zoomCanvas = (zoomCanvas + 0.5f).coerceAtMost(6f) }
                                    }
                                }
                                CartaoFerramenta("Resetar zoom e posição", false, Modifier.fillMaxWidth(), icone = Icons.Filled.RestartAlt) { zoomCanvas = 1f; panCanvas = Offset.Zero }
                                }
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
                                            excluirSelecionados()
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
                                            excluirSelecionados()
                                        }
                                    }
                                } else {
                                    Text("Toque numa peça na prancheta pra selecionar.", fontSize = 11.sp, color = Texto.copy(alpha = 0.6f))
                                }
                            }
                            CategoriaEncaixe.CONFIGURACOES -> {
                                if (telaGirada) {
                                    CartaoFerramenta(
                                        "Voltar ao retrato",
                                        true,
                                        Modifier.fillMaxWidth(),
                                        icone = Icons.Filled.ScreenRotation,
                                    ) { telaGirada = false }
                                }
                                CardSubconfiguracaoEncaixe(
                                    Icons.Filled.ViewModule,
                                    "Tipo de módulo",
                                    "Retângulo ou paralelogramo",
                                    cor = Color(0xFF16A34A),
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CartaoFerramenta("Retângulo", tipo == TipoModuloEncaixe.RETANGULO, Modifier.weight(1f), icone = Icons.Filled.CropSquare, cor = Color(0xFF16A34A)) { tipo = TipoModuloEncaixe.RETANGULO }
                                        CartaoFerramenta("Paralelogramo", tipo == TipoModuloEncaixe.PARALELOGRAMO, Modifier.weight(1f), icone = Icons.Filled.CropSquare, cor = Color(0xFF0891B2)) { tipo = TipoModuloEncaixe.PARALELOGRAMO }
                                    }
                                }
                                CardSubconfiguracaoEncaixe(
                                    Icons.Filled.Straighten,
                                    if (tipo == TipoModuloEncaixe.RETANGULO) "Dimensões do retângulo" else "Dimensões do paralelogramo",
                                    "Largura, altura e tamanho da folha",
                                    cor = Color(0xFF0F6CBD),
                                ) {
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
                                    CartaoFerramenta(
                                        titulo = "Ajustar somente o retângulo às peças",
                                        ativo = false,
                                        modifier = Modifier.fillMaxWidth(),
                                        icone = Icons.Filled.CropSquare,
                                        habilitado = !calculandoEncaixe,
                                    ) { ajustarRetanguloAoConteudo(corrigirAfastamentos = false) }
                                    CartaoFerramenta(
                                        titulo = if (calculandoEncaixe) "Ajustando..." else "Corrigir afastamentos e ajustar retângulo",
                                        ativo = calculandoEncaixe,
                                        modifier = Modifier.fillMaxWidth(),
                                        icone = Icons.Filled.OpenInFull,
                                        cor = Color(0xFF16A34A),
                                        habilitado = !calculandoEncaixe,
                                    ) { ajustarRetanguloAoConteudo(corrigirAfastamentos = true) }
                                }
                                }
                                CardSubconfiguracaoEncaixe(
                                    Icons.Filled.CropSquare,
                                    "Tamanho da folha",
                                    "Dimensões prontas ou personalizada",
                                    cor = Color(0xFF0891B2),
                                ) {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        maxItemsInEachRow = if (painelFerramentasCompacto) 2 else 3,
                                    ) {
                                        TamanhosFolha.forEach { tamanho ->
                                            CartaoFerramenta(tamanho.nome, tamanhoFolhaSelecionado == tamanho.nome, Modifier, icone = Icons.Filled.CropSquare, cor = Color(0xFF0891B2)) { aplicarTamanhoFolha(tamanho) }
                                        }
                                        CartaoFerramenta("Personalizada", tamanhoFolhaSelecionado == "Personalizada", Modifier, icone = Icons.Filled.Straighten, cor = Color(0xFFF97316)) { tamanhoFolhaSelecionado = "Personalizada" }
                                    }
                                }
                                // Pedido do Tiago: "o afastamento mínimo entre blocos deve ter
                                // valor para as laterais e acima e abaixo" — dois sliders em vez
                                // de um só, cada um no eixo que o nome já diz.
                                CardSubconfiguracaoEncaixe(
                                    Icons.Filled.OpenInFull,
                                    "Distâncias entre blocos",
                                    "Laterais e acima/abaixo; não alteram o retângulo",
                                    cor = Color(0xFFF97316),
                                ) {
                                ControleDistancia(
                                    titulo = "LATERAIS ENTRE BLOCOS (ESQUERDA/DIREITA)",
                                    valor = afastamentoHorizontalMm,
                                    cor = Color(0xFFF97316),
                                ) { valor ->
                                    afastamentoHorizontalMm = valor
                                    guiasAlinhamento = emptyList()
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(2f, 5f, 10f, 20f).forEach { valor ->
                                        CartaoFerramenta("${valor.roundToInt()} mm", afastamentoHorizontalMm == valor, Modifier, icone = Icons.Filled.Straighten, cor = Color(0xFFF97316)) { afastamentoHorizontalMm = valor; guiasAlinhamento = emptyList() }
                                    }
                                }
                                ControleDistancia(
                                    titulo = "ACIMA/ABAIXO ENTRE BLOCOS",
                                    valor = afastamentoVerticalMm,
                                    cor = Color(0xFF0891B2),
                                ) { valor ->
                                    afastamentoVerticalMm = valor
                                    guiasAlinhamento = emptyList()
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(2f, 5f, 10f, 20f).forEach { valor ->
                                        CartaoFerramenta("${valor.roundToInt()} mm", afastamentoVerticalMm == valor, Modifier, icone = Icons.Filled.Straighten, cor = Color(0xFF0891B2)) { afastamentoVerticalMm = valor; guiasAlinhamento = emptyList() }
                                    }
                                }
                                Text(
                                    "Esses valores são aplicados somente entre os blocos.",
                                    fontSize = 9.sp,
                                    color = Texto.copy(alpha = 0.62f),
                                )
                                }
                                // Pedido do Tiago: "distância mínima da linha do retângulo ou
                                // paralelogramo aos blocos" — margem entre a borda do módulo e
                                // qualquer peça encostada nela (diferente do afastamento ENTRE
                                // blocos acima).
                                CardSubconfiguracaoEncaixe(
                                    Icons.Filled.CropSquare,
                                    "Margem da borda",
                                    "Distância exclusiva entre o retângulo e os blocos",
                                    cor = Color(0xFF16A34A),
                                ) {
                                ControleDistancia(
                                    titulo = "BORDA DO RETÂNGULO AOS BLOCOS",
                                    valor = margemModuloMm,
                                    cor = Color(0xFF16A34A),
                                ) { valor -> margemModuloMm = valor }
                                }
                                if (indicesComConflito.isNotEmpty()) {
                                    Text("Existem blocos fora da folha ou mais próximos que o permitido. Eles estão marcados em vermelho.", fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                }
                            }
                            CategoriaEncaixe.RESULTADO -> {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    maxItemsInEachRow = if (painelFerramentasCompacto) 1 else 2,
                                ) {
                                    CardMetricaResultado(Icons.Filled.ViewModule, "Área real dos blocos", "${"%.1f".format(areaRealMm2)} mm²", Color(0xFF16A34A), Modifier.weight(1f))
                                    CardMetricaResultado(Icons.Filled.CropSquare, "Dimensões da folha", "${larguraMm.roundToInt()} × ${alturaMm.roundToInt()} mm", Color(0xFF0F6CBD), Modifier.weight(1f))
                                    CardMetricaResultado(Icons.Filled.OpenInFull, "Área da folha", "${"%.1f".format(areaModuloMm2)} mm²", Color(0xFF0891B2), Modifier.weight(1f))
                                    CardMetricaResultado(Icons.Filled.Straighten, "Afastamento mínimo", "${afastamentoHorizontalMm.roundToInt()} × ${afastamentoVerticalMm.roundToInt()} mm", Color(0xFFF97316), Modifier.weight(1f))
                                    CardMetricaResultado(Icons.Filled.CheckBox, "Peças por folha", "${instancias.size} blocos", Color(0xFF2563EB), Modifier.weight(1f))
                                    CardMetricaResultado(Icons.Filled.CheckBox, "Consumo por bloco", "${"%.1f".format(consumoMm2)} mm² · ${"%.6f".format(consumoMm2 / 1_000_000.0)} m²", Color(0xFF059669), Modifier.weight(1f))
                                }
                            }
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
            }
            }
            }

            if (telaGirada) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .onSizeChanged { tamanhoAreaPaisagem = it },
                ) {
                    Column(
                        modifier = Modifier
                            .weight(fracaoVisualizacaoPaisagem)
                            .fillMaxHeight(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                                .padding(start = 4.dp, top = 2.dp, end = 4.dp),
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Borda),
                                shadowElevation = 1.dp,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Encaixe e consumo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Texto)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("A área real permanece separada", fontSize = 8.sp, color = Texto.copy(alpha = 0.58f), maxLines = 1)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "• ${"%.1f".format(consumoMm2)} mm²/bloco",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857),
                                                maxLines = 1,
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { salvarEncaixe() },
                                        enabled = salvamentoHabilitado,
                                        modifier = Modifier.width(104.dp).height(34.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AzulDestaque),
                                    ) {
                                        Text(if (salvando) "Salvando..." else "✓ Salvar", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        conteudoPrancheta(Modifier.weight(1f).fillMaxWidth())
                    }
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFF8FAFC))
                            .pointerInput(tamanhoAreaPaisagem.width) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val larguraUtil = tamanhoAreaPaisagem.width.toFloat().coerceAtLeast(1f)
                                    val minimoVisual = (with(density) { 320.dp.toPx() } / larguraUtil)
                                        .coerceIn(0.45f, 0.68f)
                                    val maximoVisual = (1f - with(density) { 190.dp.toPx() } / larguraUtil)
                                        .coerceIn(minimoVisual, 0.82f)
                                    fracaoVisualizacaoPaisagem = (
                                        fracaoVisualizacaoPaisagem + dragAmount.x / larguraUtil
                                    ).coerceIn(minimoVisual, maximoVisual)
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(58.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (painelFerramentasCompacto) AzulDestaque else Color(0xFF94A3B8)),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f - fracaoVisualizacaoPaisagem)
                            .fillMaxHeight(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (painelFerramentasCompacto) 80.dp else 92.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BarraCategoriasEncaixe(
                                categoria = categoria,
                                compacta = painelFerramentasCompacto,
                                modifier = Modifier.fillMaxWidth(),
                                onSelecionar = { cat ->
                                    categoria = cat
                                    if (cat == CategoriaEncaixe.TRANSFORMAR || cat == CategoriaEncaixe.SELECAO) modoZoomCanvas = false
                                    if (cat != CategoriaEncaixe.SELECAO) {
                                        selecaoArrastoInicio = null
                                        selecaoArrastoAtual = null
                                    }
                                },
                            )
                        }
                        conteudoFerramentasEncaixe(
                            Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
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

@Composable
private fun CardCategoriaEncaixe(
    categoria: CategoriaEncaixe,
    conteudo: @Composable () -> Unit,
) {
    val compacto = LocalFerramentasCompactas.current
    val cor = corCategoriaEncaixe(categoria)
    val subtitulo = when (categoria) {
        CategoriaEncaixe.BLOCOS -> "Adicionar, duplicar e preencher a folha"
        CategoriaEncaixe.SELECAO -> "Escolher uma ou várias peças"
        CategoriaEncaixe.TRANSFORMAR -> "Mover, girar, alinhar e espelhar"
        CategoriaEncaixe.VISUALIZACAO -> "Navegar e controlar o zoom"
        CategoriaEncaixe.RESULTADO -> "Resumo do encaixe e do consumo"
        CategoriaEncaixe.CONFIGURACOES -> "Módulo, dimensões e afastamentos"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compacto) 14.dp else 18.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, cor.copy(alpha = 0.22f)),
        shadowElevation = if (compacto) 0.dp else 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(if (compacto) 7.dp else 11.dp),
            verticalArrangement = Arrangement.spacedBy(if (compacto) 6.dp else 9.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (compacto) 28.dp else 34.dp)
                        .clip(RoundedCornerShape(if (compacto) 9.dp else 11.dp))
                        .background(cor.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        iconeCategoriaEncaixe(categoria),
                        contentDescription = null,
                        tint = cor,
                        modifier = Modifier.size(if (compacto) 15.dp else 18.dp),
                    )
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        tituloCategoriaEncaixe(categoria),
                        fontSize = if (compacto) 11.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Texto,
                    )
                    Text(
                        subtitulo,
                        fontSize = if (compacto) 8.sp else 9.sp,
                        color = Texto.copy(alpha = 0.58f),
                        maxLines = 1,
                    )
                }
            }
            conteudo()
        }
    }
}

// Pedido do Tiago: "use esse print dessa tela para copiar os estilos de tela" (Expedição Bling)
// — cabeçalho de seção com círculo de ícone + título em negrito + subtítulo cinza, igual aos
// cards "Criar lista de separação"/"Vincular Produtos" daquela tela.
@Composable
private fun CabecalhoSecaoEncaixe(icone: ImageVector, titulo: String, subtitulo: String, cor: Color = corDaFuncao(icone)) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(cor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Texto)
            Text(subtitulo, fontSize = 10.sp, color = Texto.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun CardSubconfiguracaoEncaixe(
    icone: ImageVector,
    titulo: String,
    subtitulo: String,
    cor: Color = corDaFuncao(icone),
    conteudo: @Composable () -> Unit,
) {
    val compacto = LocalFerramentasCompactas.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compacto) 12.dp else 15.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, cor.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(if (compacto) 7.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (compacto) 5.dp else 8.dp),
        ) {
            CabecalhoSecaoEncaixe(icone, titulo, subtitulo, cor)
            conteudo()
        }
    }
}

@Composable
private fun CardMetricaResultado(
    icone: ImageVector,
    rotulo: String,
    valor: String,
    cor: Color,
    modifier: Modifier = Modifier,
) {
    val compacto = LocalFerramentasCompactas.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (compacto) 11.dp else 14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, cor.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.padding(if (compacto) 7.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(if (compacto) 27.dp else 32.dp).clip(RoundedCornerShape(9.dp)).background(cor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(if (compacto) 14.dp else 17.dp))
            }
            Column(modifier = Modifier.padding(start = 7.dp).weight(1f)) {
                Text(rotulo, fontSize = if (compacto) 8.sp else 9.sp, color = Texto.copy(alpha = 0.58f), maxLines = 1)
                Text(valor, fontSize = if (compacto) 10.sp else 12.sp, fontWeight = FontWeight.Bold, color = Texto)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardAlinhamentoEncaixe(
    quantidadeBlocos: Int,
    selecionado: Int,
    selecaoMultipla: Boolean,
    referencia: Int,
    onReferenciaChange: (Int) -> Unit,
    onAlinhar: (String) -> Unit,
    onLimparGuias: () -> Unit,
) {
    val compacto = LocalFerramentasCompactas.current
    CardSubconfiguracaoEncaixe(
        Icons.Filled.AlignHorizontalCenter,
        "Alinhamento por referência",
        "Alinhar uma peça pelas bordas ou pelo centro",
        cor = Color(0xFF0284C7),
    ) {
        when {
            quantidadeBlocos < 2 -> Text(
                "Adicione pelo menos mais um bloco para usar o alinhamento.",
                fontSize = if (compacto) 9.sp else 10.sp,
                color = Texto.copy(alpha = 0.68f),
            )
            selecaoMultipla -> Text(
                "Mantenha somente um bloco selecionado para alinhá-lo com uma referência.",
                fontSize = if (compacto) 9.sp else 10.sp,
                color = Texto.copy(alpha = 0.68f),
            )
            selecionado !in 0 until quantidadeBlocos -> Text(
                "Selecione um bloco na visualização para ativar o alinhamento.",
                fontSize = if (compacto) 9.sp else 10.sp,
                color = Texto.copy(alpha = 0.68f),
            )
            else -> {
                Text("BLOCO DE REFERÊNCIA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Texto.copy(alpha = 0.55f))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxItemsInEachRow = if (compacto) 2 else 4,
                ) {
                    (0 until quantidadeBlocos).filter { it != selecionado }.forEach { indice ->
                        CartaoFerramenta(
                            "Bloco ${indice + 1}",
                            referencia == indice,
                            Modifier,
                            icone = Icons.Filled.ViewModule,
                            cor = Color(0xFF16A34A),
                        ) { onReferenciaChange(indice) }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CartaoFerramenta("Esquerda", false, Modifier.weight(1f), icone = Icons.Filled.AlignHorizontalLeft) { onAlinhar("ESQUERDA") }
                    CartaoFerramenta("Centro X", false, Modifier.weight(1f), icone = Icons.Filled.AlignHorizontalCenter) { onAlinhar("CENTRO_X") }
                    CartaoFerramenta("Direita", false, Modifier.weight(1f), icone = Icons.Filled.AlignHorizontalRight) { onAlinhar("DIREITA") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CartaoFerramenta("Topo", false, Modifier.weight(1f), icone = Icons.Filled.AlignVerticalTop) { onAlinhar("TOPO") }
                    CartaoFerramenta("Centro Y", false, Modifier.weight(1f), icone = Icons.Filled.AlignVerticalCenter) { onAlinhar("CENTRO_Y") }
                    CartaoFerramenta("Base", false, Modifier.weight(1f), icone = Icons.Filled.AlignVerticalBottom) { onAlinhar("BASE") }
                }
                CartaoFerramenta("Limpar linhas de alinhamento", false, Modifier.fillMaxWidth(), icone = Icons.Filled.Remove, onClick = onLimparGuias)
            }
        }
    }
}

@Composable
private fun OpcoesTransformacaoRapida(
    onEspelhar: () -> Unit,
    onInverter: () -> Unit,
    onDefinirAngulo: (Double) -> Unit,
) {
    CardSubconfiguracaoEncaixe(
        Icons.Filled.Flip,
        "Transformações rápidas",
        "Espelhar, inverter ou definir um ângulo",
        cor = Color(0xFFF97316),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CartaoFerramenta("Espelhar", false, Modifier.weight(1f), icone = Icons.Filled.Flip, onClick = onEspelhar)
            CartaoFerramenta("Inverter", false, Modifier.weight(1f), icone = Icons.Filled.SwapVert, onClick = onInverter)
        }
        Text("GIRAR PARA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Texto.copy(alpha = 0.55f))
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
    val compacto = LocalFerramentasCompactas.current
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
    val controles: @Composable () -> Unit = {
        IconButton(
            onClick = { atualizar(valor - incremento) },
            modifier = Modifier.size(if (compacto) 32.dp else 36.dp).clip(CircleShape).background(Color.White).border(1.dp, Borda, CircleShape),
        ) { Icon(Icons.Filled.Remove, contentDescription = "Diminuir passo", tint = Color(0xFFF97316), modifier = Modifier.size(16.dp)) }
        Surface(
            modifier = Modifier.padding(horizontal = 6.dp).width(if (compacto) 68.dp else 78.dp).height(if (compacto) 32.dp else 36.dp),
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
            modifier = Modifier.size(if (compacto) 32.dp else 36.dp).clip(CircleShape).background(Color.White).border(1.dp, Borda, CircleShape),
        ) { Icon(Icons.Filled.Add, contentDescription = "Aumentar passo", tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp)) }
    }
    CardSubconfiguracaoEncaixe(
        if (moverAtivo) Icons.Filled.OpenWith else Icons.Filled.RotateRight,
        "Precisão do ajuste",
        if (moverAtivo) "Distância aplicada em cada toque" else "Ângulo aplicado em cada toque",
        cor = if (moverAtivo) Color(0xFF0F6CBD) else Color(0xFFF97316),
    ) {
    if (compacto) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                if (moverAtivo) "Distância por toque" else "Graus por toque",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Texto.copy(alpha = 0.68f),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { controles() }
        }
    } else {
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
            controles()
        }
    }
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
    CardSubconfiguracaoEncaixe(
        if (moverAtivo) Icons.Filled.OpenWith else Icons.Filled.RotateRight,
        if (moverAtivo) "Controle de movimento" else "Controle de rotação",
        if (moverAtivo) "Ajuste a posição pelas setas" else "Gire para a esquerda ou direita",
        cor = if (moverAtivo) Color(0xFF0891B2) else Color(0xFFF97316),
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
            Icon(icone, contentDescription = descricao, tint = if (habilitado) corDaFuncao(icone) else Texto.copy(alpha = 0.25f), modifier = Modifier.size(20.dp))
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
private fun corDaFuncao(icone: ImageVector?): Color = when (icone) {
    Icons.Filled.Add, Icons.Filled.CheckBox, Icons.Filled.TouchApp -> Color(0xFF16A34A)
    Icons.Filled.Delete -> Color(0xFFDC2626)
    Icons.Filled.RotateLeft, Icons.Filled.RotateRight, Icons.Filled.RestartAlt,
    Icons.Filled.Flip, Icons.Filled.SwapVert -> Color(0xFFF97316)
    Icons.Filled.PanTool, Icons.Filled.ZoomIn, Icons.Filled.ZoomOut,
    Icons.Filled.ScreenRotation -> Color(0xFF0891B2)
    Icons.Filled.Lock, Icons.Filled.LockOpen -> Color(0xFFD97706)
    Icons.Filled.ContentCopy, Icons.Filled.OpenInFull -> Color(0xFF0F6CBD)
    Icons.Filled.AlignHorizontalLeft, Icons.Filled.AlignHorizontalCenter,
    Icons.Filled.AlignHorizontalRight, Icons.Filled.AlignVerticalTop,
    Icons.Filled.AlignVerticalCenter, Icons.Filled.AlignVerticalBottom -> Color(0xFF0284C7)
    else -> AzulDestaque
}

@Composable
private fun CartaoFerramenta(
    titulo: String,
    ativo: Boolean,
    modifier: Modifier = Modifier,
    icone: ImageVector? = null,
    cor: Color? = null,
    habilitado: Boolean = true,
    onClick: () -> Unit,
) {
    val compacto = LocalFerramentasCompactas.current
    val corAcao = cor ?: corDaFuncao(icone)
    val forma = RoundedCornerShape(if (compacto) 9.dp else 12.dp)
    val modificadorBase = modifier
        .clip(forma)
        .background(if (ativo) corAcao.copy(alpha = 0.14f) else Fundo)
        .border(1.dp, if (ativo) corAcao.copy(alpha = 0.42f) else Borda, forma)
        .clickable(enabled = habilitado, onClick = onClick)
    val corConteudo = if (!habilitado) Texto.copy(alpha = 0.3f) else if (ativo) corAcao else Texto
    val corIcone = if (!habilitado) Texto.copy(alpha = 0.3f) else corAcao

    if (compacto) {
        Row(
            modifier = modificadorBase.padding(vertical = 5.dp, horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icone != null) {
                Box(
                    modifier = Modifier.size(22.dp).clip(RoundedCornerShape(7.dp)).background(corAcao.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icone, contentDescription = null, tint = corIcone, modifier = Modifier.size(13.dp))
                }
                Spacer(Modifier.width(3.dp))
            }
            Text(
                titulo,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = corConteudo,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    } else {
        Column(
            modifier = modificadorBase.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (icone != null) {
                Box(
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(corAcao.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icone, contentDescription = null, tint = corIcone, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                titulo,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = corConteudo,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ControleDistancia(
    titulo: String,
    valor: Float,
    cor: Color,
    onValorChange: (Float) -> Unit,
) {
    val compacto = LocalFerramentasCompactas.current
    Text(
        titulo,
        fontSize = if (compacto) 8.sp else 10.sp,
        fontWeight = FontWeight.Bold,
        color = Texto.copy(alpha = 0.62f),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(
            onClick = { onValorChange((valor - 1f).coerceIn(0f, 50f)) },
            enabled = valor > 0f,
            modifier = Modifier
                .size(if (compacto) 26.dp else 30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(cor.copy(alpha = 0.1f))
                .border(1.dp, cor.copy(alpha = 0.35f), RoundedCornerShape(9.dp)),
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Diminuir 1 mm", tint = cor, modifier = Modifier.size(14.dp))
        }
        Slider(
            value = valor,
            onValueChange = { onValorChange(it.roundToInt().toFloat()) },
            valueRange = 0f..50f,
            steps = 49,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { onValorChange((valor + 1f).coerceIn(0f, 50f)) },
            enabled = valor < 50f,
            modifier = Modifier
                .size(if (compacto) 26.dp else 30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(cor.copy(alpha = 0.1f))
                .border(1.dp, cor.copy(alpha = 0.35f), RoundedCornerShape(9.dp)),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar 1 mm", tint = cor, modifier = Modifier.size(14.dp))
        }
        Text(
            "${valor.roundToInt()} mm",
            modifier = Modifier.width(if (compacto) 34.dp else 40.dp),
            fontSize = if (compacto) 8.sp else 10.sp,
            fontWeight = FontWeight.Bold,
            color = Texto,
            textAlign = TextAlign.End,
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
    val compacto = LocalFerramentasCompactas.current
    val forma = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compacto) 44.dp else 50.dp)
            .clip(forma)
            .background(Fundo)
            .border(1.dp, Borda, forma)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(titulo, fontSize = if (compacto) 9.sp else 11.sp, fontWeight = FontWeight.Bold, color = Texto, modifier = Modifier.weight(1f), maxLines = 1)
        IconButton(onClick = { onAjusteFino(-1f) }, modifier = Modifier.size(if (compacto) 26.dp else 30.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = "Diminuir 1mm", tint = Color(0xFFF97316), modifier = Modifier.size(14.dp))
        }
        OutlinedTextField(
            value = valorTexto,
            onValueChange = { texto -> onValorChange(texto.filter { it.isDigit() || it == ',' || it == '.' || it == '-' }) },
            trailingIcon = { Text("mm", fontSize = 9.sp, color = Texto.copy(alpha = 0.6f), modifier = Modifier.padding(end = 6.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Texto),
            modifier = Modifier.width(if (compacto) 82.dp else 108.dp).height(if (compacto) 40.dp else 44.dp),
        )
        IconButton(onClick = { onAjusteFino(1f) }, modifier = Modifier.size(if (compacto) 26.dp else 30.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar 1mm", tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
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
