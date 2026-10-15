package com.musgo.vendaseproducao.moldevetor.domain

/** Dimensões lineares (não área) do contorno externo de uma faca — usadas na Fase 4 pra
 * comparar com uma medida real tirada com paquímetro (ver especificação, seção 4 passo 7: "o
 * usuário informa uma medida real... o app compara com o valor que calculou"). Um paquímetro
 * mede comprimento, não área, por isso esse é o valor usado em ValidacaoDesvio, não areaMm2.
 *
 * Simplificação deliberada: usa o retângulo delimitador ALINHADO aos eixos (bounding box), não
 * o retângulo de área mínima rotacionado — como a imagem já está retificada (perspectiva
 * corrigida em relação ao marcador), e a faca normalmente é fotografada já alinhada à mesa, essa
 * aproximação é suficiente pra esta fase sem precisar de um algoritmo de rotating calipers. */
object DimensoesContorno {

    data class Dimensoes(val comprimentoMm: Double, val larguraMm: Double)

    /** [comprimentoMm] é sempre a MAIOR dimensão, [larguraMm] a menor — independente de a faca
     * estar fotografada "deitada" ou "em pé" na imagem corrigida.
     *
     * Mede SÓ [Contorno.externo] — NUNCA os [Contorno.blocos] (camadas de fundo/cópias). Um
     * paquímetro mede UMA peça física; se o usuário duplicar um bloco pra organizar o layout no
     * editor, essa duplicata não pode inflar a medida "real" da peça original (bug real
     * encontrado: "esse tamanho se diferencia ao duplicar" — a versão anterior somava o
     * enquadramento de TODOS os blocos, então duplicar sempre aumentava o comprimento/largura
     * calculados, mesmo a peça original não tendo mudado). Blocos continuam contando pra
     * CalculoArea (consumo de material combinado faz sentido ali) — só não entram aqui. */
    fun calcular(contorno: Contorno): Dimensoes {
        val xs = contorno.externo.map { it.xMm }
        val ys = contorno.externo.map { it.yMm }
        if (xs.isEmpty() || ys.isEmpty()) return Dimensoes(0.0, 0.0)

        val extensaoX = (xs.max() - xs.min())
        val extensaoY = (ys.max() - ys.min())
        return Dimensoes(
            comprimentoMm = maxOf(extensaoX, extensaoY),
            larguraMm = minOf(extensaoX, extensaoY),
        )
    }
}
