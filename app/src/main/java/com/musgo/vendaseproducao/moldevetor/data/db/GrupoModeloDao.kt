package com.musgo.vendaseproducao.moldevetor.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GrupoModeloDao {

    @Query("SELECT * FROM grupos_modelos ORDER BY nome COLLATE NOCASE")
    fun observeAll(): Flow<List<GrupoModeloEntity>>

    @Query("SELECT * FROM grupos_modelos WHERE id = :id")
    suspend fun getById(id: Long): GrupoModeloEntity?

    @Query("SELECT * FROM grupos_modelos WHERE nome = :nome COLLATE NOCASE LIMIT 1")
    suspend fun getByNome(nome: String): GrupoModeloEntity?

    @Insert
    suspend fun insert(entity: GrupoModeloEntity): Long

    @Update
    suspend fun update(entity: GrupoModeloEntity)

    @Query("UPDATE faca_moldes SET grupoModeloId = NULL, updatedAt = :updatedAt WHERE grupoModeloId = :grupoId")
    suspend fun removerAssociacoes(grupoId: Long, updatedAt: Long)

    @Query("DELETE FROM grupos_modelos WHERE id = :id")
    suspend fun delete(id: Long)
}
