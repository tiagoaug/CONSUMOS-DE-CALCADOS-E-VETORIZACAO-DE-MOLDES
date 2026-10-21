package com.musgo.vendaseproducao.moldevetor.data

import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeDao
import com.musgo.vendaseproducao.moldevetor.data.db.FacaMoldeEntity
import com.musgo.vendaseproducao.moldevetor.data.db.GrupoModeloDao
import com.musgo.vendaseproducao.moldevetor.data.db.GrupoModeloEntity
import com.musgo.vendaseproducao.moldevetor.domain.CalculoArea
import com.musgo.vendaseproducao.moldevetor.domain.Contorno
import com.musgo.vendaseproducao.moldevetor.domain.EncaixeConsumo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    val grupoModeloId: Long?,
    val grupoModeloNome: String?,
    val referenciaModelo: String?,
    val status: String,
    val areaM2: Double?,
    val consumoEncaixeM2: Double?,
    val corCapaArgb: Int,
    val updatedAt: Long,
    val contornoJson: String?,
)

class FacaMoldeCodigoDuplicadoException(codigo: String) :
    Exception("Já existe uma faca cadastrada com o código \"$codigo\".")

class GrupoModeloNomeDuplicadoException(nome: String) :
    Exception("Já existe um grupo de modelos com o nome \"$nome\".")

class BlocoImportacaoConflitoException(val codigo: String, val nome: String) :
    Exception("Já existe um bloco com o código \"$codigo\".")

data class BlocoImportadoResultado(val id: Long, val codigo: String, val nome: String)

