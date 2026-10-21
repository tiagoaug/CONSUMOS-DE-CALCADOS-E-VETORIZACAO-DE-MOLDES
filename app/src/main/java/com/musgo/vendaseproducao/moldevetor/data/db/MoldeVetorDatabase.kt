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
@Database(
    entities = [FacaMoldeEntity::class, GrupoModeloEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class MoldeVetorDatabase : RoomDatabase() {

    abstract fun facaMoldeDao(): FacaMoldeDao
    abstract fun grupoModeloDao(): GrupoModeloDao

    companion object {
        @Volatile private var instance: MoldeVetorDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE faca_moldes ADD COLUMN encaixeJson TEXT")
                db.execSQL("ALTER TABLE faca_moldes ADD COLUMN consumoEncaixeM2 REAL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS grupos_modelos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        descricao TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("ALTER TABLE faca_moldes ADD COLUMN grupoModeloId INTEGER")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE faca_moldes ADD COLUMN corCapaArgb INTEGER NOT NULL DEFAULT -16777216")
            }
        }

        fun getInstance(context: Context): MoldeVetorDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoldeVetorDatabase::class.java,
                    "molde_vetor.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
            }
    }
}
