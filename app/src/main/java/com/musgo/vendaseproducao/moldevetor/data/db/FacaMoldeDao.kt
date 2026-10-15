package com.musgo.vendaseproducao.moldevetor.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FacaMoldeDao {

    @Query("SELECT * FROM faca_moldes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<FacaMoldeEntity>>

    @Query("SELECT * FROM faca_moldes WHERE id = :id")
    suspend fun getById(id: Long): FacaMoldeEntity?

    // Usado pra checar unicidade de código antes de criar/editar (ver
    // FacaMoldeRepository.codigoDisponivel) e pra casar por código na importação do lado Web
    // (Fase 6) — aqui só a leitura local importa.
    @Query("SELECT * FROM faca_moldes WHERE codigo = :codigo LIMIT 1")
    suspend fun getByCodigo(codigo: String): FacaMoldeEntity?

    @Insert
    suspend fun insert(entity: FacaMoldeEntity): Long

    @Update
    suspend fun update(entity: FacaMoldeEntity)

    @Query("UPDATE faca_moldes SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setStatus(id: Long, status: String, updatedAt: Long)

    // Exclusão FÍSICA — restrita a administradores no nível de UI (ver ListaFacasScreen), nunca
    // o caminho padrão (ver FacaMoldeEntity.STATUS_INATIVA).
    @Query("DELETE FROM faca_moldes WHERE id = :id")
    suspend fun deletePhysical(id: Long)
}
