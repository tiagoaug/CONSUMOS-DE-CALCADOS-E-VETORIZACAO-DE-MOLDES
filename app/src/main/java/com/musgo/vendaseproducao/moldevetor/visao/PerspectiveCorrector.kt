package com.musgo.vendaseproducao.moldevetor.visao

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/** Corrige perspectiva/escala usando os 4 cantos do marcador ArUco detectados (ver
 * ArucoMarkerDetector) — produz uma imagem "vista de cima", onde cada pixel corresponde a uma
 * distância real conhecida ([pxPorMm]), pronta pra extração de contorno (ContornoExtractor) em
 * milímetros sem mais nenhuma conversão de escala depois disso. Pressupõe que a faca está no
 * MESMO plano do marcador (mesa plana) — mesma premissa da especificação, seção 4 passo 1. */
object PerspectiveCorrector {
    /** 2,5 px por milímetro. Com o marcador físico de 100 mm, ele ocupa 250 px na imagem
     * retificada — a mesma resolução em pixels usada anteriormente, agora com a escala física
     * correta e sem aumentar o consumo de memória. */
    const val PX_POR_MM = 2.5

    /** Campo físico de 600 mm. Multiplicado por [PX_POR_MM], mantém a imagem de saída em
     * 1500 × 1500 px e evita cortar peças posicionadas ao lado do marcador de 100 mm. */
    const val LADO_SAIDA_MM = 600.0

    data class Resultado(val imagemCorrigida: Mat, val pxPorMm: Double)

    /** @param cantosMarcadorPx os 4 cantos do marcador, na ORDEM que o ArucoDetector retorna
     * (sentido horário, começando pelo canto superior-esquerdo do marcador). */
    fun corrigir(imagemOriginal: Mat, cantosMarcadorPx: List<Point>): Resultado {
        require(cantosMarcadorPx.size == 4) { "Marcador precisa ter exatamente 4 cantos." }

        val origem = MatOfPoint2f(*cantosMarcadorPx.toTypedArray())

        val ladoMarcadorPx = MarcadorAruco.TAMANHO_MM * PX_POR_MM
        // Mesma ordem dos cantos de origem: superior-esquerdo, superior-direito,
        // inferior-direito, inferior-esquerdo — o marcador fica ancorado no canto (0,0) da
        // imagem de destino; o resto da cena é posicionado relativo a ele pela homografia.
        val destino = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(ladoMarcadorPx, 0.0),
            Point(ladoMarcadorPx, ladoMarcadorPx),
            Point(0.0, ladoMarcadorPx),
        )

        val homografia = Imgproc.getPerspectiveTransform(origem, destino)

        val ladoSaidaPx = LADO_SAIDA_MM * PX_POR_MM
        val tamanhoSaida = Size(ladoSaidaPx, ladoSaidaPx)

        val corrigida = Mat()
        Imgproc.warpPerspective(imagemOriginal, corrigida, homografia, tamanhoSaida)

        return Resultado(imagemCorrigida = corrigida, pxPorMm = PX_POR_MM)
    }
}
