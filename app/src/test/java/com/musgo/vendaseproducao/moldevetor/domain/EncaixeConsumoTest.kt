package com.musgo.vendaseproducao.moldevetor.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class EncaixeConsumoTest {
    @Test fun consumoUsaAreaDoModuloDivididaPelasInstancias() {
        val encaixe = EncaixeConsumo(
            tipo = TipoModuloEncaixe.RETANGULO,
            larguraMm = 100.0,
            alturaMm = 50.0,
            instancias = List(5) { InstanciaEncaixe(0, 0.0, 0.0) },
        )
        assertEquals(5_000.0, encaixe.areaModuloMm2, 1e-9)
        assertEquals(1_000.0, encaixe.consumoPorBlocoMm2, 1e-9)
        assertEquals(0.001, encaixe.consumoPorBlocoM2, 1e-12)
    }

    @Test fun deslocamentoDoParalelogramoNaoAlteraAreaDaCelula() {
        val encaixe = EncaixeConsumo(
            tipo = TipoModuloEncaixe.PARALELOGRAMO,
            larguraMm = 80.0,
            alturaMm = 40.0,
            deslocamentoMm = 25.0,
            instancias = listOf(InstanciaEncaixe(0, 0.0, 0.0)),
        )
        assertEquals(3_200.0, encaixe.areaModuloMm2, 1e-9)
    }
}
