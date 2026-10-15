package com.musgo.vendaseproducao.moldevetor.domain

import kotlin.math.abs

/** Resultado da validação por medida real de referência — ver especificação, seção 4, passo 7:
 * o usuário informa uma medida real (ex.: comprimento medido com paquímetro) e o app compara
 * com o valor que calculou, alertando se o desvio passar do limite configurável. */
data class ResultadoDesvio(
    val valorCalculado: Double,
    val valorReferencia: Double,
    val desvioPercentual: Double,
    val dentroDoLimite: Boolean,
)

object ValidacaoDesvio {

    /** @param limitePercent limite aceitável de desvio, em %. Padrão/sugestão confirmada com o
     * Tiago: 1.0 (1%). */
    fun validar(valorCalculado: Double, valorReferencia: Double, limitePercent: Double = 1.0): ResultadoDesvio {
        require(valorReferencia != 0.0) { "Valor de referência não pode ser zero." }
        val desvioPercentual = abs(valorCalculado - valorReferencia) / abs(valorReferencia) * 100.0
        return ResultadoDesvio(
            valorCalculado = valorCalculado,
            valorReferencia = valorReferencia,
            desvioPercentual = desvioPercentual,
            dentroDoLimite = desvioPercentual <= limitePercent,
        )
    }
}
