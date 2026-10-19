package com.musgo.vendaseproducao.moldevetor.visao

import org.junit.Assert.assertEquals
import org.junit.Test

class CalibracaoMarcadorTest {

    @Test
    fun marcadorDeDezCentimetrosMantemImagemDe1500PixelsComEscalaCorreta() {
        assertEquals(100.0, MarcadorAruco.TAMANHO_MM, 0.0)
        assertEquals(2.5, PerspectiveCorrector.PX_POR_MM, 0.0)
        assertEquals(250.0, MarcadorAruco.TAMANHO_MM * PerspectiveCorrector.PX_POR_MM, 0.0)
        assertEquals(1500.0, PerspectiveCorrector.LADO_SAIDA_MM * PerspectiveCorrector.PX_POR_MM, 0.0)
    }
}
