package br.com.eduponto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HorarioEntity::class, ChamadaEntity::class, PresencaEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun horarioDao(): HorarioDao
    abstract fun chamadaDao(): ChamadaDao
    abstract fun presencaDao(): PresencaDao

    companion object {
        fun criar(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "eduponto.db"
            )
                // Versão de testes: ao mudar o esquema, o banco é recriado (dados antigos são apagados).
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
