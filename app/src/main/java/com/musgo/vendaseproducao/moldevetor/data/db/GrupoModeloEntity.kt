package com.musgo.vendaseproducao.moldevetor.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Agrupa blocos que pertencem ao mesmo modelo de produto. */
@Entity(tableName = "grupos_modelos")
data class GrupoModeloEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val descricao: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
