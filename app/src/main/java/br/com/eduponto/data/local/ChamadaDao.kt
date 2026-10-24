package br.com.eduponto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChamadaDao {

    @Insert
    suspend fun inserir(chamada: ChamadaEntity): Long

    @Query("SELECT * FROM chamadas WHERE id = :id")
    suspend fun buscarPorId(id: Long): ChamadaEntity?

    @Query(
        "SELECT * FROM chamadas WHERE professor = :professor AND encerradaEm IS NULL " +
            "ORDER BY inicioEm DESC LIMIT 1"
    )
    suspend fun buscarAtiva(professor: String): ChamadaEntity?

    @Query("UPDATE chamadas SET encerradaEm = :agora WHERE id = :id AND encerradaEm IS NULL")
    suspend fun encerrar(id: Long, agora: Long)

    @Query("SELECT * FROM chamadas WHERE id = :id")
    fun observar(id: Long): Flow<ChamadaEntity?>

    @Query(
        "SELECT * FROM chamadas WHERE professor = :professor AND encerradaEm IS NULL " +
            "ORDER BY inicioEm DESC LIMIT 1"
    )
    fun observarAtiva(professor: String): Flow<ChamadaEntity?>

    @Query("SELECT * FROM chamadas WHERE professor = :professor ORDER BY inicioEm DESC")
    fun observarDoProfessor(professor: String): Flow<List<ChamadaEntity>>
}
