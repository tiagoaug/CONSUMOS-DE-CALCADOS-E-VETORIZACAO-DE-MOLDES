package com.musgo.vendaseproducao.moldevetor.visao

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Objdetect

/** Marcador de referência usado pra corrigir escala/perspectiva — DICT_4X4_50, impresso em
 * 5 cm (decisão confirmada com o Tiago). API verificada contra o jar real baixado
 * (org.opencv:opencv:4.10.0) com `javap`, não documentação — este projeto não tem como
 * compilar/testar OpenCV de outra forma além do build real do Gradle. */
object MarcadorAruco {
    const val TAMANHO_MM = 50.0
}

data class MarcadorDetectado(
    val id: Int,
    /** 4 cantos do marcador, em pixels da imagem de entrada, na ordem que o OpenCV retorna
     * (sentido horário, começando pelo canto superior-esquerdo do marcador). */
    val cantosPx: List<org.opencv.core.Point>,
)

/** Detecta o marcador ArUco numa imagem — espelha o passo 3 da especificação ("Detecção dos
 * marcadores: o OpenCV detecta os marcadores ArUco e calcula a homografia"). A homografia em
 * si é calculada depois, em PerspectiveCorrector, a partir dos cantos devolvidos aqui. */
object ArucoMarkerDetector {
    private val detector: ArucoDetector by lazy {
        ArucoDetector(Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50))
    }

    /** @return o primeiro marcador detectado, ou null se nenhum foi encontrado (ver
     * especificação: "Se a detecção falhar, o app informa o motivo"). Só o primeiro importa
     * nesta versão — um único marcador de referência por foto. */
    fun detectar(imagemCinzaOuColorida: Mat): MarcadorDetectado? {
        val cantos = mutableListOf<Mat>()
        val ids = Mat()
        detector.detectMarkers(imagemCinzaOuColorida, cantos, ids)

        if (cantos.isEmpty()) return null

        val idsArray = IntArray(ids.rows())
        if (ids.rows() > 0) ids.get(0, 0, idsArray)

        val primeiroCantoMat = cantos[0] // 1x4 Mat de Point2f (ou similar) — converte via MatOfPoint2f
        val pontos = MatOfPoint2f(primeiroCantoMat).toList()

        return MarcadorDetectado(
            id = idsArray.getOrElse(0) { -1 },
            cantosPx = pontos,
        )
    }
}
