package com.musgo.vendaseproducao.moldevetor.data

import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Manifesto versionado gravado dentro do pacote .bloco. */
@Serializable
internal data class EtapasProjetoFaca(
    val ordem: List<String> = listOf("vetorizacao", "conferencia_desvio", "encaixe_consumo"),
    val vetorizacaoSalva: Boolean,
    val conferenciaDesvioSalva: Boolean,
    val encaixeSalvo: Boolean,
)

@Serializable
internal data class ManifestoProjetoFaca(
    val formato: String = FacaProjetoArquivo.FORMATO,
    val versao: Int = FacaProjetoArquivo.VERSAO_ATUAL,
    val exportadoEm: Long,
    val codigo: String,
    val nome: String,
    val grupoModeloNome: String? = null,
    val referenciaModelo: String? = null,
    val observacoes: String? = null,
    val status: String,
    val contornoJson: String? = null,
    val areaMm2: Double? = null,
    val areaM2: Double? = null,
    val areaAnteriorJson: String? = null,
    val encaixeJson: String? = null,
    val consumoEncaixeM2: Double? = null,
    val corCapaArgb: Int = -16777216,
    val createdAt: Long,
    val updatedAt: Long,
    val fotoArquivo: String? = null,
    val capaVetorArquivo: String? = null,
    val capaVetorMimeType: String? = null,
    val encaixePdfArquivo: String? = null,
    val encaixePdfMimeType: String? = null,
    val instrucoesArquivo: String? = null,
    val etapas: EtapasProjetoFaca? = null,
)

@Serializable
internal data class RecursoImportacaoFaca(
    val arquivo: String,
    val mimeType: String,
    val finalidade: String,
    val campoDestino: String,
)

@Serializable
internal data class ConsumoImportacaoFaca(
    val valorM2PorBloco: Double? = null,
    val unidade: String = "m2_por_bloco",
    val campoOrigem: String = "projeto.json#consumoEncaixeM2",
    val campoDestino: String = "metadata.sizeAreas[numeracao_escolhida]",
    val aplicarAutomaticamente: Boolean = false,
    val instrucao: String = "Exibir o valor importado e deixar o usuário escolher a numeração da grade antes de aplicar.",
)

@Serializable
internal data class InstrucoesImportacaoFaca(
    val formato: String = "MUSGO_IMPORTACAO_FACA",
    val versao: Int = 1,
    val arquivoProjeto: String = "projeto.json",
    val capa: RecursoImportacaoFaca? = null,
    val encaixe: RecursoImportacaoFaca? = null,
    val consumo: ConsumoImportacaoFaca,
    val compatibilidade: String = "O importador deve aceitar MUSGO_BLOCO versões 1 e 2; na versão 1, gerar capa e PDF a partir dos JSONs.",
)

internal data class ProjetoFacaLido(
    val manifesto: ManifestoProjetoFaca,
    val fotoTemporaria: File?,
)

/**
 * Pacote completo e portátil da faca. O ZIP usa a extensão amigável `.bloco` e contém:
 * `projeto.json`, `instrucoes.json`, a foto original e, quando disponíveis, a capa vetorial PNG
 * e o PDF do encaixe.
 */
