package com.musgo.vendaseproducao.moldevetor.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculoAreaTest {

    @Test fun retanguloSimples() {
        // 10mm x 20mm = 200mm²
        val retangulo = listOf(
            Ponto(0.0, 0.0), Ponto(10.0, 0.0), Ponto(10.0, 20.0), Ponto(0.0, 20.0),
        )
        assertEquals(200.0, CalculoArea.areaPoligonoMm2(retangulo), 1e-9)
    }

    @Test fun retanguloSentidoAntiHorarioDaMesmaArea() {
        // Mesmo retângulo, vértices na ordem inversa — o valor absoluto da fórmula de Gauss
        // precisa dar o mesmo resultado independente do sentido.
        val retangulo = listOf(
            Ponto(0.0, 0.0), Ponto(0.0, 20.0), Ponto(10.0, 20.0), Ponto(10.0, 0.0),
        )
        assertEquals(200.0, CalculoArea.areaPoligonoMm2(retangulo), 1e-9)
    }

    @Test fun triangulo() {
        // Base 10mm, altura 6mm -> área = base*altura/2 = 30mm²
        val triangulo = listOf(Ponto(0.0, 0.0), Ponto(10.0, 0.0), Ponto(5.0, 6.0))
        assertEquals(30.0, CalculoArea.areaPoligonoMm2(triangulo), 1e-9)
    }

    @Test fun menosDeTresPontosDaAreaZero() {
        assertEquals(0.0, CalculoArea.areaPoligonoMm2(listOf(Ponto(0.0, 0.0), Ponto(1.0, 1.0))), 1e-9)
        assertEquals(0.0, CalculoArea.areaPoligonoMm2(emptyList()), 1e-9)
    }

    @Test fun contornoComFuroSubtraiAreaInterna() {
        // Externo: 10mm x 10mm = 100mm². Furo: 2mm x 2mm = 4mm² -> total 96mm².
        val externo = listOf(Ponto(0.0, 0.0), Ponto(10.0, 0.0), Ponto(10.0, 10.0), Ponto(0.0, 10.0))
        val furo = listOf(Ponto(4.0, 4.0), Ponto(6.0, 4.0), Ponto(6.0, 6.0), Ponto(4.0, 6.0))
        val contorno = Contorno(externo = externo, furos = listOf(furo))
        assertEquals(96.0, CalculoArea.calcularAreaMm2(contorno), 1e-9)
    }

    @Test fun contornoComDoisFuros() {
        // Externo 20x20=400mm². Dois furos de 2x2=4mm² cada -> 400 - 8 = 392mm².
        val externo = listOf(Ponto(0.0, 0.0), Ponto(20.0, 0.0), Ponto(20.0, 20.0), Ponto(0.0, 20.0))
        val furo1 = listOf(Ponto(2.0, 2.0), Ponto(4.0, 2.0), Ponto(4.0, 4.0), Ponto(2.0, 4.0))
        val furo2 = listOf(Ponto(12.0, 12.0), Ponto(14.0, 12.0), Ponto(14.0, 14.0), Ponto(12.0, 14.0))
        val contorno = Contorno(externo = externo, furos = listOf(furo1, furo2))
        assertEquals(392.0, CalculoArea.calcularAreaMm2(contorno), 1e-9)
    }

    @Test fun furosMaioresQueExternoTravaEmZero() {
        val externo = listOf(Ponto(0.0, 0.0), Ponto(2.0, 0.0), Ponto(2.0, 2.0), Ponto(0.0, 2.0)) // 4mm²
        val furo = listOf(Ponto(0.0, 0.0), Ponto(10.0, 0.0), Ponto(10.0, 10.0), Ponto(0.0, 10.0)) // 100mm²
        val contorno = Contorno(externo = externo, furos = listOf(furo))
        assertEquals(0.0, CalculoArea.calcularAreaMm2(contorno), 1e-9)
    }

    @Test fun blocosAdicionaisSomamAoPerimetroPrincipal() {
        val principal = listOf(Ponto(0.0, 0.0), Ponto(10.0, 0.0), Ponto(10.0, 10.0), Ponto(0.0, 10.0))
        val copia = listOf(Ponto(20.0, 0.0), Ponto(30.0, 0.0), Ponto(30.0, 10.0), Ponto(20.0, 10.0))
        val contorno = Contorno(externo = principal, blocos = listOf(copia))
        assertEquals(200.0, CalculoArea.calcularAreaMm2(contorno), 1e-9)
    }

    @Test fun conversaoMm2ParaM2() {
        assertEquals(1.0, CalculoArea.mm2ParaM2(1_000_000.0), 1e-9)
        assertEquals(0.0001, CalculoArea.mm2ParaM2(100.0), 1e-12)
    }
}