class FacaMoldeRepository(
    private val dao: FacaMoldeDao,
    private val grupoDao: GrupoModeloDao,
    private val filesDir: File? = null,
) {

    fun observeSummaries(): Flow<List<FacaMoldeSummary>> =
        combine(dao.observeAll(), grupoDao.observeAll()) { facas, grupos ->
            val nomesPorId = grupos.associate { it.id to it.nome }
            facas.map { it.toSummary(nomesPorId[it.grupoModeloId]) }
        }

    fun observeGruposModelos(): Flow<List<GrupoModeloEntity>> = grupoDao.observeAll()

    suspend fun getById(id: Long): FacaMoldeEntity? = dao.getById(id)

    suspend fun salvarGrupoModelo(id: Long?, nome: String, descricao: String?): Long {
        val nomeTrim = nome.trim()
        require(nomeTrim.isNotBlank()) { "Informe o nome do grupo de modelos." }
        val existente = grupoDao.getByNome(nomeTrim)
        if (existente != null && existente.id != id) {
            throw GrupoModeloNomeDuplicadoException(nomeTrim)
        }

        val agora = System.currentTimeMillis()
        return if (id == null) {
            grupoDao.insert(
                GrupoModeloEntity(
                    nome = nomeTrim,
                    descricao = descricao?.trim()?.ifBlank { null },
                    createdAt = agora,
                    updatedAt = agora,
                ),
            )
        } else {
            val atual = grupoDao.getById(id)
                ?: throw IllegalStateException("Grupo de modelos não encontrado (id=$id).")
            grupoDao.update(
                atual.copy(
                    nome = nomeTrim,
                    descricao = descricao?.trim()?.ifBlank { null },
                    updatedAt = agora,
                ),
            )
            id
        }
    }

    suspend fun excluirGrupoModelo(id: Long) {
        grupoDao.removerAssociacoes(id, System.currentTimeMillis())
        grupoDao.delete(id)
    }

    /** Cria (id == null) ou edita uma faca. Valida unicidade de código ANTES de gravar — ver
     * campo "Código da faca (único, obrigatório)" na especificação. */
    suspend fun salvar(
        id: Long?,
        codigo: String,
        nome: String,
        grupoModeloId: Long?,
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
                    grupoModeloId = grupoModeloId,
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
                    grupoModeloId = grupoModeloId,
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

    /**
     * Exporta cadastro, vetores, blocos/camadas, áreas e foto num único `.bloco`. A versão 2
     * também inclui uma capa PNG do vetor, o encaixe em PDF e instruções JSON para o importador.
     */
    suspend fun exportarBloco(id: Long, destino: OutputStream) {
        val faca = dao.getById(id) ?: throw IllegalStateException("Bloco não encontrado (id=$id).")
        val grupoNome = faca.grupoModeloId?.let { grupoDao.getById(it)?.nome }
        val artefatos = FacaArtefatosExportacao.gerar(faca)
        FacaProjetoArquivo.escrever(faca, destino, grupoNome, artefatos)
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
            val grupoModeloId = dados.grupoModeloNome?.trim()?.takeIf { it.isNotBlank() }?.let { nomeGrupo ->
                grupoDao.getByNome(nomeGrupo)?.id ?: grupoDao.insert(
                    GrupoModeloEntity(
                        nome = nomeGrupo,
                        createdAt = agora,
                        updatedAt = agora,
                    ),
                )
            }
            val entidade = if (existente == null) {
                FacaMoldeEntity(
                    codigo = dados.codigo.trim(),
                    nome = dados.nome.trim(),
                    grupoModeloId = grupoModeloId,
                    referenciaModelo = dados.referenciaModelo,
                    observacoes = dados.observacoes,
                    status = FacaMoldeEntity.STATUS_ATIVA,
                    contornoJson = dados.contornoJson,
                    areaMm2 = dados.areaMm2,
                    areaM2 = dados.areaM2,
                    areaAnteriorJson = dados.areaAnteriorJson,
                    encaixeJson = dados.encaixeJson,
                    consumoEncaixeM2 = dados.consumoEncaixeM2,
                    corCapaArgb = dados.corCapaArgb,
                    fotoPath = fotoNova,
                    createdAt = dados.createdAt,
                    updatedAt = agora,
                )
            } else {
                existente.copy(
                    codigo = dados.codigo.trim(),
                    nome = dados.nome.trim(),
                    grupoModeloId = grupoModeloId,
                    referenciaModelo = dados.referenciaModelo,
                    observacoes = dados.observacoes,
                    status = FacaMoldeEntity.STATUS_ATIVA,
                    contornoJson = dados.contornoJson,
                    areaMm2 = dados.areaMm2,
                    areaM2 = dados.areaM2,
                    areaAnteriorJson = dados.areaAnteriorJson,
                    encaixeJson = dados.encaixeJson,
                    consumoEncaixeM2 = dados.consumoEncaixeM2,
                    corCapaArgb = dados.corCapaArgb,
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

    /** Salva somente vetorização/conferência. O encaixe existente é preservado: editar pontos
     * ou conferir o desvio nunca pode apagar a disposição já montada. */
    suspend fun salvarEtapaContorno(id: Long, contorno: Contorno, fotoPath: String?, corCapaArgb: Int? = null) {
        val atual = dao.getById(id) ?: throw IllegalStateException("Faca não encontrada (id=$id).")
        val areaMm2 = CalculoArea.calcularAreaMm2(contorno)
        val areaM2 = CalculoArea.mm2ParaM2(areaMm2)
        val fotoFinal = fotoPath ?: atual.fotoPath

        val historico = JSONArray(atual.areaAnteriorJson ?: "[]")
        if (atual.areaMm2 != null && kotlin.math.abs(atual.areaMm2 - areaMm2) > 1e-6) {
            historico.put(atual.areaMm2)
        }

        dao.update(
            atual.copy(
                contornoJson = ContornoJsonMapper.paraJson(contorno),
                areaMm2 = areaMm2,
                areaM2 = areaM2,
                areaAnteriorJson = historico.toString(),
                corCapaArgb = corCapaArgb ?: atual.corCapaArgb,
                fotoPath = fotoFinal,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        if (fotoPath != null && atual.fotoPath != fotoFinal) removerFotoInterna(atual.fotoPath)
    }

    /** Salva apenas o encaixe/consumo, incluindo toda a disposição, sem alterar o vetor. */
    suspend fun salvarEtapaEncaixe(id: Long, encaixe: EncaixeConsumo) {
        val atual = dao.getById(id) ?: throw IllegalStateException("Faca não encontrada (id=$id).")
        dao.update(
            atual.copy(
                encaixeJson = EncaixeJsonMapper.paraJson(encaixe),
                consumoEncaixeM2 = encaixe.consumoPorBlocoM2,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    /** Compatibilidade com o fluxo antigo que confirmava todas as etapas de uma só vez. */
    suspend fun salvarResultadoVetorizacao(id: Long, contorno: Contorno, encaixe: EncaixeConsumo, fotoPath: String?) {
        salvarEtapaContorno(id, contorno, fotoPath)
        salvarEtapaEncaixe(id, encaixe)
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

private fun FacaMoldeEntity.toSummary(grupoModeloNome: String?) = FacaMoldeSummary(
    id = id,
    codigo = codigo,
    nome = nome,
    grupoModeloId = grupoModeloId,
    grupoModeloNome = grupoModeloNome,
    referenciaModelo = referenciaModelo,
    status = status,
    areaM2 = areaM2,
    consumoEncaixeM2 = consumoEncaixeM2,
    corCapaArgb = corCapaArgb,
    updatedAt = updatedAt,
    contornoJson = contornoJson,
)
