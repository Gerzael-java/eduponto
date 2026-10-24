package br.com.eduponto.domain.repository

import br.com.eduponto.domain.model.Chamada
import kotlinx.coroutines.flow.Flow

interface ChamadaRepository {
    suspend fun criar(chamada: Chamada): Long
    suspend fun buscarPorId(id: Long): Chamada?
    suspend fun buscarAtiva(professor: String): Chamada?
    suspend fun encerrar(id: Long, agora: Long)
    fun observar(id: Long): Flow<Chamada?>
    fun observarAtiva(professor: String): Flow<Chamada?>
    fun observarDoProfessor(professor: String): Flow<List<Chamada>>
}
