package com.musgo.vendaseproducao.moldevetor.visao

import com.musgo.vendaseproducao.moldevetor.domain.Contorno
import com.musgo.vendaseproducao.moldevetor.domain.Ponto
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

/** Extrai o contorno da faca a partir da imagem já corrigida por [PerspectiveCorrector] — espelha
 * o passo 4 da especificação ("Processamento de imagem: o app aplica threshold adaptativo e
 * detecta os contornos"). Trabalha em cima de uma imagem onde a escala px→mm já é conhecida
 * (ver [PerspectiveCorrector.Resultado.pxPorMm]), então a conversão de unidade acontece aqui
 * mesmo, no fim — o resto do app (CalculoArea, telas) só lida com milímetros.
 *
 * Usa detecção de BORDA (Canny), não threshold de brilho global (Otsu) — testado num caso real
 * (caixa de papelão bege sobre mesa bege) onde o Otsu não tinha contraste suficiente entre peça
 * e mesa e acabava pegando sombra/textura da mesa em vez do objeto. Canny segue a linha de
 * borda em si (gradiente), que continua visível mesmo quando as cores são parecidas, desde que
 * exista alguma linha de contorno perceptível entre a peça e o fundo — ainda assim, uma peça
 * praticamente idêntica ao fundo (sem nenhuma borda visível) continua sendo um caso que NENHUM
 * threshold resolve; nesse caso a orientação pro usuário é fotografar sobre um fundo com mais
 * contraste. */
object ContornoExtractor {
    /** Área mínima de contorno, em px², pra descartar ruído (poeira, sombra, reflexo) que a
     * detecção de borda capta junto com a peça — bem menor que qualquer faca de calçado real. */
    private const val AREA_MINIMA_PX = 200.0

    /** Epsilon do approxPolyDP, como fração do perímetro — pedido do Tiago: "adicione quantos
     * pontos forem necessários para fazer os contornos de curva precisos". Douglas-Peucker
     * (o algoritmo por trás do approxPolyDP) já é adaptativo por natureza: um trecho RETO do
     * contorno vira poucos pontos (não desvia do segmento reto o bastante pra precisar de mais),
     * um trecho CURVO vira muitos pontos automaticamente (cada ponto da curva desvia da reta que
     * liga as pontas, então o algoritmo mantém inserindo pontos até a curva inteira ficar dentro
     * da tolerância). Baixar o epsilon simplesmente baixa essa tolerância, então curvas ganham
     * mais pontos em todo lugar que precisarem, sem precisar de lógica própria pra "detectar
     * curva". 0.15% (bem mais fino que o 0.5% anterior) — pontos em excesso numa reta não
     * atrapalham o cálculo de área e dá pra apagar à mão na tela de validação (modo "Remover
     * ponto"), mas uma curva SUBrepresentada vira reta errada sem solução fácil depois. */
    private const val EPSILON_FRACAO_PERIMETRO = 0.0015