internal object FacaProjetoArquivo {
    const val FORMATO = "MUSGO_BLOCO"
    const val VERSAO_ATUAL = 2
    const val MIME_TYPE = "application/octet-stream"
    private const val MANIFESTO = "projeto.json"
    private const val INSTRUCOES = "instrucoes.json"
    private const val CAPA_VETOR = "vetor-capa.png"
    private const val ENCAIXE_PDF = "encaixe.pdf"
    private const val LIMITE_MANIFESTO_BYTES = 5L * 1024L * 1024L
    private const val LIMITE_FOTO_BYTES = 150L * 1024L * 1024L

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    fun escrever(
        faca: FacaMoldeEntity,
        destino: OutputStream,
        grupoModeloNome: String? = null,
        artefatos: ArtefatosExportacaoFaca = ArtefatosExportacaoFaca(),
    ) {
        val foto = faca.fotoPath?.let(::File)?.takeIf { it.isFile && it.canRead() }
        val extensao = foto?.extension?.lowercase()?.takeIf { it.matches(Regex("[a-z0-9]{1,5}")) } ?: "jpg"
        val nomeFoto = foto?.let { "foto.$extensao" }
        val nomeCapa = CAPA_VETOR.takeIf { artefatos.capaVetorPng?.isNotEmpty() == true }
        val nomePdf = ENCAIXE_PDF.takeIf { artefatos.encaixePdf?.isNotEmpty() == true }
        val manifesto = ManifestoProjetoFaca(
            exportadoEm = System.currentTimeMillis(),
            codigo = faca.codigo,
            nome = faca.nome,
            grupoModeloNome = grupoModeloNome,
            referenciaModelo = faca.referenciaModelo,
            observacoes = faca.observacoes,
            status = faca.status,
            contornoJson = faca.contornoJson,
            areaMm2 = faca.areaMm2,
            areaM2 = faca.areaM2,
            areaAnteriorJson = faca.areaAnteriorJson,
            encaixeJson = faca.encaixeJson,
            consumoEncaixeM2 = faca.consumoEncaixeM2,
            corCapaArgb = faca.corCapaArgb,
            createdAt = faca.createdAt,
            updatedAt = faca.updatedAt,
            fotoArquivo = nomeFoto,
            capaVetorArquivo = nomeCapa,
            capaVetorMimeType = nomeCapa?.let { "image/png" },
            encaixePdfArquivo = nomePdf,
            encaixePdfMimeType = nomePdf?.let { "application/pdf" },
            instrucoesArquivo = INSTRUCOES,
            etapas = EtapasProjetoFaca(
                vetorizacaoSalva = !faca.contornoJson.isNullOrBlank(),
                conferenciaDesvioSalva = faca.areaMm2 != null,
                encaixeSalvo = !faca.encaixeJson.isNullOrBlank(),
            ),
        )
        val instrucoes = InstrucoesImportacaoFaca(
            capa = nomeCapa?.let {
                RecursoImportacaoFaca(
                    arquivo = it,
                    mimeType = "image/png",
                    finalidade = "foto_de_capa_da_faca",
                    campoDestino = "imageUrl",
                )
            },
            encaixe = nomePdf?.let {
                RecursoImportacaoFaca(
                    arquivo = it,
                    mimeType = "application/pdf",
                    finalidade = "documento_do_encaixe_e_consumo",
                    campoDestino = "metadata.editorEncaixePdfUrl",
                )
            },
            consumo = ConsumoImportacaoFaca(valorM2PorBloco = faca.consumoEncaixeM2),
        )

        ZipOutputStream(destino.buffered()).use { zip ->
            zip.escreverEntrada(MANIFESTO, json.encodeToString(manifesto).toByteArray(Charsets.UTF_8))
            zip.escreverEntrada(INSTRUCOES, json.encodeToString(instrucoes).toByteArray(Charsets.UTF_8))
            if (foto != null && nomeFoto != null) {
                zip.putNextEntry(ZipEntry(nomeFoto))
                foto.inputStream().buffered().use { origem -> origem.copyTo(zip) }
                zip.closeEntry()
            }
            if (nomeCapa != null) zip.escreverEntrada(nomeCapa, artefatos.capaVetorPng!!)
            if (nomePdf != null) zip.escreverEntrada(nomePdf, artefatos.encaixePdf!!)
        }
    }

    fun ler(origem: InputStream, diretorioFotos: File): ProjetoFacaLido {
        diretorioFotos.mkdirs()
        var manifesto: ManifestoProjetoFaca? = null
        var fotoTemporaria: File? = null
        try {
            ZipInputStream(origem.buffered()).use { zip ->
                while (true) {
                    val entrada = zip.nextEntry ?: break
                    when {
                        entrada.isDirectory -> Unit
                        entrada.name == MANIFESTO -> {
                            val texto = String(zip.lerAte(LIMITE_MANIFESTO_BYTES), Charsets.UTF_8)
                            manifesto = json.decodeFromString<ManifestoProjetoFaca>(texto)
                        }
                        entrada.name.startsWith("foto.") && fotoTemporaria == null -> {
                            val extensao = entrada.name.substringAfterLast('.', "jpg")
                                .lowercase().takeIf { it.matches(Regex("[a-z0-9]{1,5}")) } ?: "jpg"
                            val arquivo = File.createTempFile("faca_importada_", ".$extensao", diretorioFotos)
                            arquivo.outputStream().buffered().use { destino ->
                                zip.copiarAte(destino, LIMITE_FOTO_BYTES)
                            }
                            fotoTemporaria = arquivo
                        }
                    }
                    zip.closeEntry()
                }
            }

            val dados = manifesto ?: throw IllegalArgumentException("O arquivo não contém projeto.json.")
            require(dados.formato == FORMATO) { "Este arquivo não é um projeto de faca válido." }
            require(dados.versao in 1..VERSAO_ATUAL) { "Versão de projeto não suportada: ${dados.versao}." }
            require(dados.codigo.isNotBlank() && dados.nome.isNotBlank()) { "Projeto sem código ou nome." }
            if (dados.fotoArquivo != null && fotoTemporaria == null) {
                throw IllegalArgumentException("A foto indicada no projeto não foi encontrada.")
            }
            return ProjetoFacaLido(dados, fotoTemporaria)
        } catch (erro: Exception) {
            fotoTemporaria?.delete()
            throw erro
        }
    }

    private fun InputStream.lerAte(limite: Long): ByteArray {
        val destino = ByteArrayOutputStream()
        copiarAte(destino, limite)
        return destino.toByteArray()
    }

    private fun InputStream.copiarAte(destino: OutputStream, limite: Long) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val lidos = read(buffer)
            if (lidos < 0) break
            total += lidos
            require(total <= limite) { "Arquivo de projeto excede o limite permitido." }
            destino.write(buffer, 0, lidos)
        }
    }

    private fun ZipOutputStream.escreverEntrada(nome: String, conteudo: ByteArray) {
        putNextEntry(ZipEntry(nome))
        write(conteudo)
        closeEntry()
    }
}
