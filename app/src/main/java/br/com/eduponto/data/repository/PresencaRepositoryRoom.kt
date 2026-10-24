package br.com.eduponto.data.repository

import br.com.eduponto.data.local.PresencaDao
import br.com.eduponto.data.local.paraDominio
import br.com.eduponto.data.local.paraEntidade
import br.com.eduponto.domain.model.Perfil
import br.com.eduponto.domain.model.Presenca
import br.com.eduponto.domain.repository.PresencaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PresencaRepositoryRoom(private val dao: PresencaDao) : PresencaRepository {

    override suspend fun registrar(presenca: Presenca): Boolean =
        dao.inserir(presenca.paraEntidade()) != -1L

    override suspend fun buscar(codigoChamada: String, aluno: String, perfil: Perfil): Presenca? =
        dao.buscar(codigoChamada, aluno, perfil.name)?.paraDominio()

    override fun observarDaChamada(codigoChamada: String): Flow<List<Presenca>> =
        dao.observarDaChamada(codigoChamada).map { lista -> lista.map { it.paraDominio() } }

    override fun observarDoProfessor(professor: String): Flow<List<Presenca>> =
        dao.observarDoProfessor(professor).map { lista -> lista.map { it.paraDominio() } }

    override fun observarDoAluno(aluno: String): Flow<List<Presenca>> =
        dao.observarDoAluno(aluno).map { lista -> lista.map { it.paraDominio() } }
}
