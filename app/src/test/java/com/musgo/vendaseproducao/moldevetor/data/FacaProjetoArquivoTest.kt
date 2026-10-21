package com.musgo.vendaseproducao.moldevetor.data

import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeEntity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.zip.ZipInputStream

class FacaProjetoArquivoTest {
    @Test
    fun pacoteBlocoV2PreservaDadosEIncluiArtefatosDeIntegracao() {
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
                corCapaArgb = -14606047,
                fotoPath = foto.absolutePath,
                createdAt = 100L,
                updatedAt = 200L,
            )

            val capa = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
            val pdf = "%PDF-1.4\n%%EOF".toByteArray()
            val arquivo = ByteArrayOutputStream().also {
                FacaProjetoArquivo.escrever(
                    original,
                    it,
                    grupoModeloNome = "Tênis Casual",
                    artefatos = ArtefatosExportacaoFaca(capaVetorPng = capa, encaixePdf = pdf),
                )
            }.toByteArray()
            val importado = FacaProjetoArquivo.ler(ByteArrayInputStream(arquivo), pasta.resolve("fotos"))

            assertEquals("BLOCO-001", importado.manifesto.codigo)
            assertEquals("Tênis Casual", importado.manifesto.grupoModeloNome)
            assertEquals(original.contornoJson, importado.manifesto.contornoJson)
            assertEquals(original.encaixeJson, importado.manifesto.encaixeJson)
            assertEquals(original.areaAnteriorJson, importado.manifesto.areaAnteriorJson)
            assertEquals(original.consumoEncaixeM2, importado.manifesto.consumoEncaixeM2)
            assertEquals(original.corCapaArgb, importado.manifesto.corCapaArgb)
            assertEquals(2, importado.manifesto.versao)
            assertEquals("vetor-capa.png", importado.manifesto.capaVetorArquivo)
            assertEquals("encaixe.pdf", importado.manifesto.encaixePdfArquivo)
            assertEquals("instrucoes.json", importado.manifesto.instrucoesArquivo)
            assertTrue(importado.manifesto.etapas?.vetorizacaoSalva == true)
            assertTrue(importado.manifesto.etapas?.conferenciaDesvioSalva == true)
            assertTrue(importado.manifesto.etapas?.encaixeSalvo == true)
            assertNotNull(importado.fotoTemporaria)
            assertArrayEquals(foto.readBytes(), importado.fotoTemporaria!!.readBytes())

            val entradas = mutableMapOf<String, ByteArray>()
            ZipInputStream(ByteArrayInputStream(arquivo)).use { zip ->
                while (true) {
                    val entrada = zip.nextEntry ?: break
                    entradas[entrada.name] = zip.readBytes()
                    zip.closeEntry()
                }
            }
            assertArrayEquals(capa, entradas["vetor-capa.png"])
            assertArrayEquals(pdf, entradas["encaixe.pdf"])
            val instrucoes = entradas["instrucoes.json"]?.toString(Charsets.UTF_8).orEmpty()
            assertTrue(instrucoes.contains("MUSGO_IMPORTACAO_FACA"))
            assertTrue(instrucoes.contains("m2_por_bloco"))
            assertTrue(instrucoes.contains("sizeAreas[numeracao_escolhida]"))
        } finally {
            pasta.deleteRecursively()
        }
    }
}
