package com.musgo.vendaseproducao.moldevetor.data

import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeDao
import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeEntity
import com.musgo.vendaseproducao.moldevetor.domain.CalculoArea
import com.musgo.vendaseproducao.moldevetor.domain.Contorno
import com.musgo.vendaseproducao.moldevetor.domain.EncaixeConsumo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** Resumo exibido na lista — não carrega areaAnteriorJson (histórico, só importa na tela de
 * edição/validação). [contornoJson] vai junto pra desenhar a miniatura do bloco no card da
 * lista (pedido do Tiago: "um thumbnail do bloco representativo como miniatura") — sem custo
 * extra de consulta, já que [FacaMoldeDao.observeAll] carrega a entidade inteira mesmo. */
data class FacaMoldeSummary(
    val id: Long,
    val codigo: String,
    val nome: String,
    val referenciaModelo: String?,
    val status: String,
    val areaM2: Double?,
    val consumoEncaixeM2: Double?,
    val updatedAt: Long,
    val contornoJson: String?,
)

class FacaMoldeCodigoDuplicadoException(codigo: String) :
    Exception("Já existe uma faca cadastrada com o código \"$codigo\".")

class BlocoImportacaoConflitoException(val codigo: String, val nome: String) :
    Exception("Já existe um bloco com o código \"$codigo\".")

data class BlocoImportadoResultado(val id: Long, val codigo: String, val nome: String)

