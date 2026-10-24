package br.com.eduponto.domain.repository

import br.com.eduponto.domain.model.Perfil
import br.com.eduponto.domain.model.Presenca
import kotlinx.coroutines.flow.Flow

interface PresencaRepository {
    /** Retorna true se gravou; false se já existia presença igual (mesma chamada, aluno e perfil). */
    suspend fun registrar(presenca: Presenca): Boolean
    suspend fun buscar(codigoChamada: String, aluno: String, perfil: Perfil): Presenca?
    fun observarDaChamada(codigoChamada: String): Flow<List<Presenca>>
    fun observarDoProfessor(professor: String): Flow<List<Presenca>>
    fun observarDoAluno(aluno: String): Flow<List<Presenca>>
}
