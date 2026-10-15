package com.musgo.vendaseproducao.moldevetor.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacaoDesvioTest {

    @Test fun desvioDentroDoLimitePadrao() {
        // 100.5 vs 100 = 0,5% de desvio, dentro do limite padrão de 1%.
        val resultado = ValidacaoDesvio.validar(valorCalculado = 100.5, valorReferencia = 100.0)
        assertEquals(0.5, resultado.desvioPercentual, 1e-9)
        assertTrue(resultado.dentroDoLimite)
    }

    @Test fun desvioExatamenteNoLimiteContaComoDentro() {
        val resultado = ValidacaoDesvio.validar(valorCalculado = 101.0, valorReferencia = 100.0, limitePercent = 1.0)
        assertEquals(1.0, resultado.desvioPercentual, 1e-9)
        assertTrue(resultado.dentroDoLimite)
    }

    @Test fun desvioAcimaDoLimiteDisparaAlerta() {
        // 102 vs 100 = 2% de desvio, acima do limite padrão de 1%.
        val resultado = ValidacaoDesvio.validar(valorCalculado = 102.0, valorReferencia = 100.0)
        assertEquals(2.0, resultado.desvioPercentual, 1e-9)
        assertFalse(resultado.dentroDoLimite)
    }

    @Test fun desvioParaMenosTambemConta() {
        // 95 vs 100 = 5% de desvio (valor calculado MENOR que o real).
        val resultado = ValidacaoDesvio.validar(valorCalculado = 95.0, valorReferencia = 100.0)
        assertEquals(5.0, resultado.desvioPercentual, 1e-9)
        assertFalse(resultado.dentroDoLimite)
    }

    @Test fun limitePersonalizado() {
        // 103 vs 100 = 3% de desvio; com limite de 5% ainda fica dentro.
        val resultado = ValidacaoDesvio.validar(valorCalculado = 103.0, valorReferencia = 100.0, limitePercent = 5.0)
        assertTrue(resultado.dentroDoLimite)
    }

    @Test fun referenciaZeroLancaExcecao() {
        assertThrows(IllegalArgumentException::class.java) {
            ValidacaoDesvio.validar(valorCalculado = 10.0, valorReferencia = 0.0)
        }
    }
}
