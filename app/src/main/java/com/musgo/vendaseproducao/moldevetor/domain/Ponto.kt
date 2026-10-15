package com.musgo.vendaseproducao.moldevetor.domain

/** Um vértice do contorno de uma faca, já em milímetros (não pixels) — a conversão px→mm
 * acontece antes, na correção de perspectiva (Fase 3, visao/PerspectiveCorrector.kt). */
data class Ponto(val xMm: Double, val yMm: Double)
