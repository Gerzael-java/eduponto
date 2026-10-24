package br.com.eduponto.domain.repository

import br.com.eduponto.domain.model.Horario
import kotlinx.coroutines.flow.Flow

interface HorarioRepository {
    fun observarDoProfessor(professor: String): Flow<List<Horario>>
    suspend fun salvar(horario: Horario)
    suspend fun excluir(id: Long)
}
