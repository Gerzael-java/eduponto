package br.com.eduponto.data.repository

import br.com.eduponto.data.local.HorarioDao
import br.com.eduponto.data.local.paraDominio
import br.com.eduponto.data.local.paraEntidade
import br.com.eduponto.domain.model.Horario
import br.com.eduponto.domain.repository.HorarioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HorarioRepositoryRoom(private val dao: HorarioDao) : HorarioRepository {

    override fun observarDoProfessor(professor: String): Flow<List<Horario>> =
        dao.observarDoProfessor(professor).map { lista -> lista.map { it.paraDominio() } }

    override suspend fun salvar(horario: Horario) {
        dao.inserir(horario.paraEntidade())
    }

    override suspend fun excluir(id: Long) {
        dao.excluir(id)
    }
}