    /** @param imagemCorrigida saída de [PerspectiveCorrector.corrigir].
     * @param pxPorMm escala da imagem corrigida (ver [PerspectiveCorrector.Resultado.pxPorMm]).
     * @return o contorno externo de maior área encontrado (a faca), com seus furos internos
     * (RETR_CCOMP dá só 2 níveis — externo e furos diretos, suficiente pra uma faca de corte),
     * já convertido pra milímetros; ou null se nada foi encontrado (ver especificação: "Se a
     * detecção falhar, o app informa o motivo"). */
    fun extrair(imagemCorrigida: Mat, pxPorMm: Double): Contorno? {
        val cinza = Mat()
        if (imagemCorrigida.channels() > 1) {
            Imgproc.cvtColor(imagemCorrigida, cinza, Imgproc.COLOR_BGR2GRAY)
        } else {
            imagemCorrigida.copyTo(cinza)
        }

        // O marcador ArUco (preto e branco, altíssimo contraste) fica ancorado no canto
        // superior esquerdo da imagem corrigida (ver PerspectiveCorrector.corrigir) — e
        // normalmente vem impresso numa folha com instruções/texto ao redor dele, não só o
        // quadrado de 5cm puro. Sem mascarar essa região, o próprio marcador (ou o texto da
        // folha) vira a borda de maior destaque da imagem inteira, maior até que o objeto real
        // — mascara uma área 3x o lado do marcador (cobre a folha sem precisar saber o tamanho
        // exato dela) preenchendo de branco, que não gera nenhuma borda.
        val ladoMarcadorPx = MarcadorAruco.TAMANHO_MM * pxPorMm
        val margemMascaraPx = ladoMarcadorPx * 3
        Imgproc.rectangle(
            cinza,
            Point(0.0, 0.0),
            Point(margemMascaraPx, margemMascaraPx),
            Scalar(255.0),
            -1,
        )

        val suavizada = Mat()
        Imgproc.GaussianBlur(cinza, suavizada, Size(5.0, 5.0), 0.0)

        val bordas = Mat()
        Imgproc.Canny(suavizada, bordas, 50.0, 150.0)

        // Fecha pequenas lacunas na borda detectada (sombra, reflexo, textura) antes de
        // procurar contornos fechados — Canny sozinho raramente fecha o contorno 100%.
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0))
        val bordasFechadas = Mat()
        Imgproc.dilate(bordas, bordasFechadas, kernel)

        val contornos = mutableListOf<MatOfPoint>()
        val hierarquia = Mat()
        // RETR_CCOMP: contornos externos + seus furos diretos, em 2 níveis — exatamente o que
        // Contorno.furos precisa (lista de furos do contorno externo escolhido).
        Imgproc.findContours(
            bordasFechadas,
            contornos,
            hierarquia,
            Imgproc.RETR_CCOMP,
            Imgproc.CHAIN_APPROX_SIMPLE,
        )

        if (contornos.isEmpty()) return null

        // Escolhe o contorno externo de maior área — hierarquia[i] = [next, previous, firstChild,
        // parent]; parent == -1 identifica um contorno de nível externo (não é furo de outro).
        var indiceExterno = -1
        var maiorArea = AREA_MINIMA_PX
        for (i in contornos.indices) {
            val parent = hierarquia.get(0, i)?.getOrNull(3)?.toInt() ?: -1
            if (parent != -1) continue
            val area = Imgproc.contourArea(contornos[i])
            if (area > maiorArea) {
                maiorArea = area
                indiceExterno = i
            }
        }
        if (indiceExterno == -1) return null

        val externoMm = aproximarEConverter(contornos[indiceExterno], pxPorMm)

        // Furos: só aceita contornos filhos com área entre AREA_MINIMA_PX e metade da área
        // externa — a dilatação da borda (acima) pode criar um "anel" artificial bem fino
        // (borda interna + externa da MESMA linha espessada), que apareceria como filho na
        // hierarquia sem ser um furo real; o teto de 50% é uma defesa simples contra esse
        // artefato. Ainda não testado com uma faca real com furo — ajustar se necessário.
        val furosMm = mutableListOf<List<Ponto>>()
        for (i in contornos.indices) {
            val parent = hierarquia.get(0, i)?.getOrNull(3)?.toInt() ?: -1
            if (parent != indiceExterno) continue
            val areaFuro = Imgproc.contourArea(contornos[i])
            if (areaFuro < AREA_MINIMA_PX || areaFuro > maiorArea * 0.5) continue
            furosMm.add(aproximarEConverter(contornos[i], pxPorMm))
        }

        return Contorno(externo = externoMm, furos = furosMm)
    }

    /** Reexecuta a detecção de borda, mas restrita à região que o usuário já indicou arrastando
     * o contorno pra um retângulo/polígono grosseiro em volta da peça certa — pedido do Tiago:
     * "recalcular de acordo com objeto que os vértices já estão circundando, ajustando curvas e
     * retas". Em vez de o usuário reajustar vértice por vértice, o app encontra a borda real
     * (reta ou curva) dentro da área já indicada, igual a um "snap pra borda" guiado.
     *
     * @param poligonoGuiaPx vértices atuais (em px da imagem corrigida) que o usuário já
     * posicionou em volta do objeto — não precisa ser preciso, só precisa cercar a peça.
     * @return os vértices do contorno refinado (mesma escala px de [poligonoGuiaPx]), ou null
     * se nenhum contorno razoável foi encontrado dentro da região (ex.: o guia não cerca nada
     * com borda visível). */
    fun refinarDentroDoGuia(imagemCorrigida: Mat, poligonoGuiaPx: List<Point>): List<Point>? {
        if (poligonoGuiaPx.size < 3) return null

        val minX = poligonoGuiaPx.minOf { it.x }
        val maxX = poligonoGuiaPx.maxOf { it.x }
        val minY = poligonoGuiaPx.minOf { it.y }
        val maxY = poligonoGuiaPx.maxOf { it.y }

        // Expande o retângulo delimitador do guia em 25% pra cada lado — margem pra pegar a
        // borda real mesmo que o usuário tenha desenhado o guia um pouco pra dentro do objeto.
        val margemX = (maxX - minX) * 0.25
        val margemY = (maxY - minY) * 0.25

        val left = (minX - margemX).toInt().coerceIn(0, imagemCorrigida.cols() - 1)
        val top = (minY - margemY).toInt().coerceIn(0, imagemCorrigida.rows() - 1)
        val right = (maxX + margemX).toInt().coerceIn(left + 1, imagemCorrigida.cols())
        val bottom = (maxY + margemY).toInt().coerceIn(top + 1, imagemCorrigida.rows())

        val recorte = Mat(imagemCorrigida, Rect(left, top, right - left, bottom - top))

        val cinza = Mat()
        if (recorte.channels() > 1) {
            Imgproc.cvtColor(recorte, cinza, Imgproc.COLOR_BGR2GRAY)
        } else {
            recorte.copyTo(cinza)
        }
        val suavizada = Mat()
        Imgproc.GaussianBlur(cinza, suavizada, Size(5.0, 5.0), 0.0)
        val bordas = Mat()
        Imgproc.Canny(suavizada, bordas, 50.0, 150.0)
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0))
        val bordasFechadas = Mat()
        Imgproc.dilate(bordas, bordasFechadas, kernel)

        val contornos = mutableListOf<MatOfPoint>()
        val hierarquia = Mat()
        // RETR_EXTERNAL: só a borda externa dentro do recorte — este recálculo ajusta o
        // contorno externo guiado pelo usuário, não procura furos (esses continuam vindos da
        // detecção original feita em extrair()).
        Imgproc.findContours(bordasFechadas, contornos, hierarquia, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        if (contornos.isEmpty()) return null

        // Escolhe, entre os contornos encontrados no recorte, o de área mais PARECIDA com a do
        // guia do usuário — não necessariamente o maior: perto da borda do recorte pode sobrar
        // pedaço de mesa/sombra que também vira um contorno grande.
        val areaGuia = Imgproc.contourArea(MatOfPoint(*poligonoGuiaPx.toTypedArray()))
        val melhor = contornos.minByOrNull { c -> abs(Imgproc.contourArea(c) - areaGuia) } ?: return null
        if (Imgproc.contourArea(melhor) < AREA_MINIMA_PX) return null

        val contorno2f = MatOfPoint2f(*melhor.toArray())
        val perimetro = Imgproc.arcLength(contorno2f, true)
        val aproximado = MatOfPoint2f()
        Imgproc.approxPolyDP(contorno2f, aproximado, EPSILON_FRACAO_PERIMETRO * perimetro, true)

        // Devolve em coordenadas da imagem ORIGINAL (soma o deslocamento do recorte).
        return aproximado.toList().map { p -> Point(p.x + left, p.y + top) }
    }

    private fun aproximarEConverter(contorno: MatOfPoint, pxPorMm: Double): List<Ponto> {
        val contorno2f = MatOfPoint2f(*contorno.toArray())
        val perimetro = Imgproc.arcLength(contorno2f, true)
        val aproximado = MatOfPoint2f()
        Imgproc.approxPolyDP(contorno2f, aproximado, EPSILON_FRACAO_PERIMETRO * perimetro, true)

        return aproximado.toList().map { p: Point -> Ponto(xMm = p.x / pxPorMm, yMm = p.y / pxPorMm) }
    }
}
