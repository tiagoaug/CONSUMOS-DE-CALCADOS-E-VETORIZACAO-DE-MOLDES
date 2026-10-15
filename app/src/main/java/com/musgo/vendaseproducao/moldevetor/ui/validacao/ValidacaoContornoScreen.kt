package com.musgo.vendaseproducao.moldevetor.ui.validacao

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.musgo.vendaseproducao.moldevetor.domain.CalculoArea
import com.musgo.vendaseproducao.moldevetor.domain.Contorno
import com.musgo.vendaseproducao.moldevetor.domain.Ponto
import com.musgo.vendaseproducao.moldevetor.visao.ContornoExtractor
import com.musgo.vendaseproducao.moldevetor.visao.MoldeVetorIA
import com.musgo.vendaseproducao.moldevetor.visao.PerspectiveCorrector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class ModoEdicao { MOVER, ADICIONAR, REMOVER, TRAVAR, MEDIR_CURVA, MEDIR_RETA, ZOOM, PAN, SELECIONAR, SELECIONAR_TOQUE, SELECIONAR_PINCEL, BEZIER, LINHA_EIXO }
// A ordem da enum é também a ordem visual das categorias (duas linhas de três itens).
private enum class CategoriaPaleta { SELECAO, EDICAO, TRANSFORMAR, VISUALIZACAO, CORES }

private fun rotuloModo(modo: ModoEdicao): String = when (modo) {
    ModoEdicao.MOVER -> "Mover"
    ModoEdicao.ADICIONAR -> "Adicionar ponto"
    ModoEdicao.REMOVER -> "Remover ponto"
    ModoEdicao.TRAVAR -> "Travar nó"
    ModoEdicao.MEDIR_CURVA -> "Medir curva"
    ModoEdicao.MEDIR_RETA -> "Régua (reta entre pontos)"
    ModoEdicao.ZOOM -> "Zoom"
    ModoEdicao.PAN -> "Pan (arrastar livre)"
    ModoEdicao.SELECIONAR -> "Selecionar área"
    ModoEdicao.SELECIONAR_TOQUE -> "Selecionar por toque"
    ModoEdicao.SELECIONAR_PINCEL -> "Selecionar por arrasto"
    ModoEdicao.BEZIER -> "Curva (arraste uma aresta pra moldar a curva)"
    ModoEdicao.LINHA_EIXO -> "Linha eixo (toque 2 pontos, ajuste e espelhe)"
}

// Paleta do "estilo moderno" já usado em outras partes do app (ver o modal de ferramentas do
// editor de etiquetas) — fundo cinza-claro nos itens, destaque roxo-claro quando ativo.
private val CorBrancoPuro = Color(0xFFFFFFFF)
private val CorFundoEditor = Color(0xFFF4F6F8)
private val CorBordaPaleta = Color(0xFFE2E8F0)
private val CorFundoCartaoItem = Color(0xFFFFFFFF)
private val CorFundoCartaoItemAtivo = Color(0xFFE0EAFF)
private val CorDestaque = Color(0xFF2563EB)
private val CorTextoItem = Color(0xFF1E293B)
private val CoresGeometria = listOf(
    Color(0xFF22C55E), Color(0xFF2563EB), Color(0xFFDC2626), Color(0xFFF59E0B),
    Color(0xFF9333EA), Color(0xFF0D9488), Color(0xFFEC4899), Color(0xFF334155),
)

