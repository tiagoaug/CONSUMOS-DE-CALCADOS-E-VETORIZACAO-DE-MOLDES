package com.musgo.vendaseproducao.moldevetor.data

import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeEntity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FacaProjetoArquivoTest {
    @Test
    fun pacoteBlocoPreservaTodosOsDadosEAfoto() {
        val pasta = Files.createTempDirectory("bloco_teste_").toFile()
        try {
            val foto = pasta.resolve("original.jpg").apply { writeBytes(byteArrayOf(1, 3, 5, 7, 9)) }
            val original = FacaMoldeEntity(
                id = 42,
                codigo = "BLOCO-001",
                nome = "Teste completo",
                referenciaModelo = "MODELO A",
                observacoes = "Observação",
                status = FacaMoldeEntity.STATUS_ATIVA,
                contornoJson = "{\"externo\":[{\"x\":0,\"y\":0}],\"furos\":[],\"blocos\":[]}",
                areaMm2 = 1234.5,
                areaM2 = 0.0012345,
                areaAnteriorJson = "[1200.0]",
                encaixeJson = "{\"tipo\":\"RETANGULO\",\"larguraMm\":10,\"alturaMm\":20,\"instancias\":[]}",
                consumoEncaixeM2 = 0.0002,
                fotoPath = foto.absolutePath,
                createdAt = 100L,
                updatedAt = 200L,
            )

            val arquivo = ByteArrayOutputStream().also { FacaProjetoArquivo.escrever(original, it) }.toByteArray()
            val importado = FacaProjetoArquivo.ler(ByteArrayInputStream(arquivo), pasta.resolve("fotos"))

            assertEquals("BLOCO-001", importado.manifesto.codigo)
            assertEquals(original.contornoJson, importado.manifesto.contornoJson)
            assertEquals(original.encaixeJson, importado.manifesto.encaixeJson)
            assertEquals(original.areaAnteriorJson, importado.manifesto.areaAnteriorJson)
            assertEquals(original.consumoEncaixeM2, importado.manifesto.consumoEncaixeM2)
            assertNotNull(importado.fotoTemporaria)
            assertArrayEquals(foto.readBytes(), importado.fotoTemporaria!!.readBytes())
        } finally {
            pasta.deleteRecursively()
        }
    }
}
