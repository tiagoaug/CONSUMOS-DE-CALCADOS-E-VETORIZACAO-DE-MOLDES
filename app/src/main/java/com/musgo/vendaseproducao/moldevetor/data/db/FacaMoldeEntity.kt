package com.musgo.vendaseproducao.moldevetor.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Uma linha por faca de corte (molde) cadastrada. Nesta Fase 1 só os campos de identificação
 * são usados de verdade; os campos de foto/contorno/área ficam reservados pro schema já nascer
 * pronto pras Fases 2-4 (evita migração de Room logo na sequência). [contornoJson] e
 * [areaAnteriorJson] guardam listas serializadas (JSON simples via org.json, sem dependência de
 * kotlinx.serialization nesta entidade) — [areaAnteriorJson] é o HISTÓRICO de áreas anteriores
 * (pedido explícito do Tiago: "manter histórico da área anterior" ao editar o contorno). */
@Entity(tableName = "faca_moldes")
data class FacaMoldeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val nome: String,
    val referenciaModelo: String? = null,
    val observacoes: String? = null,
    // ATIVA | INATIVA — exclusão lógica por padrão (ver FacaMoldeRepository.excluirLogico).
    val status: String = STATUS_ATIVA,
    val contornoJson: String? = null,
    val areaMm2: Double? = null,
    val areaM2: Double? = null,
    val areaAnteriorJson: String? = null,
    // Encaixe/infesto é independente da área real: guarda a célula repetível e o consumo
    // efetivo por bloco calculado a partir dela.
    val encaixeJson: String? = null,
    val consumoEncaixeM2: Double? = null,
    val fotoPath: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        const val STATUS_ATIVA = "ATIVA"
        const val STATUS_INATIVA = "INATIVA"
    }
}