/** Validação visual do contorno detectado — layout fixo em duas partes: a área de visualização
 * sempre em cima e totalmente visível, e uma área de ferramentas DOCADA embaixo (nunca um popup
 * flutuante cobrindo a foto) — pedido explícito do Tiago depois de ver o popup escurecer/cobrir a
 * visualização: "a visualização deve ser mostrada... pra escolher e visualizar ao mesmo tempo...
 * uma área de ferramentas e acima a visualização". Os 3 grupos (Editar/Transformar/Exibição) são
 * abas dentro dessa área fixa, trocando só o conteúdo interno, nunca o tamanho da visualização.
 *
 * Ferramentas de edição: Mover, Adicionar/Remover vértice, Travar nó (protege um vértice já
 * ajustado), Medir curva (soma a distância em mm ao longo do contorno entre dois vértices, nos
 * dois sentidos possíveis — útil pra bordas curvas, onde a medida "em linha reta" não serve) e
 * Recalcular dentro da área (re-roda a detecção de borda só dentro do polígono já desenhado).
 * Ferramentas de zoom/pan (pedido do Tiago: "ferramentas de pan e zoom, zoom por área e arraste
 * de uma barra"): pinça livre, zoom por área (desenha um retângulo que vira a nova área visível
 * de uma vez), stepper +/-, e barras de rolagem arrastáveis quando ampliado. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ValidacaoContornoScreen(
    contornoInicial: Contorno,
    fotoCorrigida: Bitmap?,
    pxPorMm: Double,
    somenteVisualizacao: Boolean = false,
    onConfirmado: (Contorno) -> Unit,
    onBack: () -> Unit,
) {
    // O editor permanece em retrato. Ao sair, a Activity volta a seguir a orientação do app.
    val context = LocalContext.current
    val activity = remember(context) { context.paraActivity() }
    var telaGirada by remember { mutableStateOf(false) }
    DisposableEffect(activity) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }
    // Girar a tela (botão em Visualização) sobrepõe o retrato travado acima enquanto o usuário
    // optar por ela; ao desligar, volta pro retrato de sempre.
    LaunchedEffect(telaGirada, activity) {
        activity?.requestedOrientation = if (telaGirada) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // Lado real da imagem corrigida, em px — PerspectiveCorrector sempre gera uma imagem
    // quadrada de LADO_SAIDA_MM * pxPorMm (ver PerspectiveCorrector.corrigir).
    val ladoImagemPx = remember(pxPorMm) { (PerspectiveCorrector.LADO_SAIDA_MM * pxPorMm).toFloat() }

    // Vértices mantidos em px da imagem corrigida (não em mm) enquanto o usuário edita — só
    // convertidos de volta pra mm ao confirmar. Furos ficam de fora da edição, mas continuam
    // fazendo parte do contorno confirmado (preservados sem alteração).
    val verticesOriginalPx = remember {
        contornoInicial.externo.map { p -> Offset((p.xMm * pxPorMm).toFloat(), (p.yMm * pxPorMm).toFloat()) }
    }
    var verticesPx by remember { mutableStateOf(verticesOriginalPx) }
    var tamanhoCanvas by remember { mutableStateOf(IntSize.Zero) }
    var indiceArrastando by remember { mutableStateOf(-1) }
    var posicaoToqueTela by remember { mutableStateOf<Offset?>(null) }
    var modo by remember(somenteVisualizacao) { mutableStateOf(if (somenteVisualizacao) ModoEdicao.PAN else ModoEdicao.MOVER) }
    // Pedido do Tiago: "modo evidência da ferramenta de edição que está sendo usada no momento...
    // como temos em etiquetas" — ao escolher uma ferramenta, oculta as abas de categoria e a
    // grade inteira, deixando só a ferramenta ativa e suas opções em evidência, pra sempre ter
    // espaço de trabalho visível sem precisar rolar a tela.
    var modoFoco by remember { mutableStateOf(false) }
    // Pedido do Tiago: "crie uma barra arrastável para poder ajustar o tamanho da barra de
    // visualização e a barra de configurações da maneira que eu precisar... fico com espaços
    // vazios e as ferramentas sem espaço de visualização, isso deve ser ajustado" — fração
    // (0 a 1) da área de trabalho reservada às ferramentas; o resto vai pra prancheta. Ajustável
    // pela alça entre as duas áreas OU pelo slider na aba Configuração.
    var fracaoFerramentas by remember { mutableStateOf(0.42f) }
    var tamanhoCorpo by remember { mutableStateOf(IntSize.Zero) }
    var indicesTravados by remember { mutableStateOf(setOf<Int>()) }
    // Vértice selecionado (fica marcado depois de um arrasto no modo Mover) — habilita o ajuste
    // fino por setas, pra corrigir por frações de milímetro, mais preciso do que o dedo consegue
    // mirar sozinho num toque. Pedido do Tiago: ferramentas de edição vetorial pra mais fluidez.
    var verticeSelecionado by remember { mutableStateOf<Int?>(null) }
    var pontoMedicaoA by remember { mutableStateOf<Int?>(null) }
    var resultadoMedicaoCurva by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    // Régua (reta entre dois pontos/vértices) — pedido do Tiago: "régua de medição, entre pontos,
    // ou medir vértices, e opção de definir tamanhos de vértices". Diferente de Medir curva (que
    // segue o perímetro), aqui é a distância em LINHA RETA entre os dois pontos tocados — e dá
    // pra digitar um novo valor e mover o 2º ponto pra bater com essa distância exata.
    var pontoReguaA by remember { mutableStateOf<Int?>(null) }
    var pontoReguaB by remember { mutableStateOf<Int?>(null) }
    var resultadoReguaMm by remember { mutableStateOf<Double?>(null) }
    var novoValorReguaTexto by remember { mutableStateOf("") }

    // Pedido do Tiago: "crie uma linha eixo, em transformação... a linha eixo pode ser
    // posicionada livremente clicando em dois pontos e podendo ser ajustada como uma linha
    // normal" — diferente da régua (que mede entre VÉRTICES existentes), o eixo é uma linha
    // livre em QUALQUER posição da imagem (coordenadas em px da imagem, mesmo espaço de
    // verticesPx), usada só pra espelhar e soldar metade da peça na outra (ver espelharEFecharPorEixo).
    var eixoPontoA by remember { mutableStateOf<Offset?>(null) }
    var eixoPontoB by remember { mutableStateOf<Offset?>(null) }

    // Pedido do Tiago: "ferramenta chamada curvas que clico em uma edge e posso arrastar para
    // criar uma curva" — toque numa aresta já insere um ponto ali e ele segue o dedo ao vivo,
    // virando uma dobra simples na aresta (continua sendo um polígono comum por baixo, só com
    // mais um vértice — compatível com toda a exportação/área/encaixe já existente).
    // Pedido do Tiago: "ferramenta bezier" — igual à de cima, mas em vez de só inserir 1 ponto,
    // ao soltar o dedo calcula uma curva de Bézier quadrática de verdade (ponto de controle =
    // onde soltou) e substitui a aresta por vários pontos amostrados ao longo dela.
    var edgeBezierInicio by remember { mutableStateOf<Int?>(null) }
    var pontoControleBezierPx by remember { mutableStateOf<Offset?>(null) }
    var recalculando by remember { mutableStateOf(false) }
    // Pedido do Tiago: "módulo IA para ajudar a criar o contorno de peças complexas... podendo
    // escolher usar IA ou não". Desligado por padrão — o fluxo OpenCV já existente continua
    // sendo o padrão; IA é uma opção extra pra casos difíceis.
    var usarIA by remember { mutableStateOf(false) }
    var mensagemRecalculo by remember { mutableStateOf<String?>(null) }
    var pretoEBranco by remember { mutableStateOf(false) }
    var saturacao by remember { mutableStateOf(1f) }
    val escopo = rememberCoroutineScope()

    // Desfazer/Refazer (pedido do Tiago: "editor completo") — guarda um snapshot de verticesPx
    // depois de cada ação CONCLUÍDA (soltar o dedo ao mover, adicionar/remover ponto, espelhar,
    // girar, recalcular) — nunca a cada frame de um arrasto em andamento, senão um único drag
    // viraria dezenas de passos de desfazer. historico[indiceHistorico] é sempre o estado atual.
    var historico by remember { mutableStateOf(listOf(verticesPx)) }
    var indiceHistorico by remember { mutableStateOf(0) }

    fun registrarHistorico() {
        historico = historico.take(indiceHistorico + 1) + listOf(verticesPx)
        indiceHistorico = historico.size - 1
    }

    fun podeDesfazer() = indiceHistorico > 0
    fun podeRefazer() = indiceHistorico < historico.size - 1

    fun desfazer() {
        if (!podeDesfazer()) return
        indiceHistorico--
        verticesPx = historico[indiceHistorico]
    }

    fun refazer() {
        if (!podeRefazer()) return
        indiceHistorico++
        verticesPx = historico[indiceHistorico]
    }

    val colorFilterFoto = remember(pretoEBranco, saturacao) {
        val saturacaoEfetiva = if (pretoEBranco) 0f else saturacao
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturacaoEfetiva) })
    }

    // Zoom/pan da área de visualização (modo Zoom) — não afeta as coordenadas do contorno nem a
    // área calculada, só a exibição; a matemática de toque nos outros modos converte a posição
    // real do dedo de volta pro espaço "sem zoom" antes de comparar com os vértices.
    var zoom by remember { mutableStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    // zoomPorArea alterna entre pinça livre (padrão) e desenhar um retângulo que vira a nova
    // área visível de uma vez só — útil quando o objeto é pequeno numa foto grande e a pinça é
    // imprecisa demais pra mirar só nele.
    var zoomPorArea by remember { mutableStateOf(false) }
    var areaZoomInicio by remember { mutableStateOf<Offset?>(null) }
    var areaZoomAtual by remember { mutableStateOf<Offset?>(null) }
    // Pedido do Tiago: "opção de girar a tela clicando em uma seta na visualização e a barra de
    // ferramentas ficando lateral" — gira a própria Activity pra paisagem (mais espaço horizontal)
    // e troca o empilhamento vertical (canvas em cima, ferramentas embaixo) por lado a lado.
    // Velocidade do Pan. O ajuste aparece diretamente na barra superior quando o modo está ativo.
    // 1x = arrasta 1:1 com o dedo; valores menores dão mais precisão e maiores cobrem mais área.
    var velocidadePan by remember { mutableStateOf(1f) }

    // Seleção por área (modo Selecionar) — pedido do Tiago: "ferramenta de selecionar área, com
    // essa área selecionada eu posso deletar todos os pontos selecionados". Guarda os ÍNDICES
    // selecionados (não um retângulo fixo), pra continuarem válidos mesmo se os vértices se
    // moverem depois por outro motivo.
    var selecaoInicio by remember { mutableStateOf<Offset?>(null) }
    var selecaoAtual by remember { mutableStateOf<Offset?>(null) }
    var indicesSelecionados by remember { mutableStateOf(setOf<Int>()) }
    var tamanhoSelecaoDp by remember { mutableStateOf(36f) }
    var removerDaSelecao by remember { mutableStateOf(false) }
    var toleranciaSuavizacaoMm by remember { mutableStateOf(0.5f) }
    var passoAjusteMm by remember { mutableStateOf(0.2f) }
    val indicesSelecionadosAtuais by rememberUpdatedState(indicesSelecionados)
    val removerDaSelecaoAtual by rememberUpdatedState(removerDaSelecao)
    val raioSelecaoPx = with(LocalDensity.current) { tamanhoSelecaoDp.dp.toPx() }
    val margemPanPx = with(LocalDensity.current) { 56.dp.toPx() }
    val modoDeSelecao = modo == ModoEdicao.SELECIONAR || modo == ModoEdicao.SELECIONAR_TOQUE || modo == ModoEdicao.SELECIONAR_PINCEL

    // Desenho livre de uma geometria nova — pedido do Tiago: "deletar todos os pontos, criar
    // novos pontos ao tocar até fechar uma geometria, e ao fechar colocar uma paleta de cores".
    // Enquanto [desenhandoAberto] = true, cada toque em modo ADICIONAR só empilha mais um ponto
    // no fim (sem inserir em aresta), até o usuário tocar perto do ponto inicial — aí fecha e
    // abre a paleta de cor pra essa geometria.
    var desenhandoAberto by remember { mutableStateOf(false) }
    var corGeometria by remember { mutableStateOf(Color(0xFF22C55E)) }
    var mostrarSeletorCor by remember { mutableStateOf(false) }

    // Pedido do Tiago: "gostaria de duplicar somente dentro de encaixe, pode retirar a opção de
    // vários blocos aqui e deixar em encaixes" — esta tela edita UMA peça só; duplicar/posicionar
    // cópias é coisa da tela de Encaixe e Consumo (que já tem esse conceito, sem somar a área da
    // peça por cada cópia). Os travamentos de edição/movimento/rotação abaixo continuam —
    // aplicam-se à ÚNICA peça desta tela, nada a ver com múltiplos blocos.
    var blocoAtivoTravadoParaEdicao by remember { mutableStateOf(false) }
    var movimentoBlocoAtivoTravado by remember { mutableStateOf(false) }
    var rotacaoBlocoAtivoTravada by remember { mutableStateOf(false) }
    var anguloBlocoAtivo by remember { mutableStateOf(0f) }
    var painelFerramentaVisivel by remember { mutableStateOf(false) }
    var confirmarConclusao by remember { mutableStateOf(false) }

    // Prancheta livre: deixa a imagem percorrer toda a área de trabalho, inclusive sem zoom.
    // Só mantém uma pequena faixa visível para o conteúdo não ser perdido fora da tela.
    fun limitarPan(novoPan: Offset, novoZoom: Float): Offset {
        if (tamanhoCanvas.width <= 0) return novoPan
        val ladoConteudo = tamanhoCanvas.width * novoZoom
        val minX = margemPanPx - ladoConteudo
        val maxX = tamanhoCanvas.width - margemPanPx
        val minY = margemPanPx - ladoConteudo
        val maxY = tamanhoCanvas.height - margemPanPx
        return Offset(novoPan.x.coerceIn(minX, maxX), novoPan.y.coerceIn(minY, maxY))
    }

    fun aplicarZoom(novoZoom: Float) {
        val z = novoZoom.coerceIn(1f, 6f)
        zoom = z
        pan = limitarPan(pan, z)
    }

    val areaMm2 = remember(verticesPx, pxPorMm) {
        val pontosMm = verticesPx.map { p -> Ponto(xMm = p.x / pxPorMm, yMm = p.y / pxPorMm) }
        CalculoArea.areaPoligonoMm2(pontosMm)
    }

    fun espelharHorizontal() {
        if (verticesPx.isEmpty()) return
        val minX = verticesPx.minOf { it.x }
        val maxX = verticesPx.maxOf { it.x }
        verticesPx = verticesPx.map { Offset((minX + maxX) - it.x, it.y) }
        registrarHistorico()
    }

    fun espelharVertical() {
        if (verticesPx.isEmpty()) return
        val minY = verticesPx.minOf { it.y }
        val maxY = verticesPx.maxOf { it.y }
        verticesPx = verticesPx.map { Offset(it.x, (minY + maxY) - it.y) }
        registrarHistorico()
    }

    // Pedido do Tiago: "crie uma linha eixo, em transformação, e coloque a opção de espelhar
    // por linha eixo a geometria e fechar o bloco espelhado, fechando a geometria sem a linha
    // eixo impedir a união" — comum em sola de calçado: traça-se só METADE da peça (as duas
    // pontas do traço encostando na linha de centro) e esta função espelha o traço inteiro por
    // uma linha eixo QUALQUER (não precisa ser um vértice existente, o usuário desenha livre) e
    // solda metade original + metade espelhada num único contorno fechado — a linha eixo em si
    // nunca vira aresta do resultado, é só a "dobra".
    fun espelharEFecharPorEixo() {
        val a = eixoPontoA ?: return
        val b = eixoPontoB ?: return
        if (verticesPx.size < 2) return
        val dx = b.x - a.x
        val dy = b.y - a.y
        val comprimentoQuadrado = dx * dx + dy * dy
        if (comprimentoQuadrado <= 0f) return
        fun refletir(p: Offset): Offset {
            val t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / comprimentoQuadrado
            val projecao = Offset(a.x + t * dx, a.y + t * dy)
            return Offset(2f * projecao.x - p.x, 2f * projecao.y - p.y)
        }
        val espelhados = verticesPx.map { refletir(it) }
        // Metade original (sentido normal) + metade espelhada (sentido invertido) solda as duas
        // pontas, formando um único laço fechado sem precisar da linha eixo como aresta.
        verticesPx = verticesPx + espelhados.reversed()
        indicesTravados = emptySet()
        verticeSelecionado = null
        eixoPontoA = null
        eixoPontoB = null
        modo = ModoEdicao.MOVER
        registrarHistorico()
    }

    fun girar(graus: Float) {
        if (verticesPx.isEmpty()) return
        val centroX = verticesPx.sumOf { it.x.toDouble() }.toFloat() / verticesPx.size
        val centroY = verticesPx.sumOf { it.y.toDouble() }.toFloat() / verticesPx.size
        val rad = Math.toRadians(graus.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()
        verticesPx = verticesPx.map { p ->
            val dx = p.x - centroX
            val dy = p.y - centroY
            Offset(
                centroX + (dx * cosA - dy * sinA),
                centroY + (dx * sinA + dy * cosA),
            )
        }
        registrarHistorico()
    }

    // "Recalcular" (pedido do Tiago): usa o polígono grosseiro que o usuário já arrastou em
    // volta da peça como GUIA pra rodar a detecção de borda de novo, mas só dentro dessa área —
    // encontra a borda real (reta ou curva) sem o usuário precisar ajustar vértice por vértice.
    // Roda em Dispatchers.Default (CPU-bound, igual ao pipeline de CapturaScreen) pra não travar
    // a UI durante o processamento.
    fun recalcularDentroDoGuia() {
        val bitmap = fotoCorrigida ?: return
        recalculando = true
        mensagemRecalculo = null
        escopo.launch {
            val guiaAtual = verticesPx
            val novosPontos: List<Offset>? = if (usarIA) {
                // Pedido do Tiago: "módulo IA para ajudar a criar o contorno de peças complexas,
                // a IA do Claude integrada, podendo escolher usar IA ou não". Mesmo botão
                // "Recalcular", só troca o motor por trás conforme o toggle de IA.
                runCatching { MoldeVetorIA.extrairContorno(bitmap, guiaAtual) }
                    .onFailure { e -> mensagemRecalculo = "Erro ao consultar a IA: ${e.message ?: "tente novamente"}." }
                    .getOrNull()
            } else {
                withContext(Dispatchers.Default) {
                    val imagem = Mat()
                    Utils.bitmapToMat(bitmap, imagem)
                    val guiaCv = guiaAtual.map { org.opencv.core.Point(it.x.toDouble(), it.y.toDouble()) }
                    ContornoExtractor.refinarDentroDoGuia(imagem, guiaCv)
                }?.map { Offset(it.x.toFloat(), it.y.toFloat()) }
            }
            recalculando = false
            if (novosPontos == null) {
                if (mensagemRecalculo == null) {
                    mensagemRecalculo = if (usarIA) {
                        "A IA não conseguiu identificar um contorno claro nessa área. Ajuste o guia, tente com mais contraste/luz ou desative o toggle de IA."
                    } else {
                        "Não foi possível encontrar uma borda clara dentro da área indicada. Ajuste os vértices manualmente ou tente com mais contraste/luz."
                    }
                }
            } else {
                verticesPx = novosPontos
                indicesTravados = emptySet()
                pontoMedicaoA = null
                verticeSelecionado = null
                resultadoMedicaoCurva = null
                registrarHistorico()
            }
        }
    }

    // Rede de segurança separada do desfazer — volta pro contorno EXATO que a detecção
    // automática achou no começo, descartando todo ajuste manual já feito.
    fun resetarAoOriginal() {
        verticesPx = verticesOriginalPx
        indicesTravados = emptySet()
        pontoMedicaoA = null
        verticeSelecionado = null
        resultadoMedicaoCurva = null
        registrarHistorico()
    }

    // Ajuste fino: move só o vértice selecionado por uma fração de milímetro — mais preciso do
    // que o dedo consegue mirar sozinho no toque (pedido do Tiago: ferramentas de edição
    // vetorial pra mais fluidez de trabalho).
    fun ajustarFino(dxMm: Float, dyMm: Float) {
        val indice = verticeSelecionado ?: return
        val passoPx = Offset((dxMm * pxPorMm).toFloat(), (dyMm * pxPorMm).toFloat())
        verticesPx = verticesPx.toMutableList().apply { this[indice] = this[indice] + passoPx }
        registrarHistorico()
    }

    // "Opção de definir tamanhos de vértices" — pedido do Tiago. Move o 2º ponto tocado com a
    // régua ao longo da MESMA reta (mantendo a direção A→B), até a distância ficar exatamente
    // igual ao valor digitado. O 1º ponto (A) nunca se move — serve de referência fixa.
    fun aplicarNovaDistanciaRegua(novoValorMm: Double) {
        val a = pontoReguaA ?: return
        val b = pontoReguaB ?: return
        if (a !in verticesPx.indices || b !in verticesPx.indices || b in indicesTravados) return
        val pA = verticesPx[a]
        val pB = verticesPx[b]
        val direcao = pB - pA
        val distanciaAtualPx = hypot(direcao.x.toDouble(), direcao.y.toDouble())
        if (distanciaAtualPx < 0.001) return
        val novaDistanciaPx = (novoValorMm * pxPorMm)
        val fator = (novaDistanciaPx / distanciaAtualPx).toFloat()
        val novoPontoB = pA + direcao * fator
        verticesPx = verticesPx.toMutableList().apply { this[b] = novoPontoB }
        resultadoReguaMm = novoValorMm
        registrarHistorico()
    }

    fun ajustarFinoSelecionados(dxMm: Float, dyMm: Float) {
        if (indicesSelecionados.isEmpty()) return
        val passoPx = Offset((dxMm * pxPorMm).toFloat(), (dyMm * pxPorMm).toFloat())
        verticesPx = verticesPx.mapIndexed { indice, ponto ->
            if (indice in indicesSelecionados && indice !in indicesTravados) ponto + passoPx else ponto
        }
        registrarHistorico()
    }

    // Exclui todos os pontos marcados pelo retângulo de seleção de uma vez — pedido do Tiago:
    // "com essa área selecionada eu posso deletar todos os pontos selecionados". Nunca deixa
    // menos de 3 vértices (um polígono precisa de no mínimo 3) — se a seleção tentar apagar
    // demais, bloqueia e avisa, em vez de deletar só uma parte sem avisar por quê.
    fun excluirSelecionados() {
        if (indicesSelecionados.isEmpty()) return
        if (verticesPx.size - indicesSelecionados.size < 3) {
            mensagemRecalculo = "Selecione menos pontos — o contorno precisa de pelo menos 3 vértices."
            return
        }
        val indicesOrdenados = indicesSelecionados.sortedDescending()
        var novos = verticesPx
        var travados = indicesTravados
        var selecionado = verticeSelecionado
        for (indice in indicesOrdenados) {
            novos = novos.toMutableList().apply { removeAt(indice) }
            travados = travados.mapNotNull { t -> if (t == indice) null else if (t > indice) t - 1 else t }.toSet()
            selecionado = selecionado?.let { t -> if (t == indice) null else if (t > indice) t - 1 else t }
        }
        verticesPx = novos
        indicesTravados = travados
        verticeSelecionado = selecionado
        indicesSelecionados = emptySet()
        registrarHistorico()
    }

    // Simplifica os trechos selecionados com Ramer-Douglas-Peucker. Diferente da remoção local
    // anterior, o desvio é conferido contra todo o trecho original: pontos redundantes de retas e
    // curvas suaves saem em conjunto, enquanto cantos e nós não selecionados permanecem âncoras.
    fun suavizarSelecionados() {
        val selecionadosValidos = indicesSelecionados.filterTo(mutableSetOf()) { it in verticesPx.indices }
        if (selecionadosValidos.size < 2 || verticesPx.size <= 3) return

        val indicesAtivos = simplificarTrechosSelecionados(
            vertices = verticesPx,
            selecionados = selecionadosValidos,
            travados = indicesTravados,
            toleranciaPx = (toleranciaSuavizacaoMm * pxPorMm).toFloat(),
        )
        val removidos = verticesPx.size - indicesAtivos.size

        if (removidos == 0) {
            mensagemRecalculo = "Nenhum nó redundante nessa tolerância. Aumente o desvio máximo para reduzir mais."
            return
        }

        val mapaNovosIndices = indicesAtivos.withIndex().associate { (novo, original) -> original to novo }
        verticesPx = indicesAtivos.map { verticesPx[it] }
        indicesTravados = indicesTravados.mapNotNull { mapaNovosIndices[it] }.toSet()
        verticeSelecionado = verticeSelecionado?.let { mapaNovosIndices[it] }
        indicesSelecionados = selecionadosValidos.mapNotNull { mapaNovosIndices[it] }.toSet()
        pontoMedicaoA = null
        resultadoMedicaoCurva = null
        registrarHistorico()
        mensagemRecalculo = "$removidos nó${if (removidos == 1) "" else "s"} removido${if (removidos == 1) "" else "s"} sem exceder ${"%.1f".format(toleranciaSuavizacaoMm)} mm de desvio."
    }

    // Apaga TODOS os pontos de uma vez e entra em modo de desenho livre — pedido do Tiago:
    // "opção de deletar todos os pontos, criar novos pontos ao tocar até fechar uma geometria".
    fun limparTudo() {
        verticesPx = emptyList()
        indicesTravados = emptySet()
        verticeSelecionado = null
        indicesSelecionados = emptySet()
        pontoMedicaoA = null
        resultadoMedicaoCurva = null
        desenhandoAberto = true
        modo = ModoEdicao.ADICIONAR
        registrarHistorico()
    }

    // Pedido do Tiago: "crie uma função de apagar todos os nós" — igual a limparTudo(), mas SÓ
    // apaga: não força modo de desenho livre nem troca de ferramenta, pra quem só quer começar
    // o contorno do zero (ex.: desenhar à mão com Polilinha/Curva/Bezier) sem o passo extra de
    // sair do modo de desenho livre depois.
    fun apagarTodosOsNos() {
        verticesPx = emptyList()
        indicesTravados = emptySet()
        verticeSelecionado = null
        indicesSelecionados = emptySet()
        pontoMedicaoA = null
        resultadoMedicaoCurva = null
        pontoReguaA = null
        pontoReguaB = null
        resultadoReguaMm = null
        registrarHistorico()
    }

    fun girarBloco(deltaGraus: Float) {
        if (verticesPx.isEmpty()) return
        if (rotacaoBlocoAtivoTravada) {
            mensagemRecalculo = "A rotação deste bloco está travada."
            return
        }
        girar(deltaGraus)
        anguloBlocoAtivo = ((anguloBlocoAtivo + deltaGraus) % 360f + 360f) % 360f
    }

    fun definirAnguloBloco(novoAngulo: Float) {
        if (rotacaoBlocoAtivoTravada) {
            mensagemRecalculo = "A rotação deste bloco está travada."
            return
        }
        val normalizado = ((novoAngulo % 360f) + 360f) % 360f
        girarBloco(normalizado - anguloBlocoAtivo)
        anguloBlocoAtivo = normalizado
    }

    fun excluirBlocoAtivo() {
        limparTudo()
        blocoAtivoTravadoParaEdicao = false
        movimentoBlocoAtivoTravado = false
        rotacaoBlocoAtivoTravada = false
        anguloBlocoAtivo = 0f
        mensagemRecalculo = "Bloco excluído."
    }

    Scaffold(
        containerColor = CorBrancoPuro,
        topBar = {
            if (!telaGirada) {
            // Cabeçalho compacto (pedido do Tiago: "deixando a área do cabeçalho pequena") — um
            // TopAppBar comum do Material3 tem altura mínima forçada de 64dp mesmo com modifier
            // menor; uma Row simples dá controle total da altura, sem gastar espaço à toa numa
            // tela que é basicamente uma área de trabalho.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(48.dp)
                    .background(CorBrancoPuro)
                    .border(width = 1.dp, color = CorBordaPaleta)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (somenteVisualizacao) "Visualizar BLOCO" else "Editor de facas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CorTextoItem, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (somenteVisualizacao) "Somente leitura" else "Validar contorno", fontSize = 10.sp, color = CorTextoItem.copy(alpha = 0.58f), maxLines = 1)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${"%.1f".format(areaMm2)} mm² (${"%.4f".format(CalculoArea.mm2ParaM2(areaMm2))} m²)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CorDestaque,
                            maxLines = 1,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        if (somenteVisualizacao) {
                            onBack()
                        } else {
                            confirmarConclusao = true
                        }
                    },
                    modifier = Modifier.width(if (somenteVisualizacao) 72.dp else 62.dp).height(32.dp),
                    shape = RoundedCornerShape(50),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(if (somenteVisualizacao) "Fechar" else "OK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).onSizeChanged { tamanhoCorpo = it }) {
            // O painel é declarado aqui, mas renderizado depois da visualização. Pedido do Tiago:
            // "fico com espaços vazios e as ferramentas sem espaço de visualização, isso deve
            // ser ajustado" — a altura fixa de antes cortava painéis com muitos controles (ex.:
            // "Selecionar por arrasto") e desperdiçava espaço noutros; agora recebe o Modifier
            // (peso) de quem chama, flexível de verdade, e o usuário ainda pode ajustar a
            // proporção arrastando a alça entre a prancheta e as ferramentas (ver fracaoFerramentas).
            val painelContextual: @Composable (Modifier) -> Unit = { modPainel ->
            Surface(
                modifier = modPainel.fillMaxWidth(),
                color = CorBrancoPuro,
                border = androidx.compose.foundation.BorderStroke(1.dp, CorBordaPaleta),
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        val avisoFerramenta = mensagemRecalculo
                        Text(
                            text = when {
                                mostrarSeletorCor -> "Cores do bloco"
                                painelFerramentaVisivel -> rotuloModo(modo)
                                else -> "Selecione uma ferramenta"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (painelFerramentaVisivel || mostrarSeletorCor) CorDestaque else CorTextoItem.copy(alpha = 0.55f),
                            modifier = Modifier.weight(if (avisoFerramenta == null) 1f else 0.44f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        avisoFerramenta?.let { msg ->
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = msg,
                                color = if (msg.contains("copiada") || msg.contains("colada") || msg.contains("duplicado") || msg.contains("suavizado")) Color(0xFF047857) else Color(0xFFB91C1C),
                                fontSize = 8.5.sp,
                                lineHeight = 10.sp,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(0.56f),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                    if (mostrarSeletorCor) {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Escolha a cor usada para identificar este bloco.", fontSize = 11.sp, color = CorTextoItem.copy(alpha = 0.7f))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CoresGeometria.forEach { cor ->
                                    AmostraCor(cor = cor, selecionada = cor == corGeometria) { corGeometria = cor }
                                }
                            }
                        }
                    } else if (painelFerramentaVisivel) {
                if (modo == ModoEdicao.PAN) {
                    CartaoFerramenta {
                        AbaFerramenta("Preto e branco", null, pretoEBranco, Modifier) { pretoEBranco = !pretoEBranco }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(160.dp)) {
                            Text("Sat.", fontSize = 11.sp, color = CorTextoItem)
                            Slider(
                                value = saturacao,
                                onValueChange = { saturacao = it },
                                valueRange = 0f..2f,
                                enabled = !pretoEBranco,
                                modifier = Modifier.weight(1f).height(24.dp),
                            )
                        }
                    }
                }
                if (modo == ModoEdicao.MOVER && verticeSelecionado == null) {
                    CartaoFerramenta {
                        BotaoTravaBloco("Edição", blocoAtivoTravadoParaEdicao, Modifier.width(108.dp)) {
                            blocoAtivoTravadoParaEdicao = !blocoAtivoTravadoParaEdicao
                            modo = ModoEdicao.MOVER
                            verticeSelecionado = null
                            indicesSelecionados = emptySet()
                        }
                        BotaoTravaBloco("Mover", movimentoBlocoAtivoTravado, Modifier.width(108.dp)) {
                            movimentoBlocoAtivoTravado = !movimentoBlocoAtivoTravado
                        }
                        BotaoTravaBloco("Rotação", rotacaoBlocoAtivoTravada, Modifier.width(108.dp)) {
                            rotacaoBlocoAtivoTravada = !rotacaoBlocoAtivoTravada
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(210.dp)) {
                            Text("Ângulo", fontSize = 11.sp, color = CorTextoItem)
                            Slider(
                                value = anguloBlocoAtivo,
                                onValueChange = { definirAnguloBloco(it) },
                                valueRange = 0f..360f,
                                steps = 71,
                                enabled = !rotacaoBlocoAtivoTravada,
                                modifier = Modifier.weight(1f).height(24.dp),
                            )
                            Text("${anguloBlocoAtivo.toInt()}°", fontSize = 10.sp, color = CorTextoItem)
                        }
                        OutlinedButton(enabled = !rotacaoBlocoAtivoTravada, onClick = { girarBloco(-5f) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)) { Icon(Icons.Filled.RotateLeft, null, modifier = Modifier.size(17.dp)); Text(" 5°") }
                        OutlinedButton(enabled = !rotacaoBlocoAtivoTravada, onClick = { girarBloco(5f) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)) { Icon(Icons.Filled.RotateRight, null, modifier = Modifier.size(17.dp)); Text(" 5°") }
                        OutlinedButton(
                            onClick = { excluirBlocoAtivo() },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        ) { Text("Excluir bloco", fontSize = 11.sp, color = Color(0xFFDC2626)) }
                    }
                }
                if (modo == ModoEdicao.MOVER && verticeSelecionado != null) {
                    CartaoFerramenta {
                        ControleAjusteFinoOitoDirecoes(
                            passoMm = passoAjusteMm,
                            onPassoChange = { passoAjusteMm = it },
                            onMover = { dx, dy -> ajustarFino(dx, dy) },
                            modifier = Modifier.width(180.dp),
                        )
                    }
                }
                if (modo == ModoEdicao.ADICIONAR) {
                    CartaoFerramenta {
                        OutlinedButton(
                            onClick = { limparTudo() },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        ) { Text("Limpar tudo", fontSize = 12.sp) }
                        Text(
                            text = if (desenhandoAberto) "Toque pra adicionar pontos — toque perto do 1º ponto pra fechar" else "Toque numa aresta pra inserir um ponto",
                            fontSize = 11.sp,
                            color = CorTextoItem,
                        )
                    }
                }
                if (modo == ModoEdicao.LINHA_EIXO) {
                    CartaoFerramenta {
                        Text(
                            text = when {
                                eixoPontoA == null -> "Toque no 1º ponto do eixo"
                                eixoPontoB == null -> "Toque no 2º ponto do eixo"
                                else -> "Arraste os pontos pra ajustar, ou espelhe"
                            },
                            fontSize = 11.sp,
                            color = CorTextoItem,
                        )
                        OutlinedButton(
                            onClick = { eixoPontoA = null; eixoPontoB = null },
                            enabled = eixoPontoA != null || eixoPontoB != null,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        ) { Text("Limpar eixo", fontSize = 12.sp) }
                        Button(
                            onClick = { espelharEFecharPorEixo() },
                            enabled = eixoPontoA != null && eixoPontoB != null,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        ) { Text("Espelhar e fechar", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                }
                if (modo == ModoEdicao.ZOOM) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = CorBrancoPuro,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CorBordaPaleta),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (pretoEBranco) CorFundoCartaoItemAtivo else CorFundoEditor)
                                    .border(
                                        width = if (pretoEBranco) 1.5.dp else 1.dp,
                                        color = if (pretoEBranco) Color(0xFF7C3AED).copy(alpha = 0.45f) else CorBordaPaleta,
                                        shape = RoundedCornerShape(14.dp),
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(108.dp)
                                        .fillMaxHeight()
                                        .clickable { pretoEBranco = !pretoEBranco }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ColorLens,
                                        contentDescription = "Preto e branco",
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(19.dp),
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text("Preto e branco", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CorTextoItem, maxLines = 1)
                                }
                                Box(modifier = Modifier.width(1.dp).height(34.dp).background(CorBordaPaleta))
                                Column(
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        "Saturação ${(saturacao * 100).toInt()}%",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CorTextoItem.copy(alpha = if (pretoEBranco) 0.38f else 0.68f),
                                    )
                                    Slider(
                                        value = saturacao,
                                        onValueChange = { saturacao = it },
                                        valueRange = 0f..2f,
                                        enabled = !pretoEBranco,
                                        modifier = Modifier.fillMaxWidth().height(22.dp),
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CardOpcaoVisualizacao(
                                    icone = Icons.Filled.ZoomIn,
                                    titulo = "Zoom livre",
                                    selecionado = !zoomPorArea,
                                    cor = Color(0xFF0891B2),
                                    modifier = Modifier.weight(1f),
                                ) { zoomPorArea = false }
                                CardOpcaoVisualizacao(
                                    icone = Icons.Filled.CropFree,
                                    titulo = "Zoom por área",
                                    selecionado = zoomPorArea,
                                    cor = Color(0xFF7C3AED),
                                    modifier = Modifier.weight(1f),
                                ) { zoomPorArea = true }
                                CardOpcaoVisualizacao(
                                    icone = Icons.Filled.Refresh,
                                    titulo = "Resetar zoom",
                                    selecionado = false,
                                    cor = Color(0xFFF97316),
                                    modifier = Modifier.weight(1f),
                                ) { zoom = 1f; pan = Offset.Zero }
                            }
                        }
                    }
                }
                if (modo == ModoEdicao.PAN) {
                    CartaoFerramenta {
                        Text("Velocidade do Pan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CorDestaque)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(190.dp)) {
                            Slider(
                                value = velocidadePan,
                                onValueChange = { velocidadePan = it },
                                valueRange = 0.25f..3f,
                                modifier = Modifier.weight(1f).height(24.dp),
                            )
                            Text("${"%.1f".format(velocidadePan)}x", fontSize = 10.sp, color = CorTextoItem)
                        }
                        Text("Arraste livremente a prancheta", fontSize = 11.sp, color = CorTextoItem)
                    }
                }
                resultadoMedicaoCurva?.let { (menor, maior) ->
                    CartaoFerramenta {
                        Text(
                            text = "Curva: ${"%.1f".format(menor)}/${"%.1f".format(maior)} mm",
                            fontSize = 11.sp,
                            color = CorTextoItem,
                        )
                    }
                }
                if (modo == ModoEdicao.MEDIR_RETA) {
                    CartaoFerramenta {
                        if (resultadoReguaMm == null) {
                            Text(
                                text = if (pontoReguaA == null) "Toque no 1º ponto" else "Toque no 2º ponto",
                                fontSize = 11.sp,
                                color = CorTextoItem,
                            )
                        } else {
                            Text("Distância: ${"%.1f".format(resultadoReguaMm)} mm", fontSize = 11.sp, color = CorTextoItem)
                            OutlinedTextField(
                                value = novoValorReguaTexto,
                                onValueChange = { novoValorReguaTexto = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                                label = { Text("Novo valor (mm)", fontSize = 10.sp) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.width(140.dp),
                            )
                            Button(
                                onClick = {
                                    novoValorReguaTexto.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }?.let { aplicarNovaDistanciaRegua(it) }
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            ) { Text("Aplicar", fontSize = 11.sp) }
                        }
                    }
                }
                if (modoDeSelecao) {
                    PainelSelecaoNos(
                        modo = modo,
                        quantidadeSelecionada = indicesSelecionados.size,
                        tamanhoSelecaoDp = tamanhoSelecaoDp,
                        onTamanhoSelecaoChange = { tamanhoSelecaoDp = it },
                        removerDaSelecao = removerDaSelecao,
                        onRemoverDaSelecaoChange = { removerDaSelecao = it },
                        passoAjusteMm = passoAjusteMm,
                        onPassoAjusteChange = { passoAjusteMm = it },
                        onMover = { dx, dy -> ajustarFinoSelecionados(dx, dy) },
                        toleranciaSuavizacaoMm = toleranciaSuavizacaoMm,
                        onToleranciaChange = { toleranciaSuavizacaoMm = it },
                        onSuavizar = { suavizarSelecionados() },
                        onLimpar = { indicesSelecionados = emptySet() },
                        onExcluir = { excluirSelecionados() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                    }
                    }
                }
            }
            }

            // Visualização SEMPRE grande e constante — extraída numa lambda parametrizada por
            // Modifier pra poder ser chamada tanto empilhada (retrato, canvas em cima/ferramentas
            // embaixo) quanto lado a lado com a barra de ferramentas lateral (pedido do Tiago:
            // "girar a tela... barra de ferramentas ficando lateral"), sem duplicar toda a lógica
            // de gestos abaixo — só o Modifier (direção do weight) muda entre os dois layouts.
            val conteudoCanvas: @Composable (Modifier) -> Unit = { modCanvas ->
                    Box(
                        modifier = modCanvas
                            .padding(8.dp)
                            .onSizeChanged { tamanhoCanvas = it }
                    .pointerInput(modo, zoomPorArea, verticesPx.size, tamanhoCanvas, raioSelecaoPx, removerDaSelecao, blocoAtivoTravadoParaEdicao, movimentoBlocoAtivoTravado) {
                        val escala = if (ladoImagemPx > 0f) tamanhoCanvas.width / ladoImagemPx else 1f
                        // Raio de toque bem generoso (bem maior que o círculo visível de 18px)
                        // — vértices de um contorno real costumam ficar bem próximos uns dos
                        // outros, e o dedo cobre uma área bem maior que o desenho do ponto;
                        // pontos no canto da imagem são especialmente difíceis de mirar com
                        // exatidão, por isso o raio é generoso pra todos, não só esses.
                        val raioToquePx = 100f

                        // Converte a posição real do dedo na tela pra o espaço "sem zoom" em
                        // que os vértices são desenhados — necessário pra hit-test continuar
                        // preciso mesmo com a imagem ampliada/deslocada (modo Zoom).
                        fun paraEspacoSemZoom(p: Offset): Offset = (p - pan) / zoom

                        fun nosAtingidosPeloCursor(posicaoTelaBruta: Offset): Set<Int> {
                            val centro = paraEspacoSemZoom(posicaoTelaBruta)
                            val raioSemZoom = raioSelecaoPx / zoom
                            return verticesPx.indices.filter { i ->
                                distancia(verticesPx[i] * escala, centro) <= raioSemZoom
                            }.toSet()
                        }

                        fun aplicarSelecaoCircular(posicaoTelaBruta: Offset, base: Set<Int>): Set<Int> {
                            val atingidos = nosAtingidosPeloCursor(posicaoTelaBruta)
                            return if (removerDaSelecaoAtual) base - atingidos else base + atingidos
                        }

                        when (modo) {
                            ModoEdicao.ZOOM -> if (zoomPorArea) {
                                detectDragGestures(
                                    onDragStart = { p -> areaZoomInicio = p; areaZoomAtual = p },
                                    onDrag = { change, _ -> change.consume(); areaZoomAtual = change.position },
                                    onDragEnd = {
                                        val inicio = areaZoomInicio
                                        val fim = areaZoomAtual
                                        if (inicio != null && fim != null && tamanhoCanvas.width > 0) {
                                            val p1 = paraEspacoSemZoom(inicio)
                                            val p2 = paraEspacoSemZoom(fim)
                                            val minX = min(p1.x, p2.x)
                                            val maxX = maxOf(p1.x, p2.x)
                                            val minY = min(p1.y, p2.y)
                                            val maxY = maxOf(p1.y, p2.y)
                                            val larguraSel = maxX - minX
                                            val alturaSel = maxY - minY
                                            if (larguraSel > 20f && alturaSel > 20f) {
                                                val novoZoom = min(
                                                    tamanhoCanvas.width / larguraSel,
                                                    tamanhoCanvas.height / alturaSel,
                                                ).coerceIn(1f, 6f)
                                                val centroX = (minX + maxX) / 2f
                                                val centroY = (minY + maxY) / 2f
                                                val viewCX = tamanhoCanvas.width / 2f
                                                val viewCY = tamanhoCanvas.height / 2f
                                                zoom = novoZoom
                                                pan = limitarPan(
                                                    Offset(viewCX - novoZoom * centroX, viewCY - novoZoom * centroY),
                                                    novoZoom,
                                                )
                                            }
                                        }
                                        areaZoomInicio = null
                                        areaZoomAtual = null
                                    },
                                    onDragCancel = { areaZoomInicio = null; areaZoomAtual = null },
                                )
                            } else {
                                detectTransformGestures { _, panChange, zoomChange, _ ->
                                    val novoZoom = (zoom * zoomChange).coerceIn(1f, 6f)
                                    zoom = novoZoom
                                    pan = limitarPan(pan + panChange, novoZoom)
                                }
                            }

                            // Pan — pedido do Tiago: "ferramenta pan onde posso arrastar a
                            // visualização de maneira livre... dependendo do zoom". O estado de
                            // pan não é chave do pointerInput: assim a recomposição de cada passo
                            // não cancela o gesto no meio do arraste.
                            ModoEdicao.PAN -> detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    pan = limitarPan(pan + dragAmount * velocidadePan, zoom)
                                },
                            )

                            // Seleção por caixa. Soma ou subtrai os pontos encontrados sem apagar
                            // seleções anteriores, permitindo montar uma seleção precisa em etapas.
                            ModoEdicao.SELECIONAR -> detectDragGestures(
                                onDragStart = { p -> selecaoInicio = p; selecaoAtual = p },
                                onDrag = { change, _ -> change.consume(); selecaoAtual = change.position },
                                onDragEnd = {
                                    val inicio = selecaoInicio
                                    val fim = selecaoAtual
                                    if (inicio != null && fim != null) {
                                        val p1 = paraEspacoSemZoom(inicio)
                                        val p2 = paraEspacoSemZoom(fim)
                                        val minX = min(p1.x, p2.x)
                                        val maxX = maxOf(p1.x, p2.x)
                                        val minY = min(p1.y, p2.y)
                                        val maxY = maxOf(p1.y, p2.y)
                                        val atingidos = verticesPx.indices.filter { i ->
                                            val p = verticesPx[i] * escala
                                            p.x in minX..maxX && p.y in minY..maxY
                                        }.toSet()
                                        indicesSelecionados = if (removerDaSelecaoAtual) {
                                            indicesSelecionadosAtuais - atingidos
                                        } else {
                                            indicesSelecionadosAtuais + atingidos
                                        }
                                    }
                                    selecaoInicio = null
                                    selecaoAtual = null
                                },
                                onDragCancel = { selecaoInicio = null; selecaoAtual = null },
                            )

                            // Um toque seleciona todos os nós sob o círculo ajustável. Com o
                            // modo Subtrair ativo, o mesmo toque remove esses nós da seleção.
                            ModoEdicao.SELECIONAR_TOQUE -> detectTapGestures(
                                onPress = { posicao ->
                                    selecaoAtual = posicao
                                    tryAwaitRelease()
                                    selecaoAtual = null
                                },
                                onTap = { posicao ->
                                    indicesSelecionados = aplicarSelecaoCircular(posicao, indicesSelecionadosAtuais)
                                },
                            )

                            // O pincel acumula pontos enquanto o dedo percorre o contorno. A área
                            // circular permanece do mesmo tamanho visual mesmo com zoom aplicado.
                            ModoEdicao.SELECIONAR_PINCEL -> {
                                var selecaoDoGesto = emptySet<Int>()
                                detectDragGestures(
                                onDragStart = { posicao ->
                                    selecaoDoGesto = aplicarSelecaoCircular(posicao, indicesSelecionadosAtuais)
                                    indicesSelecionados = selecaoDoGesto
                                    selecaoAtual = posicao
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    selecaoDoGesto = aplicarSelecaoCircular(change.position, selecaoDoGesto)
                                    indicesSelecionados = selecaoDoGesto
                                    selecaoAtual = change.position
                                },
                                onDragEnd = { selecaoAtual = null },
                                onDragCancel = { selecaoAtual = null },
                                )
                            }

                            // Pedido do Tiago: "a linha eixo pode ser posicionada livremente
                            // clicando em dois pontos e podendo ser ajustada como uma linha
                            // normal" — o 1º toque solto (sem quase nenhum arrasto) planta o
                            // ponto A, o 2º planta o B; tocar perto de um ponto já plantado
                            // arrasta ELE em vez de criar um novo, pra poder ajustar o eixo
                            // depois de posicionado.
                            ModoEdicao.LINHA_EIXO -> {
                                var eixoArrastando: String? = null
                                detectDragGestures(
                                    onDragStart = { posicaoTelaBruta ->
                                        val posicaoTela = paraEspacoSemZoom(posicaoTelaBruta)
                                        val distA = eixoPontoA?.let { distancia(it * escala, posicaoTela) }
                                        val distB = eixoPontoB?.let { distancia(it * escala, posicaoTela) }
                                        eixoArrastando = when {
                                            distA != null && distA < raioToquePx && (distB == null || distA <= distB) -> "A"
                                            distB != null && distB < raioToquePx -> "B"
                                            eixoPontoA == null -> {
                                                eixoPontoA = posicaoTela / escala
                                                "A"
                                            }
                                            eixoPontoB == null -> {
                                                eixoPontoB = posicaoTela / escala
                                                "B"
                                            }
                                            else -> null
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val posicaoTela = paraEspacoSemZoom(change.position)
                                        when (eixoArrastando) {
                                            "A" -> eixoPontoA = posicaoTela / escala
                                            "B" -> eixoPontoB = posicaoTela / escala
                                        }
                                    },
                                    onDragEnd = { eixoArrastando = null },
                                    onDragCancel = { eixoArrastando = null },
                                )
                            }

                            ModoEdicao.MOVER -> detectDragGestures(
                                onDragStart = { posicaoTelaBruta ->
                                    if (blocoAtivoTravadoParaEdicao) {
                                        indiceArrastando = if (movimentoBlocoAtivoTravado) -1 else -2
                                        verticeSelecionado = null
                                        if (movimentoBlocoAtivoTravado) {
                                            mensagemRecalculo = "A movimentação deste bloco está travada."
                                        }
                                        return@detectDragGestures
                                    }
                                    val posicaoTela = paraEspacoSemZoom(posicaoTelaBruta)
                                    posicaoToqueTela = posicaoTelaBruta
                                    indiceArrastando = verticesPx.indices
                                        .filter { it !in indicesTravados }
                                        .minByOrNull { i -> distancia(verticesPx[i] * escala, posicaoTela) }
                                        ?.takeIf { i -> distancia(verticesPx[i] * escala, posicaoTela) < raioToquePx }
                                        ?: -1
                                    if (indiceArrastando >= 0) verticeSelecionado = indiceArrastando
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val indice = indiceArrastando
                                    if (indice == -2 && escala > 0f) {
                                        val deslocamento = dragAmount / (escala * zoom)
                                        verticesPx = verticesPx.map { it + deslocamento }
                                    } else if (indice >= 0 && escala > 0f) {
                                        posicaoToqueTela = change.position
                                        val novos = verticesPx.toMutableList()
                                        novos[indice] = novos[indice] + dragAmount / (escala * zoom)
                                        verticesPx = novos
                                    }
                                },
                                onDragEnd = {
                                    if (indiceArrastando != -1) registrarHistorico()
                                    indiceArrastando = -1
                                    posicaoToqueTela = null
                                },
                                onDragCancel = { indiceArrastando = -1; posicaoToqueTela = null },
                            )

                            ModoEdicao.REMOVER -> detectTapGestures { posicaoTelaBruta ->
                                if (verticesPx.size <= 3) return@detectTapGestures
                                val posicaoTela = paraEspacoSemZoom(posicaoTelaBruta)
                                val indice = verticesPx.indices
                                    .filter { it !in indicesTravados }
                                    .minByOrNull { i -> distancia(verticesPx[i] * escala, posicaoTela) }
                                    ?.takeIf { i -> distancia(verticesPx[i] * escala, posicaoTela) < raioToquePx }
                                    ?: return@detectTapGestures
                                verticesPx = verticesPx.toMutableList().apply { removeAt(indice) }
                                verticeSelecionado = verticeSelecionado?.let { t ->
                                    when {
                                        t == indice -> null
                                        t > indice -> t - 1
                                        else -> t
                                    }
                                }
                                indicesTravados = indicesTravados.mapNotNull { t ->
                                    when {
                                        t == indice -> null
                                        t > indice -> t - 1
                                        else -> t
                                    }
                                }.toSet()
                                registrarHistorico()
                            }

                            ModoEdicao.TRAVAR -> detectTapGestures { posicaoTelaBruta ->
                                val posicaoTela = paraEspacoSemZoom(posicaoTelaBruta)
                                val indice = verticesPx.indices
                                    .minByOrNull { i -> distancia(verticesPx[i] * escala, posicaoTela) }
                                    ?.takeIf { i -> distancia(verticesPx[i] * escala, posicaoTela) < raioToquePx }
                                    ?: return@detectTapGestures
                                indicesTravados = if (indice in indicesTravados) {
                                    indicesTravados - indice
                                } else {
                                    indicesTravados + indice
                                }
                            }

                            ModoEdicao.MEDIR_CURVA -> detectTapGestures { posicaoTelaBruta ->
                                val posicaoTela = paraEspacoSemZoom(posicaoTelaBruta)
                                val indice = verticesPx.indices
                                    .minByOrNull { i -> distancia(verticesPx[i] * escala, posicaoTela) }
                                    ?.takeIf { i -> distancia(verticesPx[i] * escala, posicaoTela) < raioToquePx }
                                    ?: return@detectTapGestures
                                val a = pontoMedicaoA
                                if (a == null || a == indice) {
                                    pontoMedicaoA = indice
                                    resultadoMedicaoCurva = null
                                } else {
                                    val pontosMm = verticesPx.map { p -> Ponto(xMm = p.x / pxPorMm, yMm = p.y / pxPorMm) }
                                    val caminho1 = comprimentoCaminhoMm(pontosMm, a, indice)
                                    val perimetroTotal = comprimentoCaminhoMm(pontosMm, 0, 0)
                                    resultadoMedicaoCurva = Pair(min(caminho1, perimetroTotal - caminho1), maxOf(caminho1, perimetroTotal - caminho1))
                                    pontoMedicaoA = null
                                }
                            }

                            ModoEdicao.MEDIR_RETA -> detectTapGestures { posicaoTelaBruta ->
                                val posicaoTela = paraEspacoSemZoom(posicaoTelaBruta)
                                val indice = verticesPx.indices
                                    .minByOrNull { i -> distancia(verticesPx[i] * escala, posicaoTela) }
                                    ?.takeIf { i -> distancia(verticesPx[i] * escala, posicaoTela) < raioToquePx }
                                    ?: return@detectTapGestures
                                val a = pontoReguaA
                                if (a == null || a == indice) {
                                    pontoReguaA = indice
                                    pontoReguaB = null
                                    resultadoReguaMm = null
                                    novoValorReguaTexto = ""
                                } else {
                                    val distanciaPx = distancia(verticesPx[a], verticesPx[indice])
                                    pontoReguaB = indice
                                    resultadoReguaMm = distanciaPx / pxPorMm
                                    novoValorReguaTexto = "%.1f".format(distanciaPx / pxPorMm)
                                }
                            }

                            ModoEdicao.ADICIONAR -> detectTapGestures { posicaoTelaBruta ->
                                if (escala <= 0f) return@detectTapGestures
                                val posicaoTela = paraEspacoSemZoom(posicaoTelaBruta)
                                if (desenhandoAberto) {
                                    // Modo de desenho livre: cada toque empilha mais um ponto no
                                    // fim, até tocar perto do ponto inicial pra fechar a geometria
                                    // (aí abre a paleta de cor).
                                    if (verticesPx.size >= 3 && distancia(verticesPx[0] * escala, posicaoTela) < raioToquePx) {
                                        desenhandoAberto = false
                                        mostrarSeletorCor = true
                                    } else {
                                        verticesPx = verticesPx + (posicaoTela / escala)
                                    }
                                    registrarHistorico()
                                    return@detectTapGestures
                                }
                                if (verticesPx.size < 2) return@detectTapGestures
                                // Acha a aresta (par de vértices consecutivos) mais próxima do
                                // toque, em espaço de tela, e insere o novo ponto logo depois
                                // do primeiro vértice dessa aresta.
                                var melhorIndice = 0
                                var melhorDistancia = Float.MAX_VALUE
                                for (i in verticesPx.indices) {
                                    val a = verticesPx[i] * escala
                                    val b = verticesPx[(i + 1) % verticesPx.size] * escala
                                    val d = distanciaPontoSegmento(posicaoTela, a, b)
                                    if (d < melhorDistancia) {
                                        melhorDistancia = d
                                        melhorIndice = i
                                    }
                                }
                                val novoPontoPx = posicaoTela / escala
                                val indiceInsercaoNo = melhorIndice + 1
                                verticesPx = verticesPx.toMutableList().apply { add(indiceInsercaoNo, novoPontoPx) }
                                // Pedido do Tiago: "ao adicionar nós não mova a linha com a
                                // adição de nós" — a régua/medida apontam pra ÍNDICES de
                                // verticesPx; sem este ajuste, inserir um ponto ANTES do índice
                                // referenciado empurra a régua/medida pro vértice errado, fazendo
                                // a linha "pular" de lugar mesmo sem o usuário ter tocado nela.
                                indicesTravados = indicesTravados.map { if (it >= indiceInsercaoNo) it + 1 else it }.toSet()
                                verticeSelecionado = verticeSelecionado?.let { if (it >= indiceInsercaoNo) it + 1 else it }
                                pontoReguaA = pontoReguaA?.let { if (it >= indiceInsercaoNo) it + 1 else it }
                                pontoReguaB = pontoReguaB?.let { if (it >= indiceInsercaoNo) it + 1 else it }
                                pontoMedicaoA = pontoMedicaoA?.let { if (it >= indiceInsercaoNo) it + 1 else it }
                                registrarHistorico()
                            }

                            // Pedido do Tiago: "crie essa nova paleta chamada edição, com a
                            // ferramenta bezier" — igual à de Curvas, mas ao soltar o dedo
                            // calcula uma curva de Bézier quadrática de verdade (ponto de
                            // controle = posição onde soltou) e substitui a aresta inteira por
                            // vários pontos amostrados ao longo da curva, em vez de só 1 ponto.
                            // Pedido do Tiago: "zoom no momento do bezier" — com 1 dedo na tela, o
                            // gesto continua moldando a curva como antes; assim que um 2º dedo
                            // toca, vira pinça de zoom (igual ao modo Zoom), pra poder ampliar e
                            // mirar o ponto de controle com precisão sem trocar de ferramenta.
                            ModoEdicao.BEZIER -> awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                var edgeInicio: Int? = null
                                if (verticesPx.size >= 2) {
                                    val posicaoTela = paraEspacoSemZoom(down.position)
                                    var melhorIndice = 0
                                    var melhorDistancia = Float.MAX_VALUE
                                    for (i in verticesPx.indices) {
                                        val a = verticesPx[i] * escala
                                        val b = verticesPx[(i + 1) % verticesPx.size] * escala
                                        val d = distanciaPontoSegmento(posicaoTela, a, b)
                                        if (d < melhorDistancia) {
                                            melhorDistancia = d
                                            melhorIndice = i
                                        }
                                    }
                                    edgeInicio = melhorIndice
                                    edgeBezierInicio = melhorIndice
                                    pontoControleBezierPx = posicaoTela / escala
                                    // Pedido do Tiago: "a opção de visualizar a área do bezier
                                    // como no zoom, pois como estou com o dedo em cima da edição
                                    // não consigo ver perfeitamente... focando entre os dois
                                    // pontos do bezier" — reaproveita a mesma lupa do modo Mover,
                                    // fixa no meio da aresta (não segue o dedo), pra mostrar a
                                    // curva inteira se formando sem o dedo tampar a visão.
                                    val pA = verticesPx[melhorIndice] * escala
                                    val pB = verticesPx[(melhorIndice + 1) % verticesPx.size] * escala
                                    val meio = (pA + pB) / 2f
                                    posicaoToqueTela = meio * zoom + pan
                                }
                                var pinchando = false
                                while (true) {
                                    val evento = awaitPointerEvent()
                                    val pressionados = evento.changes.filter { it.pressed }
                                    if (pressionados.isEmpty()) break
                                    if (pressionados.size >= 2) {
                                        if (!pinchando) {
                                            pinchando = true
                                            edgeBezierInicio = null
                                            pontoControleBezierPx = null
                                            posicaoToqueTela = null
                                        }
                                        val novoZoom = (zoom * evento.calculateZoom()).coerceIn(1f, 6f)
                                        pan = limitarPan(pan + evento.calculatePan(), novoZoom)
                                        zoom = novoZoom
                                        evento.changes.forEach { it.consume() }
                                    } else if (!pinchando && edgeInicio != null) {
                                        val change = pressionados.first()
                                        pontoControleBezierPx = paraEspacoSemZoom(change.position) / escala
                                        change.consume()
                                    }
                                }
                                posicaoToqueTela = null
                                if (!pinchando) {
                                    val i = edgeInicio
                                    val controle = pontoControleBezierPx
                                    if (i != null && controle != null && i in verticesPx.indices) {
                                        val pA = verticesPx[i]
                                        val pB = verticesPx[(i + 1) % verticesPx.size]
                                        val amostras = 9
                                        val novosPontos = (1 until amostras).map { passo ->
                                            val t = passo.toFloat() / amostras
                                            val mt = 1f - t
                                            Offset(
                                                mt * mt * pA.x + 2f * mt * t * controle.x + t * t * pB.x,
                                                mt * mt * pA.y + 2f * mt * t * controle.y + t * t * pB.y,
                                            )
                                        }
                                        val indiceInsercao = i + 1
                                        verticesPx = verticesPx.toMutableList().apply { addAll(indiceInsercao, novosPontos) }
                                        val deslocamento = novosPontos.size
                                        // Pedido do Tiago: "ao adicionar nós não mova a linha com
                                        // a adição de nós" — mesmo ajuste de índices do modo
                                        // Adicionar, aqui pros vários pontos que a curva insere.
                                        indicesTravados = indicesTravados.map { if (it >= indiceInsercao) it + deslocamento else it }.toSet()
                                        verticeSelecionado = verticeSelecionado?.let { if (it >= indiceInsercao) it + deslocamento else it }
                                        pontoReguaA = pontoReguaA?.let { if (it >= indiceInsercao) it + deslocamento else it }
                                        pontoReguaB = pontoReguaB?.let { if (it >= indiceInsercao) it + deslocamento else it }
                                        pontoMedicaoA = pontoMedicaoA?.let { if (it >= indiceInsercao) it + deslocamento else it }
                                        registrarHistorico()
                                    }
                                }
                                edgeBezierInicio = null
                                pontoControleBezierPx = null
                            }
                        }
                    },
            ) {
                val escalaAtual = if (ladoImagemPx > 0f && tamanhoCanvas.width > 0) tamanhoCanvas.width / ladoImagemPx else 1f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    clipRect {
                        translate(left = pan.x, top = pan.y) {
                            scale(scale = zoom, pivot = Offset.Zero) {
                                fotoCorrigida?.let { bmp ->
                                    drawImage(
                                        image = bmp.asImageBitmap(),
                                        dstSize = IntSize((bmp.width * escalaAtual).toInt(), (bmp.height * escalaAtual).toInt()),
                                        colorFilter = colorFilterFoto,
                                    )
                                }
                                desenharCena(null, verticesPx, escalaAtual, null, indicesTravados, pontoMedicaoA, verticeSelecionado, indicesSelecionados, corGeometria, !desenhandoAberto, zoom)
                                // Prévia da curva Bézier enquanto o dedo arrasta — pedido do
                                // Tiago: "ferramenta bezier". Só visual, a aresta só é
                                // substituída de verdade ao soltar o dedo (onDragEnd).
                                val iBezier = edgeBezierInicio
                                val controleBezier = pontoControleBezierPx
                                if (iBezier != null && controleBezier != null && iBezier in verticesPx.indices) {
                                    val pA = verticesPx[iBezier] * escalaAtual
                                    val pB = verticesPx[(iBezier + 1) % verticesPx.size] * escalaAtual
                                    val pControle = controleBezier * escalaAtual
                                    val caminhoPreview = Path().apply {
                                        moveTo(pA.x, pA.y)
                                        quadraticTo(pControle.x, pControle.y, pB.x, pB.y)
                                    }
                                    drawPath(caminhoPreview, color = Color(0xFFEC4899), style = Stroke(width = 4f / zoom))
                                    drawCircle(color = Color(0xFFEC4899), radius = 8f / zoom, center = pControle)
                                }
                                // Linha da régua — pedido do Tiago: "régua de medição, entre
                                // pontos". Desenhada DENTRO do pan/zoom, acompanha o conteúdo.
                                val ra = pontoReguaA
                                val rb = pontoReguaB
                                if (ra != null && ra in verticesPx.indices) {
                                    val pA = verticesPx[ra] * escalaAtual
                                    if (rb != null && rb in verticesPx.indices) {
                                        val pB = verticesPx[rb] * escalaAtual
                                        drawLine(
                                            color = Color(0xFFEAB308),
                                            start = pA,
                                            end = pB,
                                            strokeWidth = 3f / zoom,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f / zoom, 8f / zoom)),
                                        )
                                    }
                                    drawCircle(color = Color(0xFFEAB308), radius = 10f / zoom, center = pA, style = Stroke(width = 3f / zoom))
                                }
                                // Linha eixo — pedido do Tiago: "crie uma linha eixo... posicionada
                                // livremente clicando em dois pontos e podendo ser ajustada como
                                // uma linha normal". Estendida um pouco além dos dois pontos (não
                                // só o segmento entre eles) pra deixar claro que é uma linha de
                                // dobra/espelho, não uma medida pontual.
                                val eA = eixoPontoA
                                val eB = eixoPontoB
                                if (eA != null) {
                                    val pA = eA * escalaAtual
                                    if (eB != null) {
                                        val pB = eB * escalaAtual
                                        val direcao = pB - pA
                                        val extensao = direcao * 0.3f
                                        drawLine(
                                            color = Color(0xFF0D9488),
                                            start = pA - extensao,
                                            end = pB + extensao,
                                            strokeWidth = 3f / zoom,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f / zoom, 6f / zoom)),
                                        )
                                        drawCircle(color = Color(0xFF0D9488), radius = 9f / zoom, center = pB, style = Stroke(width = 3f / zoom))
                                    }
                                    drawCircle(color = Color(0xFF0D9488), radius = 9f / zoom, center = pA, style = Stroke(width = 3f / zoom))
                                }
                            }
                        }
                    }
                    // Retângulo de seleção do "zoom por área", desenhado em espaço de TELA (fora
                    // da transformação de pan/zoom acima) — mostra exatamente a região que vai
                    // virar a nova área visível ao soltar o dedo.
                    val ini = areaZoomInicio
                    val fim = areaZoomAtual
                    if (ini != null && fim != null) {
                        drawRect(
                            color = Color(0xFFEAB308),
                            topLeft = Offset(min(ini.x, fim.x), min(ini.y, fim.y)),
                            size = Size(abs(fim.x - ini.x), abs(fim.y - ini.y)),
                            style = Stroke(width = 4f),
                        )
                    }
                    // Retângulo do modo Selecionar — mesmo princípio, cor diferente (azul) pra
                    // não confundir com o de zoom por área.
                    val selIni = selecaoInicio
                    val selFim = selecaoAtual
                    if (selIni != null && selFim != null) {
                        drawRect(
                            color = Color(0xFF2563EB),
                            topLeft = Offset(min(selIni.x, selFim.x), min(selIni.y, selFim.y)),
                            size = Size(abs(selFim.x - selIni.x), abs(selFim.y - selIni.y)),
                            style = Stroke(width = 4f),
                        )
                    }
                }

                // Barras de rolagem arrastáveis — só aparecem quando há zoom aplicado (pedido do
                // Tiago: "arraste de uma barra"), como alternativa ao arrastar a imagem direto
                // com o dedo no modo Zoom.
                if (zoom > 1f && tamanhoCanvas.width > 0) {
                    val ladoConteudo = tamanhoCanvas.width * zoom
                    val alcanceX = (ladoConteudo - tamanhoCanvas.width).coerceAtLeast(0f)
                    val alcanceY = (ladoConteudo - tamanhoCanvas.height).coerceAtLeast(0f)
                    BarraRolagem(
                        horizontal = true,
                        fracaoVisivel = tamanhoCanvas.width / ladoConteudo,
                        fracaoPosicao = if (alcanceX > 0f) (-pan.x) / alcanceX else 0f,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(end = 14.dp),
                        aoArrastarFracao = { deltaFracao ->
                            pan = limitarPan(Offset(pan.x - deltaFracao * alcanceX, pan.y), zoom)
                        },
                    )
                    BarraRolagem(
                        horizontal = false,
                        fracaoVisivel = tamanhoCanvas.height / ladoConteudo,
                        fracaoPosicao = if (alcanceY > 0f) (-pan.y) / alcanceY else 0f,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxHeight()
                            .padding(bottom = 14.dp),
                        aoArrastarFracao = { deltaFracao ->
                            pan = limitarPan(Offset(pan.x, pan.y - deltaFracao * alcanceY), zoom)
                        },
                    )
                }

                // A lupa fica sempre num canto e substitui o círculo que antes acompanhava o
                // dedo. Na seleção, ela muda para o lado oposto ao toque e mostra o tamanho real
                // do cursor, os nós já marcados e o centro exato atingido.
                val posicaoLupa = posicaoToqueTela ?: if (modoDeSelecao) selecaoAtual else null
                posicaoLupa?.let { posicao ->
                    val tamanhoLupaDp = 140.dp
                    val exibindoSelecao = modoDeSelecao && selecaoAtual != null
                    val zoomLupa = if (exibindoSelecao) 2f else 3f
                    val alinhamentoLupa = if (posicao.x > tamanhoCanvas.width / 2f) Alignment.TopStart else Alignment.TopEnd
                    val corCursor = if (removerDaSelecao) Color(0xFFDC2626) else CorDestaque
                    Box(
                        modifier = Modifier
                            .align(alinhamentoLupa)
                            .padding(12.dp)
                            .size(tamanhoLupaDp)
                            .clip(CircleShape),
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = Color.White)
                            val posicaoSemZoomUsuario = (posicao - pan) / zoom
                            translate(left = size.width / 2f - posicaoSemZoomUsuario.x * zoomLupa, top = size.height / 2f - posicaoSemZoomUsuario.y * zoomLupa) {
                                scale(scale = zoomLupa, pivot = Offset.Zero) {
                                    desenharCena(
                                        fotoCorrigida,
                                        verticesPx,
                                        escalaAtual,
                                        colorFilterFoto,
                                        indicesTravados,
                                        pontoMedicaoA,
                                        verticeSelecionado,
                                        indicesSelecionados,
                                        corGeometria,
                                        !desenhandoAberto,
                                        zoomLupa,
                                    )
                                    // Pedido do Tiago: "quando vou fazer curva com a ferramenta
                                    // curva a visualização não mostra o que está acontecendo,
                                    // somente uma imagem do local" — a lupa chamava só
                                    // desenharCena (foto + contorno parados) e nunca desenhava a
                                    // prévia da curva sendo moldada; replica aqui o mesmo desenho
                                    // da curva feito no canvas principal.
                                    val iBezierLupa = edgeBezierInicio
                                    val controleBezierLupa = pontoControleBezierPx
                                    if (iBezierLupa != null && controleBezierLupa != null && iBezierLupa in verticesPx.indices) {
                                        val pA = verticesPx[iBezierLupa] * escalaAtual
                                        val pB = verticesPx[(iBezierLupa + 1) % verticesPx.size] * escalaAtual
                                        val pControle = controleBezierLupa * escalaAtual
                                        val caminhoPreviewLupa = Path().apply {
                                            moveTo(pA.x, pA.y)
                                            quadraticTo(pControle.x, pControle.y, pB.x, pB.y)
                                        }
                                        drawPath(caminhoPreviewLupa, color = Color(0xFFEC4899), style = Stroke(width = 4f / zoomLupa))
                                        drawCircle(color = Color(0xFFEC4899), radius = 8f / zoomLupa, center = pControle)
                                    }
                                }
                            }
                            if (exibindoSelecao) {
                                val raioNaLupa = (raioSelecaoPx * zoomLupa / zoom)
                                    .coerceAtMost(size.minDimension / 2f - 8f)
                                drawCircle(color = corCursor.copy(alpha = 0.10f), radius = raioNaLupa, center = center)
                                drawCircle(color = corCursor, radius = raioNaLupa, center = center, style = Stroke(width = 3f))
                            }
                            drawCircle(color = corCursor, radius = 4f, center = center)
                        }
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = if (exibindoSelecao) corCursor else Color(0xFF0F172A), radius = size.minDimension / 2f, style = Stroke(width = 6f))
                        }
                    }
                }

            }
            }

            // Pedido do Tiago: "barra de ferramentas ficando lateral" — mesmo painel contextual +
            // abas de categoria de sempre, só que também extraído numa lambda pra poder virar uma
            // coluna lateral ao lado do canvas, em vez de ficar sempre embaixo dele. Recebe um
            // Modifier (peso) de quem chama pra ocupar exatamente a fração reservada a ela —
            // nunca mais altura fixa, que era o que cortava painéis grandes (ver fracaoFerramentas).
            val conteudoFerramentas: @Composable (Modifier) -> Unit = { modFerramentas ->
            Column(modifier = modFerramentas) {
                    if (!somenteVisualizacao) {
                    if (modoFoco) {
                        // Modo evidência — só a tira compacta (nome da ferramenta + voltar) e as
                        // opções daquela ferramenta; nada de abas de categoria nem grade de
                        // ícones, pra sobrar o máximo de área de trabalho.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(CorBrancoPuro)
                                .border(width = 1.dp, color = CorBordaPaleta)
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = {
                                modoFoco = false
                                mostrarSeletorCor = false
                            }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar às ferramentas", tint = CorDestaque, modifier = Modifier.size(17.dp))
                            }
                            Text(
                                text = if (mostrarSeletorCor) "Cores do bloco" else rotuloModo(modo),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CorDestaque,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(start = 4.dp),
                            )
                        }
                        painelContextual(Modifier.weight(1f))
                    } else {
                    if (!telaGirada) painelContextual(Modifier.weight(0.4f).fillMaxWidth())

                    PaletaCategoriasHorizontais(
                            modifier = Modifier.weight(if (telaGirada) 1f else 0.6f).fillMaxWidth(),
                            modo = modo,
                            podeDesfazer = podeDesfazer(),
                            podeRefazer = podeRefazer(),
                            recalculando = recalculando,
                            paletaCorAberta = mostrarSeletorCor,
                            telaHorizontal = telaGirada,
                            onSelecionarModo = { novoModo ->
                                val permitidoComBlocoTravado = novoModo == ModoEdicao.MOVER || novoModo == ModoEdicao.PAN || novoModo == ModoEdicao.ZOOM
                                if (blocoAtivoTravadoParaEdicao && !permitidoComBlocoTravado) {
                                    modo = ModoEdicao.MOVER
                                    mensagemRecalculo = "Destrave o bloco em Mover para editar seus nós."
                                } else {
                                    modo = novoModo
                                }
                                painelFerramentaVisivel = true
                                mostrarSeletorCor = false
                                modoFoco = true
                                if (novoModo == ModoEdicao.MEDIR_CURVA) {
                                    pontoMedicaoA = null
                                    resultadoMedicaoCurva = null
                                }
                                if (novoModo == ModoEdicao.MEDIR_RETA) {
                                    pontoReguaA = null
                                    pontoReguaB = null
                                    resultadoReguaMm = null
                                    novoValorReguaTexto = ""
                                }
                            },
                            onDesfazer = { desfazer() },
                            onRefazer = { refazer() },
                            onRecalcular = { recalcularDentroDoGuia() },
                            onResetarOriginal = { resetarAoOriginal() },
                            onEspelharH = { espelharHorizontal() },
                            onEspelharV = { espelharVertical() },
                            onGirarEsquerda = { girarBloco(-90f) },
                            onGirarDireita = { girarBloco(90f) },
                            onZoomMais = { aplicarZoom(zoom + 0.25f) },
                            onZoomMenos = { aplicarZoom(zoom - 0.25f) },
                            onResetZoom = { zoom = 1f; pan = Offset.Zero },
                            onCores = {
                                val abrirCores = !mostrarSeletorCor
                                mostrarSeletorCor = abrirCores
                                if (abrirCores) {
                                    painelFerramentaVisivel = false
                                    if (telaGirada) modoFoco = true
                                }
                            },
                            onAlternarTela = { telaGirada = !telaGirada },
                            fracaoFerramentas = fracaoFerramentas,
                            onFracaoFerramentasChange = { fracaoFerramentas = it.coerceIn(0.22f, 0.75f) },
                            usarIA = usarIA,
                            onUsarIAChange = { usarIA = it },
                            onPolilinha = { limparTudo() },
                            onApagarTodosOsNos = { apagarTodosOsNos() },
                            desenhandoLivreAtivo = desenhandoAberto,
                    )
                    }
                    }
            }
            }

            if (telaGirada) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1f - fracaoFerramentas).fillMaxHeight()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(CorBrancoPuro)
                                .border(width = 1.dp, color = CorBordaPaleta)
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (somenteVisualizacao) "Visualizar BLOCO" else "Editor de facas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CorTextoItem, maxLines = 1)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (somenteVisualizacao) "Somente leitura" else "Validar contorno", fontSize = 10.sp, color = CorTextoItem.copy(alpha = 0.58f), maxLines = 1)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${"%.1f".format(areaMm2)} mm² (${"%.4f".format(CalculoArea.mm2ParaM2(areaMm2))} m²)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CorDestaque,
                                        maxLines = 1,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { if (somenteVisualizacao) onBack() else confirmarConclusao = true },
                                modifier = Modifier.width(if (somenteVisualizacao) 72.dp else 62.dp).height(32.dp),
                                shape = RoundedCornerShape(50),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            ) {
                                Text(if (somenteVisualizacao) "Fechar" else "OK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        conteudoCanvas(Modifier.weight(1f).fillMaxWidth().background(CorFundoEditor))
                    }
                    AlcaArrastavel(horizontal = true) { deltaPx ->
                        val largura = tamanhoCorpo.width
                        if (largura > 0) fracaoFerramentas = (fracaoFerramentas - deltaPx / largura).coerceIn(0.22f, 0.75f)
                    }
                    conteudoFerramentas(Modifier.weight(fracaoFerramentas).fillMaxHeight())
                }
            } else {
                conteudoCanvas(Modifier.weight(1f - fracaoFerramentas).fillMaxWidth().background(CorFundoEditor))
                AlcaArrastavel(horizontal = false) { deltaPx ->
                    val altura = tamanhoCorpo.height
                    if (altura > 0) fracaoFerramentas = (fracaoFerramentas - deltaPx / altura).coerceIn(0.22f, 0.75f)
                }
                conteudoFerramentas(Modifier.weight(fracaoFerramentas).fillMaxWidth())
            }

        }
    }
    if (confirmarConclusao) {
        AlertDialog(
            onDismissRequest = { confirmarConclusao = false },
            title = { Text("Concluir edição?") },
            text = { Text("Confira o contorno antes de continuar. Você pode voltar à edição sem perder nenhum ajuste.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarConclusao = false
                    val contornoAjustado = contornoInicial.copy(
                        externo = verticesPx.map { p -> Ponto(xMm = p.x / pxPorMm, yMm = p.y / pxPorMm) },
                        blocos = emptyList(),
                    )
                    onConfirmado(contornoAjustado)
                }) { Text("Concluir e continuar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarConclusao = false }) { Text("Voltar à edição") }
            },
        )
    }
}

@Composable
private fun PainelSelecaoNos(
    modo: ModoEdicao,
    quantidadeSelecionada: Int,
    tamanhoSelecaoDp: Float,
    onTamanhoSelecaoChange: (Float) -> Unit,
    removerDaSelecao: Boolean,
    onRemoverDaSelecaoChange: (Boolean) -> Unit,
    passoAjusteMm: Float,
    onPassoAjusteChange: (Float) -> Unit,
    onMover: (Float, Float) -> Unit,
    toleranciaSuavizacaoMm: Float,
    onToleranciaChange: (Float) -> Unit,
    onSuavizar: () -> Unit,
    onLimpar: () -> Unit,
    onExcluir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val instrucao = when (modo) {
        ModoEdicao.SELECIONAR_TOQUE -> "Toque nos nós para incluí-los ou removê-los."
        ModoEdicao.SELECIONAR_PINCEL -> "Mantenha o dedo pressionado e arraste sobre os nós."
        else -> "Arraste uma caixa ao redor dos nós desejados."
    }
    val usaCursorCircular = modo == ModoEdicao.SELECIONAR_TOQUE || modo == ModoEdicao.SELECIONAR_PINCEL

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CardGrupoPainelFerramenta(
            titulo = if (quantidadeSelecionada > 0) "$quantidadeSelecionada nós selecionados" else "Modo de seleção",
            legenda = instrucao,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                BotaoPainelComLegenda(
                    icone = Icons.Filled.Add,
                    titulo = "Somar",
                    legenda = "Adicionar nós",
                    ativo = !removerDaSelecao,
                    cor = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f),
                ) { onRemoverDaSelecaoChange(false) }
                BotaoPainelComLegenda(
                    icone = Icons.Filled.Remove,
                    titulo = "Subtrair",
                    legenda = "Remover nós",
                    ativo = removerDaSelecao,
                    cor = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f),
                ) { onRemoverDaSelecaoChange(true) }
            }
        }

        if (usaCursorCircular) {
            CardGrupoPainelFerramenta(
                titulo = "Área do cursor",
                legenda = "Define o alcance usado no toque ou no arrasto.",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Slider(
                        value = tamanhoSelecaoDp,
                        onValueChange = onTamanhoSelecaoChange,
                        valueRange = 18f..72f,
                        modifier = Modifier.weight(1f).height(26.dp),
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CorFundoCartaoItemAtivo,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CorDestaque.copy(alpha = 0.25f)),
                    ) {
                        Text(
                            "${tamanhoSelecaoDp.toInt()} dp",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CorDestaque,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        )
                    }
                }
            }
        }

        if (quantidadeSelecionada > 0) {
            CardGrupoPainelFerramenta(
                titulo = "Ajuste fino",
                legenda = "Defina a distância e mova a seleção pelas setas.",
            ) {
                ControleAjusteFinoOitoDirecoes(
                    passoMm = passoAjusteMm,
                    onPassoChange = onPassoAjusteChange,
                    onMover = onMover,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            CardGrupoPainelFerramenta(
                titulo = "Suavização",
                legenda = "Reduz pontos mantendo o contorno dentro do desvio escolhido.",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Slider(
                        value = toleranciaSuavizacaoMm,
                        onValueChange = onToleranciaChange,
                        valueRange = 0.1f..5f,
                        modifier = Modifier.weight(1f).height(26.dp),
                    )
                    Text(
                        "${"%.1f".format(toleranciaSuavizacaoMm)} mm",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CorTextoItem,
                    )
                }
                BotaoPainelComLegenda(
                    icone = Icons.Filled.AutoAwesome,
                    titulo = "Suavizar seleção",
                    legenda = "Processar os $quantidadeSelecionada nós selecionados",
                    ativo = false,
                    habilitado = quantidadeSelecionada > 1,
                    cor = Color(0xFF047857),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSuavizar,
                )
            }

            CardGrupoPainelFerramenta(
                titulo = "Ações da seleção",
                legenda = "Limpe a marcação ou apague os nós selecionados.",
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    BotaoPainelComLegenda(
                        icone = Icons.Filled.CropFree,
                        titulo = "Limpar",
                        legenda = "Desmarcar nós",
                        ativo = false,
                        cor = Color(0xFF64748B),
                        modifier = Modifier.weight(1f),
                        onClick = onLimpar,
                    )
                    BotaoPainelComLegenda(
                        icone = Icons.Filled.Delete,
                        titulo = "Excluir",
                        legenda = "Apagar nós",
                        ativo = false,
                        cor = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f),
                        onClick = onExcluir,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardGrupoPainelFerramenta(
    titulo: String,
    legenda: String,
    modifier: Modifier = Modifier,
    conteudo: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CorBrancoPuro,
        border = androidx.compose.foundation.BorderStroke(1.dp, CorBordaPaleta),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(titulo, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CorTextoItem)
            Text(
                legenda,
                fontSize = 8.5.sp,
                lineHeight = 10.sp,
                color = CorTextoItem.copy(alpha = 0.62f),
            )
            conteudo()
        }
    }
}

@Composable
private fun BotaoPainelComLegenda(
    icone: ImageVector,
    titulo: String,
    legenda: String,
    ativo: Boolean,
    cor: Color,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (ativo) cor.copy(alpha = 0.11f) else CorFundoEditor)
            .border(
                width = if (ativo) 1.5.dp else 1.dp,
                color = if (ativo) cor.copy(alpha = 0.5f) else CorBordaPaleta,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = icone,
            contentDescription = titulo,
            tint = if (habilitado) cor else CorTextoItem.copy(alpha = 0.28f),
            modifier = Modifier.size(19.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                titulo,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (habilitado) CorTextoItem else CorTextoItem.copy(alpha = 0.35f),
                maxLines = 1,
            )
            Text(
                legenda,
                fontSize = 7.5.sp,
                lineHeight = 8.5.sp,
                color = CorTextoItem.copy(alpha = if (habilitado) 0.58f else 0.28f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ControleAjusteFinoOitoDirecoes(
    passoMm: Float,
    onPassoChange: (Float) -> Unit,
    onMover: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val passo = passoMm.coerceIn(0.1f, 5f)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Passo", fontSize = 9.sp, color = CorTextoItem)
            BotaoPasso("−") { onPassoChange((passo - 0.1f).coerceAtLeast(0.1f)) }
            Surface(shape = RoundedCornerShape(50), color = CorFundoEditor, border = androidx.compose.foundation.BorderStroke(1.dp, CorBordaPaleta)) {
                Text("${"%.1f".format(passo)} mm", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CorTextoItem, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
            }
            BotaoPasso("+") { onPassoChange((passo + 0.1f).coerceAtMost(5f)) }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                BotaoDirecao("↖") { onMover(-passo, -passo) }
                BotaoDirecao("↑") { onMover(0f, -passo) }
                BotaoDirecao("↗") { onMover(passo, -passo) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                BotaoDirecao("←") { onMover(-passo, 0f) }
                BotaoDirecao("•", habilitado = false) {}
                BotaoDirecao("→") { onMover(passo, 0f) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                BotaoDirecao("↙") { onMover(-passo, passo) }
                BotaoDirecao("↓") { onMover(0f, passo) }
                BotaoDirecao("↘") { onMover(passo, passo) }
            }
        }
    }
}

@Composable
private fun BotaoDirecao(simbolo: String, habilitado: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier.size(32.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) { Text(simbolo, fontSize = 14.sp, textAlign = TextAlign.Center) }
}

@Composable
private fun BotaoPasso(simbolo: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(26.dp),
        shape = RoundedCornerShape(50),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) { Text(simbolo, fontSize = 12.sp) }
}

@Composable
private fun CapsulaCompacta(texto: String, ativa: Boolean, onClick: () -> Unit) {
    Text(
        text = texto,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = if (ativa) Color.White else CorTextoItem,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (ativa) CorDestaque else CorFundoEditor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
    )
}

/** Trilho de MODOS à esquerda — estilo caixa de ferramentas do CorelDraw (print de referência
 * do Tiago: ícones lisos, sem rótulo, empilhados bem juntos, sem caixa colorida grande). Cada
 * botão troca o [ModoEdicao] ativo direto, sem abrir painel nenhum. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrilhoModos(
    modo: ModoEdicao,
    onSelecionar: (ModoEdicao) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxHeight()
            .width(92.dp)
            .background(CorBrancoPuro)
            .border(width = 1.dp, color = CorBordaPaleta)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        maxItemsInEachRow = 2,
    ) {
        BotaoTrilho("✋", modo == ModoEdicao.MOVER) { onSelecionar(ModoEdicao.MOVER) }
        BotaoTrilho("🖐", modo == ModoEdicao.PAN) { onSelecionar(ModoEdicao.PAN) }
        BotaoTrilho("⬚", modo == ModoEdicao.SELECIONAR) { onSelecionar(ModoEdicao.SELECIONAR) }
        BotaoTrilho("⊙", modo == ModoEdicao.SELECIONAR_TOQUE) { onSelecionar(ModoEdicao.SELECIONAR_TOQUE) }
        BotaoTrilho("◉", modo == ModoEdicao.SELECIONAR_PINCEL) { onSelecionar(ModoEdicao.SELECIONAR_PINCEL) }
        BotaoTrilho("➕", modo == ModoEdicao.ADICIONAR) { onSelecionar(ModoEdicao.ADICIONAR) }
        BotaoTrilho("➖", modo == ModoEdicao.REMOVER) { onSelecionar(ModoEdicao.REMOVER) }
        BotaoTrilho("🔒", modo == ModoEdicao.TRAVAR) { onSelecionar(ModoEdicao.TRAVAR) }
        BotaoTrilho("📏", modo == ModoEdicao.MEDIR_CURVA) { onSelecionar(ModoEdicao.MEDIR_CURVA) }
        BotaoTrilho("🔍", modo == ModoEdicao.ZOOM) { onSelecionar(ModoEdicao.ZOOM) }
    }
}

/** Trilho de AÇÕES à direita — mesmo estilo (ícone liso, sem rótulo, sem painel) — tudo que
 * antes ficava atrás do grupo "Transformar"/ações rápidas, agora direto num toque só. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrilhoAcoes(
    podeDesfazer: Boolean,
    podeRefazer: Boolean,
    recalculando: Boolean,
    temCamadaCopiada: Boolean,
    painelCamadasAberto: Boolean,
    paletaCorAberta: Boolean,
    onDesfazer: () -> Unit,
    onRefazer: () -> Unit,
    onRecalcular: () -> Unit,
    onResetarOriginal: () -> Unit,
    onEspelharH: () -> Unit,
    onEspelharV: () -> Unit,
    onGirarEsquerda: () -> Unit,
    onGirarDireita: () -> Unit,
    onZoomMais: () -> Unit,
    onZoomMenos: () -> Unit,
    onCamadas: () -> Unit,
    onCores: () -> Unit,
    onCopiar: () -> Unit,
    onColar: () -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxHeight()
            .width(132.dp)
            .background(CorBrancoPuro)
            .border(width = 1.dp, color = CorBordaPaleta)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        maxItemsInEachRow = 3,
    ) {
        BotaoTrilho("↶", ativo = false, habilitado = podeDesfazer, onClick = onDesfazer)
        BotaoTrilho("↷", ativo = false, habilitado = podeRefazer, onClick = onRefazer)
        BotaoTrilho("🔄", ativo = false, habilitado = !recalculando, onClick = onRecalcular)
        BotaoTrilho("↩", ativo = false, onClick = onResetarOriginal)
        BotaoTrilho("↔", ativo = false, onClick = onEspelharH)
        BotaoTrilho("↕", ativo = false, onClick = onEspelharV)
        BotaoTrilho("↺", ativo = false, onClick = onGirarEsquerda)
        BotaoTrilho("↻", ativo = false, onClick = onGirarDireita)
        BotaoTrilho("🔍+", ativo = false, onClick = onZoomMais)
        BotaoTrilho("🔍−", ativo = false, onClick = onZoomMenos)
        BotaoTrilho("▱", ativo = painelCamadasAberto, onClick = onCamadas)
        BotaoTrilho("●", ativo = paletaCorAberta, onClick = onCores)
        BotaoTrilho("⧉", ativo = false, onClick = onCopiar)
        BotaoTrilho("▣", ativo = false, habilitado = temCamadaCopiada, onClick = onColar)
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PaletaCategoriasHorizontais(
    modifier: Modifier = Modifier,
    modo: ModoEdicao,
    podeDesfazer: Boolean,
    podeRefazer: Boolean,
    recalculando: Boolean,
    paletaCorAberta: Boolean,
    telaHorizontal: Boolean,
    onSelecionarModo: (ModoEdicao) -> Unit,
    onDesfazer: () -> Unit,
    onRefazer: () -> Unit,
    onRecalcular: () -> Unit,
    onResetarOriginal: () -> Unit,
    onEspelharH: () -> Unit,
    onEspelharV: () -> Unit,
    onGirarEsquerda: () -> Unit,
    onGirarDireita: () -> Unit,
    onZoomMais: () -> Unit,
    onZoomMenos: () -> Unit,
    onResetZoom: () -> Unit,
    onCores: () -> Unit,
    onAlternarTela: () -> Unit,
    fracaoFerramentas: Float,
    onFracaoFerramentasChange: (Float) -> Unit,
    usarIA: Boolean,
    onUsarIAChange: (Boolean) -> Unit,
    onPolilinha: () -> Unit,
    onApagarTodosOsNos: () -> Unit,
    desenhandoLivreAtivo: Boolean,
) {
    val categoriaDoModo = when (modo) {
        ModoEdicao.PAN, ModoEdicao.ZOOM -> CategoriaPaleta.VISUALIZACAO
        ModoEdicao.BEZIER -> CategoriaPaleta.EDICAO
        ModoEdicao.LINHA_EIXO -> CategoriaPaleta.TRANSFORMAR
        else -> CategoriaPaleta.SELECAO
    }
    var categoriaAtiva by remember(modo) { mutableStateOf(categoriaDoModo) }
    val rolagemFerramentas = rememberScrollState()
    LaunchedEffect(categoriaAtiva) {
        rolagemFerramentas.scrollTo(0)
    }
    // Altura flexível (recebida via `modifier`) em vez de fixa — pedido do Tiago: "fico com
    // espaços vazios e as ferramentas sem espaço de visualização" (ver fracaoFerramentas em
    // ValidacaoContornoScreen).
    Column(
        modifier = modifier
            .background(CorBrancoPuro)
            .border(width = 1.dp, color = CorBordaPaleta)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            maxItemsInEachRow = 3,
        ) {
            CategoriaPaleta.entries.forEach { categoria ->
                val titulo = when (categoria) {
                    CategoriaPaleta.SELECAO -> "Seleção"
                    CategoriaPaleta.EDICAO -> "Edição"
                    CategoriaPaleta.TRANSFORMAR -> "Transformação"
                    CategoriaPaleta.VISUALIZACAO -> "Visualização"
                    CategoriaPaleta.CORES -> "Configuração"
                }
                AbaCategoriaPaleta(
                    titulo = titulo,
                    ativa = categoriaAtiva == categoria,
                    modifier = Modifier.weight(1f),
                ) { categoriaAtiva = categoria }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = CorBrancoPuro,
            border = androidx.compose.foundation.BorderStroke(1.dp, CorBordaPaleta),
        ) {
            // Pedido do Tiago: "o botão de menos de remover nós sumiu" — com 8 ícones numa
            // categoria (ex.: Seleção, Transformar) e só 5 por linha, a 2ª linha (Remover,
            // Travar, Medir curva) ficava cortada pela altura FIXA do painel, que não tinha
            // espaço de sobra nem rolagem — por isso "sumia" mesmo o botão existindo no código.
            // verticalScroll aqui garante que nenhum ícone fique inacessível, mesmo que uma
            // categoria futura ganhe ainda mais ferramentas.
            FlowRow(
                modifier = Modifier.fillMaxSize().verticalScroll(rolagemFerramentas).padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 3,
            ) {
                when (categoriaAtiva) {
                    CategoriaPaleta.SELECAO -> {
                        BotaoIconePaleta(Icons.Filled.OpenWith, "Mover", modo == ModoEdicao.MOVER, cor = Color(0xFFF59E0B), onLongPress = { onSelecionarModo(ModoEdicao.MOVER) }) { onSelecionarModo(ModoEdicao.MOVER) }
                        BotaoIconePaleta(Icons.Filled.CropFree, "Selecionar área", modo == ModoEdicao.SELECIONAR, cor = Color(0xFF7C3AED), onLongPress = { onSelecionarModo(ModoEdicao.SELECIONAR) }) { onSelecionarModo(ModoEdicao.SELECIONAR) }
                        BotaoIconePaleta(Icons.Filled.TouchApp, "Selecionar por toque", modo == ModoEdicao.SELECIONAR_TOQUE, cor = Color(0xFF2563EB), onLongPress = { onSelecionarModo(ModoEdicao.SELECIONAR_TOQUE) }) { onSelecionarModo(ModoEdicao.SELECIONAR_TOQUE) }
                        BotaoIconePaleta(Icons.Filled.Swipe, "Selecionar por arrasto", modo == ModoEdicao.SELECIONAR_PINCEL, cor = Color(0xFF0891B2), onLongPress = { onSelecionarModo(ModoEdicao.SELECIONAR_PINCEL) }) { onSelecionarModo(ModoEdicao.SELECIONAR_PINCEL) }
                        BotaoIconePaleta(Icons.Filled.Add, "Adicionar nó", modo == ModoEdicao.ADICIONAR, cor = Color(0xFF16A34A), onLongPress = { onSelecionarModo(ModoEdicao.ADICIONAR) }) { onSelecionarModo(ModoEdicao.ADICIONAR) }
                        BotaoIconePaleta(Icons.Filled.Remove, "Remover nó", modo == ModoEdicao.REMOVER, cor = Color(0xFFDC2626), onLongPress = { onSelecionarModo(ModoEdicao.REMOVER) }) { onSelecionarModo(ModoEdicao.REMOVER) }
                        BotaoIconePaleta(Icons.Filled.Lock, "Travar nó", modo == ModoEdicao.TRAVAR, cor = Color(0xFFEA580C), onLongPress = { onSelecionarModo(ModoEdicao.TRAVAR) }) { onSelecionarModo(ModoEdicao.TRAVAR) }
                        BotaoIconePaleta(Icons.Filled.Straighten, "Medir curva", modo == ModoEdicao.MEDIR_CURVA, cor = Color(0xFF0D9488), onLongPress = { onSelecionarModo(ModoEdicao.MEDIR_CURVA) }) { onSelecionarModo(ModoEdicao.MEDIR_CURVA) }
                        BotaoIconePaleta(Icons.Filled.LinearScale, "Régua (reta entre pontos)", modo == ModoEdicao.MEDIR_RETA, cor = Color(0xFFCA8A04), onLongPress = { onSelecionarModo(ModoEdicao.MEDIR_RETA) }) { onSelecionarModo(ModoEdicao.MEDIR_RETA) }
                    }
                    CategoriaPaleta.TRANSFORMAR -> {
                        BotaoIconePaleta(Icons.AutoMirrored.Filled.Undo, "Desfazer", false, habilitado = podeDesfazer, cor = Color(0xFF4F46E5), onClick = onDesfazer)
                        BotaoIconePaleta(Icons.AutoMirrored.Filled.Redo, "Refazer", false, habilitado = podeRefazer, cor = Color(0xFF4F46E5), onClick = onRefazer)
                        BotaoIconePaleta(Icons.Filled.CenterFocusStrong, "Recalcular", false, habilitado = !recalculando, cor = Color(0xFF0D9488), onClick = onRecalcular)
                        BotaoIconePaleta(Icons.Filled.AutoAwesome, if (usarIA) "IA ligada (Recalcular usa Claude)" else "IA desligada (Recalcular usa OpenCV)", usarIA, cor = Color(0xFF9333EA), onClick = { onUsarIAChange(!usarIA) })
                        BotaoIconePaleta(Icons.Filled.Refresh, "Restaurar original", false, cor = Color(0xFF2563EB), onClick = onResetarOriginal)
                        BotaoIconePaleta(Icons.Filled.Flip, "Espelhar horizontal", false, cor = Color(0xFF9333EA), onClick = onEspelharH)
                        BotaoIconePaleta(Icons.Filled.SwapVert, "Espelhar vertical", false, cor = Color(0xFF9333EA), onClick = onEspelharV)
                        BotaoIconePaleta(Icons.Filled.RotateLeft, "Girar à esquerda", false, cor = Color(0xFFF97316), onClick = onGirarEsquerda)
                        BotaoIconePaleta(Icons.Filled.RotateRight, "Girar à direita", false, cor = Color(0xFFF97316), onClick = onGirarDireita)
                        BotaoIconePaleta(Icons.Filled.SwapVert, "Linha eixo (toque 2 pontos)", modo == ModoEdicao.LINHA_EIXO, cor = Color(0xFF0D9488), onLongPress = { onSelecionarModo(ModoEdicao.LINHA_EIXO) }) { onSelecionarModo(ModoEdicao.LINHA_EIXO) }
                    }
                    CategoriaPaleta.VISUALIZACAO -> {
                        BotaoIconePaleta(Icons.Filled.PanTool, "Pan", modo == ModoEdicao.PAN, cor = Color(0xFFF59E0B), onLongPress = { onSelecionarModo(ModoEdicao.PAN) }) { onSelecionarModo(ModoEdicao.PAN) }
                        BotaoIconePaleta(Icons.Filled.ZoomIn, "Zoom", modo == ModoEdicao.ZOOM, cor = Color(0xFF0891B2), onLongPress = { onSelecionarModo(ModoEdicao.ZOOM) }) { onSelecionarModo(ModoEdicao.ZOOM) }
                        BotaoIconePaleta(Icons.Filled.ZoomIn, "Aumentar zoom", false, cor = Color(0xFF16A34A), onClick = onZoomMais)
                        BotaoIconePaleta(Icons.Filled.ZoomOut, "Diminuir zoom", false, cor = Color(0xFFDC2626), onClick = onZoomMenos)
                        BotaoIconePaleta(Icons.Filled.CenterFocusStrong, "Enquadrar", false, cor = Color(0xFF4F46E5), onClick = onResetZoom)
                    }
                    CategoriaPaleta.CORES -> {
                        BotaoIconePaleta(Icons.Filled.Palette, "Cores", paletaCorAberta, cor = Color(0xFFEC4899), onLongPress = onCores, onClick = onCores)
                        BotaoIconePaleta(
                            Icons.Filled.ScreenRotation,
                            if (telaHorizontal) "Voltar ao retrato" else "Girar tela",
                            telaHorizontal,
                            cor = Color(0xFF16A34A),
                            onClick = onAlternarTela,
                        )
                        // Pedido do Tiago: "crie uma barra arrastável para poder ajustar o
                        // tamanho da barra de visualização e a barra de configurações... deixe
                        // essa função em configurações" — mesmo ajuste da alça entre as duas
                        // áreas, só que por slider, mais fácil de afinar com precisão.
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                            Text(
                                "Tamanho da área de ferramentas: ${(fracaoFerramentas * 100).roundToInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CorTextoItem.copy(alpha = 0.7f),
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Slider(
                                    value = fracaoFerramentas,
                                    onValueChange = onFracaoFerramentasChange,
                                    valueRange = 0.22f..0.75f,
                                    modifier = Modifier.weight(1f).height(24.dp),
                                )
                                OutlinedButton(
                                    onClick = { onFracaoFerramentasChange(0.42f) },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                ) { Text("Resetar", fontSize = 10.sp) }
                            }
                        }
                    }
                    // Pedido do Tiago: "crie essa nova paleta chamada edição, com a ferramenta
                    // bezier, polilinhas, curva".
                    CategoriaPaleta.EDICAO -> {
                        BotaoIconePaleta(Icons.Filled.Edit, "Polilinha (apaga tudo e desenha do zero)", desenhandoLivreAtivo, cor = Color(0xFF16A34A), onClick = onPolilinha)
                        BotaoIconePaleta(Icons.Filled.AutoGraph, "Curva (arraste uma aresta)", modo == ModoEdicao.BEZIER, cor = Color(0xFFEC4899), onLongPress = { onSelecionarModo(ModoEdicao.BEZIER) }) { onSelecionarModo(ModoEdicao.BEZIER) }
                        BotaoIconePaleta(Icons.Filled.DeleteSweep, "Apagar todos os nós", false, cor = Color(0xFFDC2626), onClick = onApagarTodosOsNos)
                    }
                }
            }
        }
    }
}

@Composable
private fun AbaCategoriaPaleta(
    titulo: String,
    ativa: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    // Pedido do Tiago: "retire essas cápsulas e transforme em cards com cantos pouco
    // arredondados seguindo o padrão do programa" — mesmo estilo de card usado em
    // BotaoIconePaleta (RoundedCornerShape pequeno + borda quando ativo), em vez da pílula.
    Box(
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (ativa) CorFundoCartaoItemAtivo else CorFundoEditor)
            .border(
                width = if (ativa) 1.5.dp else 1.dp,
                color = if (ativa) CorDestaque.copy(alpha = 0.45f) else CorBordaPaleta,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = titulo.uppercase(),
            fontSize = 8.sp,
            lineHeight = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (ativa) CorDestaque else CorTextoItem.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun CardPaletaHorizontal(titulo: String, conteudo: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CorBrancoPuro,
        border = androidx.compose.foundation.BorderStroke(1.dp, CorBordaPaleta),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = titulo,
                modifier = Modifier.width(74.dp),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = CorTextoItem.copy(alpha = 0.62f),
                maxLines = 1,
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                conteudo()
            }
        }
    }
}

@Composable
private fun AmostraCor(cor: Color, selecionada: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(cor)
            .border(width = if (selecionada) 3.dp else 1.dp, color = if (selecionada) CorTextoItem else CorBordaPaleta, shape = CircleShape)
            .clickable(onClick = onClick),
    )
}

/** Ferramenta em cápsula cinza, com ícone colorido e nome sempre visível. */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun BotaoIconePaleta(
    icone: ImageVector,
    descricao: String,
    ativo: Boolean,
    habilitado: Boolean = true,
    cor: Color = CorDestaque,
    onLongPress: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    var mostrarDica by remember { mutableStateOf(false) }
    val nomeVisivel = descricao.substringBefore(" (")
    LaunchedEffect(mostrarDica) {
        if (mostrarDica) {
            delay(1800)
            mostrarDica = false
        }
    }
    val corIcone = when {
        !habilitado -> CorTextoItem.copy(alpha = 0.26f)
        ativo -> cor
        else -> cor.copy(alpha = 0.72f)
    }
    Column(
        modifier = Modifier
            .width(100.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (ativo) CorFundoCartaoItemAtivo else CorFundoEditor)
            .border(
                width = if (ativo) 1.5.dp else 0.dp,
                color = if (ativo) cor.copy(alpha = 0.45f) else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
            )
            .combinedClickable(
                enabled = habilitado,
                onClick = onClick,
                onLongClick = {
                    mostrarDica = true
                    onLongPress?.invoke()
                },
            )
            .padding(horizontal = 4.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icone,
            contentDescription = descricao,
            tint = corIcone,
            modifier = Modifier.size(if (ativo) 20.dp else 19.dp),
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = nomeVisivel,
            color = if (habilitado) CorTextoItem.copy(alpha = 0.82f) else CorTextoItem.copy(alpha = 0.34f),
            fontSize = 7.5.sp,
            fontWeight = if (ativo) FontWeight.Bold else FontWeight.Medium,
            lineHeight = 8.5.sp,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
        if (mostrarDica) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -54),
                properties = PopupProperties(focusable = false),
                onDismissRequest = { mostrarDica = false },
            ) {
                Surface(
                    color = Color(0xEE0F172A),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp,
                ) {
                    Text(
                        text = descricao,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BotaoTravaBloco(
    rotulo: String,
    travado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val fundo = if (travado) Color(0xFFFFEDD5) else Color(0xFFDCFCE7)
    val borda = if (travado) Color(0xFFEA580C) else Color(0xFF16A34A)
    val textoEstado = if (travado) "TRAVADO" else "LIVRE"
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(fundo)
            .border(1.5.dp, borda, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = if (travado) Icons.Filled.Lock else Icons.Filled.LockOpen,
            contentDescription = "$rotulo: $textoEstado",
            tint = borda,
            modifier = Modifier.size(23.dp),
        )
        Column {
            Text(rotulo, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CorTextoItem, maxLines = 1)
            Text(textoEstado, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = borda, maxLines = 1)
        }
    }
}

@Composable
private fun BotaoTrilho(simbolo: String, ativo: Boolean, habilitado: Boolean = true, onClick: () -> Unit) {
    val cor = when {
        !habilitado -> CorTextoItem.copy(alpha = 0.28f)
        ativo -> CorDestaque
        else -> CorTextoItem
    }
    val forma = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(forma)
            .background(if (ativo) CorFundoCartaoItemAtivo else Color.Transparent)
            .clickable(enabled = habilitado, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(simbolo, fontSize = 16.sp, color = cor)
    }
}

/** Agrupa os controles de UM contexto (ex.: tudo do modo Zoom, tudo do modo Pan) dentro de um
 * cartão com borda própria — pedido do Tiago: "ajuste para sempre mostrar cards organizados ao
 * invés de cápsulas soltas na tela". Antes cada grupo de controles (ex.: "Zoom livre"/"Zoom por
 * área"/"250%"/"Resetar zoom") ficava solto direto na barra de propriedades; agora cada grupo
 * contextual vira seu próprio cartão visível, mesmo com vários grupos lado a lado. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CartaoFerramenta(modifier: Modifier = Modifier, conteudo: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = CorFundoEditor,
        border = androidx.compose.foundation.BorderStroke(1.dp, CorBordaPaleta),
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) { conteudo() }
    }
}

/** Card contextual no mesmo padrão visual das ferramentas do editor de etiquetas. */
@Composable
private fun CardOpcaoVisualizacao(
    icone: ImageVector,
    titulo: String,
    selecionado: Boolean,
    cor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selecionado) CorFundoCartaoItemAtivo else CorFundoEditor)
            .border(
                width = if (selecionado) 1.5.dp else 1.dp,
                color = if (selecionado) cor.copy(alpha = 0.45f) else CorBordaPaleta,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(imageVector = icone, contentDescription = titulo, tint = cor, modifier = Modifier.size(19.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = titulo,
            fontSize = 8.5.sp,
            lineHeight = 10.sp,
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
            color = CorTextoItem.copy(alpha = 0.82f),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

/** Aba compacta usada na barra de propriedades (ex.: Preto e branco, Zoom livre/Zoom por área)
 * — fundo roxo-claro quando [selecionada]. */
@Composable
private fun AbaFerramenta(titulo: String, subtitulo: String?, selecionada: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selecionada) CorFundoCartaoItemAtivo else CorFundoCartaoItem)
            .border(width = 1.dp, color = if (selecionada) CorDestaque.copy(alpha = 0.45f) else CorBordaPaleta, shape = RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CorTextoItem)
        if (subtitulo != null) {
            Text(subtitulo, fontSize = 12.sp, color = CorTextoItem.copy(alpha = 0.7f), maxLines = 1)
        }
    }
}

/** Alça arrastável entre a prancheta e o painel de ferramentas — pedido do Tiago: "crie uma
 * barra arrastável para poder ajustar o tamanho da barra de visualização e a barra de
 * configurações da maneira que eu precisar". [horizontal] = true quando a divisão é lado a lado
 * (tela girada, arrasta pra os lados); false quando é embaixo/em cima (retrato, arrasta pra
 * cima/baixo). Devolve o deslocamento em PX pro chamador converter em fração. */
@Composable
private fun AlcaArrastavel(horizontal: Boolean, aoArrastarPx: (Float) -> Unit) {
    Box(
        modifier = (if (horizontal) Modifier.width(16.dp).fillMaxHeight() else Modifier.fillMaxWidth().height(16.dp))
            .background(CorFundoEditor)
            .pointerInput(horizontal) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        aoArrastarPx(if (horizontal) dragAmount.x else dragAmount.y)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (horizontal) {
            Box(modifier = Modifier.width(4.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(CorBordaPaleta))
        } else {
            Box(modifier = Modifier.width(44.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(CorBordaPaleta))
        }
    }
}

/** Barra de rolagem arrastável — mostra quanto da imagem ampliada está visível ([fracaoVisivel],
 * vira o tamanho do "polegar") e onde ([fracaoPosicao], 0 = início, 1 = fim), e arrastar o
 * polegar chama [aoArrastarFracao] com o deslocamento em FRAÇÃO do comprimento da própria barra
 * (não em pixels) — quem chama converte essa fração pro alcance de pan real. Pedido do Tiago:
 * "arraste de uma barra", como alternativa a arrastar a imagem direto com o dedo. */
@Composable
private fun BarraRolagem(
    horizontal: Boolean,
    fracaoVisivel: Float,
    fracaoPosicao: Float,
    modifier: Modifier = Modifier,
    aoArrastarFracao: (Float) -> Unit,
) {
    var comprimentoTrackPx by remember { mutableStateOf(0f) }
    val fracaoVisivelClamp = fracaoVisivel.coerceIn(0.08f, 1f)
    val fracaoPosicaoClamp = fracaoPosicao.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .then(if (horizontal) Modifier.height(10.dp) else Modifier.width(10.dp))
            .onSizeChanged { comprimentoTrackPx = if (horizontal) it.width.toFloat() else it.height.toFloat() }
            .background(Color(0x33000000), RoundedCornerShape(5.dp)),
    ) {
        val deslocamentoPx = comprimentoTrackPx * fracaoPosicaoClamp * (1 - fracaoVisivelClamp)
        val thumbModifier = if (horizontal) {
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fracaoVisivelClamp)
                .offset { IntOffset(deslocamentoPx.toInt(), 0) }
        } else {
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(fracaoVisivelClamp)
                .offset { IntOffset(0, deslocamentoPx.toInt()) }
        }
        Box(
            modifier = thumbModifier
                .background(Color(0xFF334155), RoundedCornerShape(5.dp))
                .pointerInput(horizontal, comprimentoTrackPx) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        if (comprimentoTrackPx > 0f) {
                            val delta = if (horizontal) dragAmount.x else dragAmount.y
                            aoArrastarFracao(delta / comprimentoTrackPx)
                        }
                    }
                },
        )
    }
}

