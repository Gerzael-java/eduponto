package br.com.eduponto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HorarioDao {

    @Query(
        "SELECT * FROM horarios WHERE professor = :professor " +
            "ORDER BY diaSemana, inicioMinutos"
    )
    fun observarDoProfessor(professor: String): Flow<List<HorarioEntity>>

    @Insert
    suspend fun inserir(horario: HorarioEntity): Long

    @Query("DELETE FROM horarios WHERE id = :id")
    suspend fun excluir(id: Long)
}
