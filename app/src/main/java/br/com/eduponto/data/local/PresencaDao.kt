package br.com.eduponto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresencaDao {

    /** Retorna -1 quando já existe uma presença igual (índice único). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(presenca: PresencaEntity): Long

    @Query(
        "SELECT * FROM presencas WHERE codigoChamada = :codigo AND aluno = :aluno " +
            "AND perfil = :perfil LIMIT 1"
    )
    suspend fun buscar(codigo: String, aluno: String, perfil: String): PresencaEntity?

    @Query(
        "SELECT * FROM presencas WHERE codigoChamada = :codigo AND perfil = 'PROFESSOR' " +
            "ORDER BY registradoEm"
    )
    fun observarDaChamada(codigo: String): Flow<List<PresencaEntity>>

    @Query(
        "SELECT * FROM presencas WHERE professor = :professor AND perfil = 'PROFESSOR' " +
            "ORDER BY registradoEm"
    )
    fun observarDoProfessor(professor: String): Flow<List<PresencaEntity>>

    @Query(
        "SELECT * FROM presencas WHERE aluno = :aluno AND perfil = 'ALUNO' " +
            "ORDER BY registradoEm DESC"
    )
    fun observarDoAluno(aluno: String): Flow<List<PresencaEntity>>
}
