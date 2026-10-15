package com.musgo.vendaseproducao.moldevetor.ui.validacao

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertTrue
import org.junit.Test

class SimplificacaoNosTest {
    @Test
    fun removeNosRedundantesDeRetasEPreservaCantos() {
        val vertices = listOf(
            Offset(0f, 0f), Offset(25f, 0f), Offset(50f, 0f), Offset(75f, 0f), Offset(100f, 0f),
            Offset(100f, 25f), Offset(100f, 50f), Offset(100f, 75f), Offset(100f, 100f),
            Offset(75f, 100f), Offset(50f, 100f), Offset(25f, 100f), Offset(0f, 100f),
            Offset(0f, 75f), Offset(0f, 50f), Offset(0f, 25f),
        )

        val mantidos = simplificarTrechosSelecionados(
            vertices = vertices,
            selecionados = vertices.indices.toSet(),
            travados = emptySet(),
            toleranciaPx = 0.5f,
        )

        assertTrue("deve retirar os pontos intermediários das retas", mantidos.size <= 6)
        assertTrue(setOf(0, 4, 8, 12).all { it in mantidos })
    }

    @Test
    fun noForaDaSelecaoPermaneceComoAncora() {
        val vertices = listOf(
            Offset(0f, 0f), Offset(25f, 0f), Offset(50f, 0f), Offset(75f, 0f),
            Offset(100f, 0f), Offset(100f, 100f), Offset(0f, 100f),
        )

        val mantidos = simplificarTrechosSelecionados(
            vertices = vertices,
            selecionados = setOf(1, 2, 3),
            travados = emptySet(),
            toleranciaPx = 0.5f,
        )

        assertTrue(0 in mantidos && 4 in mantidos && 5 in mantidos && 6 in mantidos)
        assertTrue(1 !in mantidos && 2 !in mantidos && 3 !in mantidos)
    }

    @Test
    fun preservaCurvaQueUltrapassaOTotalDeDesvio() {
        val vertices = listOf(
            Offset(0f, 0f), Offset(25f, -4f), Offset(50f, -7f), Offset(75f, -4f),
            Offset(100f, 0f), Offset(100f, 100f), Offset(0f, 100f),
        )

        val mantidos = simplificarTrechosSelecionados(
            vertices = vertices,
            selecionados = setOf(1, 2, 3),
            travados = emptySet(),
            toleranciaPx = 2f,
        )

        assertTrue("ao menos um nó da curva deve ser preservado", setOf(1, 2, 3).any { it in mantidos })
    }
}