/** Desenha a cena (foto corrigida + contorno + vértices) — função compartilhada entre o canvas
 * principal e a lupa de zoom, pra garantir que os dois mostram exatamente a mesma coisa (só
 * muda a transformação aplicada por fora, ver o `scale`/`translate` no chamador). [indicesTravados]
 * pinta o vértice de cinza em vez de verde; [indiceSelecionadoMedicao] destaca o primeiro ponto
 * escolhido no modo Medir curva. */
private fun DrawScope.desenharCena(
    fotoCorrigida: Bitmap?,
    verticesPx: List<Offset>,
    escala: Float,
    colorFilter: ColorFilter? = null,
    indicesTravados: Set<Int> = emptySet(),
    indiceSelecionadoMedicao: Int? = null,
    verticeSelecionado: Int? = null,
    indicesSelecionados: Set<Int> = emptySet(),
    corContorno: Color = Color(0xFF22C55E),
    fechado: Boolean = true,
    zoom: Float = 1f,
) {
    fotoCorrigida?.let { bmp ->
        drawImage(
            image = bmp.asImageBitmap(),
            dstSize = IntSize((bmp.width * escala).toInt(), (bmp.height * escala).toInt()),
            colorFilter = colorFilter,
        )
    }

    if (verticesPx.isNotEmpty()) {
        val caminho = Path().apply {
            verticesPx.forEachIndexed { i, p ->
                val pt = Offset(p.x * escala, p.y * escala)
                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
            }
            if (fechado) close()
        }
        // Pedido do Tiago: "ao fechar a geometria vamos colocar uma paleta de cores" — quando
        // fechado, preenche com a cor escolhida (bem translúcida, pra não esconder a foto por
        // baixo) além do traço; enquanto ainda sendo desenhado (fechado=false), só o traço.
        if (fechado) {
            drawPath(caminho, color = corContorno.copy(alpha = 0.25f))
        }
        // Pedido do Tiago: "com o zoom as bolinhas ficam todas juntas, impossível de editar" —
        // o Canvas inteiro é desenhado dentro de um scale(zoom) (ver pointerInput acima), então
        // um raio fixo cresce junto com o zoom e as bolinhas colidem umas nas outras bem antes
        // dos vértices em si (que SIM se afastam com o zoom). Dividindo por [zoom] aqui, a
        // bolinha mantém um tamanho constante NA TELA, então zoom in sempre ajuda a separar
        // vértices próximos, nunca piora.
        val raioVertice = 18f / zoom
        drawPath(caminho, color = corContorno, style = Stroke(width = 5f / zoom))
        verticesPx.forEachIndexed { i, p ->
            val cor = when {
                i in indicesSelecionados -> Color(0xFFDC2626)
                i == indiceSelecionadoMedicao -> Color(0xFFEAB308)
                i == verticeSelecionado -> Color(0xFF2563EB)
                i in indicesTravados -> Color(0xFF64748B)
                i == 0 && !fechado -> Color(0xFFEAB308)
                else -> corContorno
            }
            drawCircle(color = cor, radius = raioVertice, center = Offset(p.x * escala, p.y * escala))
        }
    }
}

