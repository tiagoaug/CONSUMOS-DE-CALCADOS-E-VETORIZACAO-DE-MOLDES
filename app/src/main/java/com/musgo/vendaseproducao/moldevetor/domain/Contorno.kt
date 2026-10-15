package com.musgo.vendaseproducao.moldevetor.domain

/** Contorno completo de uma faca: vértices EXTERNOS + furos internos (ex.: faca com um furo
 * pra passar outra peça durante o corte) — ver especificação, seção 5 ("considerar a área de
 * furos internos subtraindo do contorno externo"). A ordem (horária/anti-horária) dos vértices
 * não importa: CalculoArea.areaPoligonoMm2 usa valor absoluto. */
data class Contorno(
    val externo: List<Ponto>,
    val furos: List<List<Ponto>> = emptyList(),
    /** Outros perímetros externos fechados que compõem a mesma faca. Cada item é um bloco
     * independente no editor e pode ser movido, girado, duplicado ou excluído. */
    val blocos: List<List<Ponto>> = emptyList(),
) {
    /** Aplica uma correção de escala uniforme a TODOS os pontos (externo, furos e blocos) — usada
     * pra corrigir o pequeno desvio entre o comprimento calculado pela foto e a medida real feita
     * com paquímetro (ver [com.musgo.vendaseproducao.moldevetor.domain.ValidacaoDesvio]). A
     * detecção do marcador ArUco tem uma precisão de borda limitada a poucos pixels; sem essa
     * correção, cada foto carrega um pequeno viés próprio de calibração que acaba virando
     * "variação de tamanho" perceptível quando várias peças/fotos são combinadas no cálculo de
     * consumo (pedido do Tiago: "as fotos estão tendo variações de tamanho ao medir seus tamanhos
     * para o consumo"). Escala a partir da origem (0,0) — a mesma origem já usada pela
     * retificação de perspectiva — então preserva proporções e não desloca o contorno.
     */
    fun escalar(fator: Double): Contorno {
        fun escalarPontos(pontos: List<Ponto>) = pontos.map { Ponto(xMm = it.xMm * fator, yMm = it.yMm * fator) }
        return copy(
            externo = escalarPontos(externo),
            furos = furos.map(::escalarPontos),
            blocos = blocos.map(::escalarPontos),
        )
    }
}
