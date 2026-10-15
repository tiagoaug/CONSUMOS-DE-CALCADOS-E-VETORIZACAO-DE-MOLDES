package com.musgo.vendaseproducao.moldevetor.domain

enum class TipoModuloEncaixe { RETANGULO, PARALELOGRAMO }

/** Uma ocorrência de um bloco real dentro do módulo repetível de encaixe. */
data class InstanciaEncaixe(
    val blocoIndice: Int,
    val xMm: Double,
    val yMm: Double,
    val rotacaoGraus: Double = 0.0,
    val espelhadaHorizontal: Boolean = false,
    val invertidaVertical: Boolean = false,
    val travada: Boolean = false,
)

/** Informação de consumo separada do contorno real. O paralelogramo usa [deslocamentoMm]
 * entre a base inferior e a superior; sua área continua sendo base × altura. */
data class EncaixeConsumo(
    val tipo: TipoModuloEncaixe,
    val larguraMm: Double,
    val alturaMm: Double,
    val deslocamentoMm: Double = 0.0,
    /** Distância mínima permitida entre os perímetros dos blocos na folha, separada por eixo —
     * pedido do Tiago: "o afastamento mínimo entre blocos deve ter valor para as laterais e
     * acima e abaixo" (duas peças lado a lado respeitam [afastamentoHorizontalMm]; uma acima da
     * outra respeitam [afastamentoVerticalMm]). */
    val afastamentoHorizontalMm: Double = 5.0,
    val afastamentoVerticalMm: Double = 5.0,
    val instancias: List<InstanciaEncaixe>,
) {
    val areaModuloMm2: Double get() = larguraMm.coerceAtLeast(0.0) * alturaMm.coerceAtLeast(0.0)
    val consumoPorBlocoMm2: Double get() = if (instancias.isEmpty()) 0.0 else areaModuloMm2 / instancias.size
    val consumoPorBlocoM2: Double get() = consumoPorBlocoMm2 / 1_000_000.0
}
