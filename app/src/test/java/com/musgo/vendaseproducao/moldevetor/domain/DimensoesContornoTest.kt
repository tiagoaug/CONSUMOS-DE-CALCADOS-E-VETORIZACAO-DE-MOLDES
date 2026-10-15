package com.musgo.vendaseproducao.moldevetor.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DimensoesContornoTest {

    @Test fun retanguloDeitado() {
        // 30mm (x) x 10mm (y) -> comprimento é sempre a maior dimensão, não importa o eixo.
        val retangulo = Contorno(
            externo = listOf(Ponto(0.0, 0.0), Ponto(30.0, 0.0), Ponto(30.0, 10.0), Ponto(0.0, 10.0)),
        )
        val dimensoes = DimensoesContorno.calcular(retangulo)
        assertEquals(30.0, dimensoes.comprimentoMm, 1e-9)
        assertEquals(10.0, dimensoes.larguraMm, 1e-9)
    }

    @Test fun retanguloEmPe() {
        // Mesmo retângulo, girado 90° (10mm em x, 30mm em y) -> mesmo comprimento/largura.
        val retangulo = Contorno(
            externo = listOf(Ponto(0.0, 0.0), Ponto(10.0, 0.0), Ponto(10.0, 30.0), Ponto(0.0, 30.0)),
        )
        val dimensoes = DimensoesContorno.calcular(retangulo)
        assertEquals(30.0, dimensoes.comprimentoMm, 1e-9)
        assertEquals(10.0, dimensoes.larguraMm, 1e-9)
    }

    @Test fun formaIrregularUsaExtensaoTotal() {
        // Vértices espalhados — comprimento/largura vêm da extensão (max-min) em cada eixo,
        // não da distância entre dois vértices específicos.
        val forma = Contorno(
            externo = listOf(Ponto(-5.0, 2.0), Ponto(15.0, 0.0), Ponto(10.0, 8.0), Ponto(0.0, 6.0)),
        )
        val dimensoes = DimensoesContorno.calcular(forma)
        assertEquals(20.0, dimensoes.comprimentoMm, 1e-9) // extensão em x: 15 - (-5)
        assertEquals(8.0, dimensoes.larguraMm, 1e-9) // extensão em y: 8 - 0
    }

    @Test fun contornoVazioDaZero() {
        val dimensoes = DimensoesContorno.calcular(Contorno(externo = emptyList()))
        assertEquals(0.0, dimensoes.comprimentoMm, 1e-9)
        assertEquals(0.0, dimensoes.larguraMm, 1e-9)
    }

    @Test fun blocosAdicionaisNaoEntramNaMedida() {
        // Bug real corrigido: duplicar um bloco (ou ter outras camadas de fundo) NÃO pode mudar
        // a medida calculada da peça principal — um paquímetro mede só o externo, nunca o
        // enquadramento combinado com cópias/outros blocos.
        val principal = listOf(Ponto(0.0, 0.0), Ponto(10.0, 0.0), Ponto(10.0, 10.0), Ponto(0.0, 10.0))
        val bloco = listOf(Ponto(20.0, 5.0), Ponto(30.0, 5.0), Ponto(30.0, 15.0), Ponto(20.0, 15.0))
        val dimensoes = DimensoesContorno.calcular(Contorno(externo = principal, blocos = listOf(bloco)))
        assertEquals(10.0, dimensoes.comprimentoMm, 1e-9)
        assertEquals(10.0, dimensoes.larguraMm, 1e-9)
    }
}