/** Desembrulha um Context até achar a Activity de verdade — necessário pra mudar
 * requestedOrientation, já que LocalContext.current às vezes vem envolvido num ContextWrapper
 * (não diretamente a Activity) dependendo de como a árvore de Compose foi montada. */
private tailrec fun android.content.Context.paraActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.paraActivity()
    else -> null
}

/**
 * Simplifica somente os nós selecionados. Nós fora da seleção e nós travados são âncoras
 * obrigatórias. Quando todo o contorno está selecionado, os extremos horizontal e vertical
 * viram âncoras automáticas para manter tamanho, orientação e cantos principais.
 */
internal fun simplificarTrechosSelecionados(
    vertices: List<Offset>,
    selecionados: Set<Int>,
    travados: Set<Int>,
    toleranciaPx: Float,
): List<Int> {
    if (vertices.size <= 3 || selecionados.size < 2) return vertices.indices.toList()

    val validos = vertices.indices.toSet()
    val candidatos = selecionados.intersect(validos) - travados
    if (candidatos.size < 2) return vertices.indices.toList()

    val ancoras = vertices.indices.filterTo(mutableSetOf()) { it !in candidatos }
    if (ancoras.size < 3) {
        ancoras += vertices.indices.minByOrNull { vertices[it].x } ?: 0
        ancoras += vertices.indices.maxByOrNull { vertices[it].x } ?: 0
        ancoras += vertices.indices.minByOrNull { vertices[it].y } ?: 0
        ancoras += vertices.indices.maxByOrNull { vertices[it].y } ?: 0
    }
    if (ancoras.size < 3) {
        ancoras += 0
        ancoras += vertices.size / 3
        ancoras += (vertices.size * 2) / 3
    }

    val manter = ancoras.toMutableSet()
    val ancorasOrdenadas = ancoras.sorted()
    val tolerancia = toleranciaPx.coerceAtLeast(0f)

    fun simplificarCadeia(cadeia: List<Int>, inicio: Int, fim: Int) {
        if (fim <= inicio + 1) return
        val a = vertices[cadeia[inicio]]
        val b = vertices[cadeia[fim]]
        var maiorDistancia = -1f
        var posicaoMaisDistante = -1
        for (posicao in (inicio + 1) until fim) {
            val distancia = distanciaPontoSegmento(vertices[cadeia[posicao]], a, b)
            if (distancia > maiorDistancia) {
                maiorDistancia = distancia
                posicaoMaisDistante = posicao
            }
        }
        if (posicaoMaisDistante >= 0 && maiorDistancia > tolerancia) {
            manter += cadeia[posicaoMaisDistante]
            simplificarCadeia(cadeia, inicio, posicaoMaisDistante)
            simplificarCadeia(cadeia, posicaoMaisDistante, fim)
        }
    }

    ancorasOrdenadas.indices.forEach { posicaoAncora ->
        val inicio = ancorasOrdenadas[posicaoAncora]
        val fim = ancorasOrdenadas[(posicaoAncora + 1) % ancorasOrdenadas.size]
        val cadeia = mutableListOf(inicio)
        var indice = inicio
        while (indice != fim) {
            indice = (indice + 1) % vertices.size
            cadeia += indice
        }
        simplificarCadeia(cadeia, 0, cadeia.lastIndex)
    }

    return vertices.indices.filter { it in manter }.let { mantidos ->
        if (mantidos.size >= 3) mantidos else vertices.indices.toList()
    }
}

