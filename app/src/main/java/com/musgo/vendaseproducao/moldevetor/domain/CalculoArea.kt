package com.musgo.vendaseproducao.moldevetor.domain

import kotlin.math.abs

/** Cálculo de área de polígono pela fórmula do Shoelace (Gauss) — ver especificação, seção 5.
 * Classe PURA (sem dependência de Android/OpenCV), testável isoladamente com JUnit comum (ver
 * android/app/src/test/.../CalculoAreaTest.kt) — mesmo critério de isolamento já usado em
 * expedicaobling/domain/ neste projeto. */
object CalculoArea {

    /** Área de um polígono simples (não auto-intersectante), SEMPRE positiva — o sinal da
     * fórmula de Gauss só indica o sentido (horário/anti-horário) dos vértices, irrelevante
     * aqui. Menos de 3 pontos não forma um polígono válido, retorna 0. */
    fun areaPoligonoMm2(pontos: List<Ponto>): Double {
        if (pontos.size < 3) return 0.0
        var somaCruzada = 0.0
        for (i in pontos.indices) {
            val atual = pontos[i]
            val proximo = pontos[(i + 1) % pontos.size]
            somaCruzada += atual.xMm * proximo.yMm - proximo.xMm * atual.yMm
        }
        return abs(somaCruzada) / 2.0
    }

    /** Área TOTAL do contorno de uma faca — área externa menos a soma das áreas de cada furo
     * interno. Nunca retorna valor negativo (se os furos, por erro de digitalização, somarem
     * mais que a área externa, o resultado fica travado em 0 — nunca uma área negativa sem
     * sentido físico). */
    fun calcularAreaMm2(contorno: Contorno): Double {
        val areaExterna = areaPoligonoMm2(contorno.externo) + contorno.blocos.sumOf { areaPoligonoMm2(it) }
        val areaFuros = contorno.furos.sumOf { areaPoligonoMm2(it) }
        return (areaExterna - areaFuros).coerceAtLeast(0.0)
    }

    /** Conversão: área em m² = área em mm² ÷ 1.000.000 (ver especificação, seção 5). */
    fun mm2ParaM2(areaMm2: Double): Double = areaMm2 / 1_000_000.0
}
