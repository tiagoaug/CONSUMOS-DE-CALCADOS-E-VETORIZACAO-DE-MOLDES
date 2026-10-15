package com.musgo.vendaseproducao.moldevetor.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// exportSchema = false é deliberado: schema v1, sem histórico de migração ainda — mesmo
// critério do PrintStudioDatabase.kt. Banco PRÓPRIO (molde_vetor.db), não compartilha com
// PrintStudioDatabase nem com nenhum outro módulo nativo.
@Database(entities = [FacaMoldeEntity::class], version = 2, exportSchema = false)
abstract class MoldeVetorDatabase : RoomDatabase() {

    abstract fun facaMoldeDao(): FacaMoldeDao

    companion object {
        @Volatile private var instance: MoldeVetorDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE faca_moldes ADD COLUMN encaixeJson TEXT")
                db.execSQL("ALTER TABLE faca_moldes ADD COLUMN consumoEncaixeM2 REAL")
            }
        }

        fun getInstance(context: Context): MoldeVetorDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoldeVetorDatabase::class.java,
                    "molde_vetor.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