private fun distancia(a: Offset, b: Offset): Float = hypot(a.x - b.x, a.y - b.y)

/** Distância perpendicular de [p] ao segmento [a]-[b] (não à reta infinita) — usada pra achar a
 * aresta mais próxima de um toque no modo Adicionar. */
private fun distanciaPontoSegmento(p: Offset, a: Offset, b: Offset): Float {
    val abx = b.x - a.x
    val aby = b.y - a.y
    val comprimentoAo2 = abx * abx + aby * aby
    if (comprimentoAo2 == 0f) return distancia(p, a)
    var t = ((p.x - a.x) * abx + (p.y - a.y) * aby) / comprimentoAo2
    t = t.coerceIn(0f, 1f)
    val projecao = Offset(a.x + t * abx, a.y + t * aby)
    return distancia(p, projecao)
}

/** Soma a distância (mm) ao longo do contorno, andando pelos vértices na ORDEM do polígono, de
 * [inicio] até [fim] (sentido "pra frente"/crescente, com volta ao início no fim da lista) — ver
 * ContornoExtractor/CalculoArea pra contexto de por que os vértices já vêm em mm aqui. Chamar com
 * inicio == fim devolve o perímetro total (dá a volta completa). Usado pelo modo Medir curva: o
 * comprimento "pelo outro lado" do polígono é sempre `perímetroTotal - comprimentoCaminhoMm(a, b)`. */
private fun comprimentoCaminhoMm(pontosMm: List<Ponto>, inicio: Int, fim: Int): Double {
    if (pontosMm.isEmpty()) return 0.0
    var soma = 0.0
    var i = inicio
    do {
        val prox = (i + 1) % pontosMm.size
        val a = pontosMm[i]
        val b = pontosMm[prox]
        soma += hypot(a.xMm - b.xMm, a.yMm - b.yMm)
        i = prox
    } while (i != fim)
    return soma
}
