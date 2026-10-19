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
    val createdAt: Long,
    val updatedAt: Long,
    val fotoArquivo: String? = null,
)

internal data class ProjetoFacaLido(
    val manifesto: ManifestoProjetoFaca,
    val fotoTemporaria: File?,
)

/**
 * Pacote completo e portátil da faca. O ZIP usa a extensão amigável `.bloco` e contém:
 * `projeto.json` (cadastro, contorno, camadas/blocos, áreas e encaixe) e a foto original.
 */
internal object FacaProjetoArquivo {
    const val FORMATO = "MUSGO_BLOCO"
    const val VERSAO_ATUAL = 1
    const val MIME_TYPE = "application/octet-stream"
    private const val MANIFESTO = "projeto.json"
    private const val LIMITE_MANIFESTO_BYTES = 5L * 1024L * 1024L
    private const val LIMITE_FOTO_BYTES = 150L * 1024L * 1024L

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    fun escrever(faca: FacaMoldeEntity, destino: OutputStream, grupoModeloNome: String? = null) {
        val foto = faca.fotoPath?.let(::File)?.takeIf { it.isFile && it.canRead() }
        val extensao = foto?.extension?.lowercase()?.takeIf { it.matches(Regex("[a-z0-9]{1,5}")) } ?: "jpg"
        val nomeFoto = foto?.let { "foto.$extensao" }
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
            createdAt = faca.createdAt,
            updatedAt = faca.updatedAt,
            fotoArquivo = nomeFoto,
        )

        ZipOutputStream(destino.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFESTO))
            zip.write(json.encodeToString(manifesto).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            if (foto != null && nomeFoto != null) {
                zip.putNextEntry(ZipEntry(nomeFoto))
                foto.inputStream().buffered().use { origem -> origem.copyTo(zip) }
                zip.closeEntry()
            }
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
}