class FacaMoldeRepository(
    private val dao: FacaMoldeDao,
    private val filesDir: File? = null,
) {

    fun observeSummaries(): Flow<List<FacaMoldeSummary>> =
        dao.observeAll().map { list -> list.map { it.toSummary() } }

    suspend fun getById(id: Long): FacaMoldeEntity? = dao.getById(id)

    /** Cria (id == null) ou edita uma faca. Valida unicidade de código ANTES de gravar — ver
     * campo "Código da faca (único, obrigatório)" na especificação. */
    suspend fun salvar(
        id: Long?,
        codigo: String,
        nome: String,
        referenciaModelo: String?,
        observacoes: String?,
    ): Long {
        val codigoTrim = codigo.trim()
        val existente = dao.getByCodigo(codigoTrim)
        if (existente != null && existente.id != id) {
            throw FacaMoldeCodigoDuplicadoException(codigoTrim)
        }

        val now = System.currentTimeMillis()
        return if (id == null) {
            dao.insert(
                FacaMoldeEntity(
                    codigo = codigoTrim,
                    nome = nome.trim(),
                    referenciaModelo = referenciaModelo?.trim()?.ifBlank { null },
                    observacoes = observacoes?.trim()?.ifBlank { null },
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            val atual = dao.getById(id) ?: throw IllegalStateException("Faca não encontrada (id=$id).")
            dao.update(
                atual.copy(
                    codigo = codigoTrim,
                    nome = nome.trim(),
                    referenciaModelo = referenciaModelo?.trim()?.ifBlank { null },
                    observacoes = observacoes?.trim()?.ifBlank { null },
                    updatedAt = now,
                ),
            )
            id
        }
    }

    /** Exclusão lógica — comportamento PADRÃO (ver especificação: "para não quebrar registros
     * já exportados ou usados em cálculos anteriores"). */
    suspend fun excluirLogico(id: Long) = dao.setStatus(id, FacaMoldeEntity.STATUS_INATIVA, System.currentTimeMillis())

    suspend fun reativar(id: Long) = dao.setStatus(id, FacaMoldeEntity.STATUS_ATIVA, System.currentTimeMillis())

    /** Exclusão FÍSICA — a tela (ListaFacasScreen) é responsável por restringir isso a
     * administradores e pedir confirmação extra antes de chamar este método. */
    suspend fun excluirFisico(id: Long) {
        val atual = dao.getById(id)
        dao.deletePhysical(id)
        removerFotoInterna(atual?.fotoPath)
    }

    /** Exporta cadastro, vetores, blocos/camadas, áreas, encaixe e foto num único `.bloco`. */
    suspend fun exportarBloco(id: Long, destino: OutputStream) {
        val faca = dao.getById(id) ?: throw IllegalStateException("Bloco não encontrado (id=$id).")
        FacaProjetoArquivo.escrever(faca, destino)
    }

    /**
     * Importa um pacote `.bloco`. Se já existir o mesmo código, exige confirmação explícita para
     * substituir. A foto é restaurada no armazenamento interno e vinculada ao registro importado.
     */
    suspend fun importarBloco(origem: InputStream, sobrescreverExistente: Boolean = false): BlocoImportadoResultado {
        val raiz = filesDir ?: throw IllegalStateException("Diretório interno indisponível para importar a foto.")
        val projeto = FacaProjetoArquivo.ler(origem, File(raiz, "moldevetor_fotos"))
        val dados = projeto.manifesto

        try {
            // Valida também o conteúdo vetorial antes de alterar o banco.
            dados.contornoJson?.let(ContornoJsonMapper::deJson)
            dados.encaixeJson?.let(EncaixeJsonMapper::deJson)
            dados.areaAnteriorJson?.let(::JSONArray)

            val existente = dao.getByCodigo(dados.codigo)
            if (existente != null && !sobrescreverExistente) {
                throw BlocoImportacaoConflitoException(dados.codigo, dados.nome)
            }

            val agora = System.currentTimeMillis()
            val fotoNova = projeto.fotoTemporaria?.absolutePath
            val entidade = if (existente == null) {
                FacaMoldeEntity(
                    codigo = dados.codigo.trim(),
                    nome = dados.nome.trim(),
                    referenciaModelo = dados.referenciaModelo,
                    observacoes = dados.observacoes,
                    status = FacaMoldeEntity.STATUS_ATIVA,
                    contornoJson = dados.contornoJson,
                    areaMm2 = dados.areaMm2,
                    areaM2 = dados.areaM2,
                    areaAnteriorJson = dados.areaAnteriorJson,
                    encaixeJson = dados.encaixeJson,
                    consumoEncaixeM2 = dados.consumoEncaixeM2,
                    fotoPath = fotoNova,
                    createdAt = dados.createdAt,
                    updatedAt = agora,
                )
            } else {
                existente.copy(
                    codigo = dados.codigo.trim(),
                    nome = dados.nome.trim(),
                    referenciaModelo = dados.referenciaModelo,
                    observacoes = dados.observacoes,
                    status = FacaMoldeEntity.STATUS_ATIVA,
                    contornoJson = dados.contornoJson,
                    areaMm2 = dados.areaMm2,
                    areaM2 = dados.areaM2,
                    areaAnteriorJson = dados.areaAnteriorJson,
                    encaixeJson = dados.encaixeJson,
                    consumoEncaixeM2 = dados.consumoEncaixeM2,
                    fotoPath = fotoNova,
                    updatedAt = agora,
                )
            }

            val id = if (existente == null) dao.insert(entidade) else {
                dao.update(entidade)
                existente.id
            }
            if (existente != null && existente.fotoPath != fotoNova) removerFotoInterna(existente.fotoPath)
            return BlocoImportadoResultado(id, entidade.codigo, entidade.nome)
        } catch (erro: Exception) {
            projeto.fotoTemporaria?.delete()
            throw erro
        }
    }

    /** Grava o resultado de uma vetorização (Fases 3-4: captura → detecção → validação) — chamado
     * só depois que o usuário confirma o contorno (com ou sem ajuste manual de vértices, Fase 4).
     * Mantém a área anterior em [FacaMoldeEntity.areaAnteriorJson] como histórico (pedido do
     * Tiago: "manter histórico da área anterior" ao revetorizar uma faca já cadastrada). */
    suspend fun salvarResultadoVetorizacao(id: Long, contorno: Contorno, encaixe: EncaixeConsumo, fotoPath: String?) {
        val atual = dao.getById(id) ?: throw IllegalStateException("Faca não encontrada (id=$id).")
        val areaMm2 = CalculoArea.calcularAreaMm2(contorno)
        val areaM2 = CalculoArea.mm2ParaM2(areaMm2)
        val fotoFinal = fotoPath ?: atual.fotoPath

        val historico = JSONArray(atual.areaAnteriorJson ?: "[]")
        if (atual.areaMm2 != null) historico.put(atual.areaMm2)

        dao.update(
            atual.copy(
                contornoJson = ContornoJsonMapper.paraJson(contorno),
                areaMm2 = areaMm2,
                areaM2 = areaM2,
                areaAnteriorJson = historico.toString(),
                encaixeJson = EncaixeJsonMapper.paraJson(encaixe),
                consumoEncaixeM2 = encaixe.consumoPorBlocoM2,
                fotoPath = fotoFinal,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        if (fotoPath != null && atual.fotoPath != fotoFinal) removerFotoInterna(atual.fotoPath)
    }

    private fun removerFotoInterna(caminho: String?) {
        val raiz = filesDir ?: return
        val arquivo = caminho?.let(::File) ?: return
        runCatching {
            val raizCanonica = raiz.canonicalFile
            val alvoCanonico = arquivo.canonicalFile
            if (alvoCanonico.path.startsWith(raizCanonica.path + File.separator) && alvoCanonico.isFile) {
                alvoCanonico.delete()
            }
        }
    }
}

private fun FacaMoldeEntity.toSummary() = FacaMoldeSummary(
    id = id,
    codigo = codigo,
    nome = nome,
    referenciaModelo = referenciaModelo,
    status = status,
    areaM2 = areaM2,
    consumoEncaixeM2 = consumoEncaixeM2,
    updatedAt = updatedAt,
    contornoJson = contornoJson,
)
