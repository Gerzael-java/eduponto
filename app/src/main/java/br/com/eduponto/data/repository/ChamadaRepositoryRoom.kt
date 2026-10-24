package br.com.eduponto.data.repository

import br.com.eduponto.data.local.ChamadaDao
import br.com.eduponto.data.local.paraDominio
import br.com.eduponto.data.local.paraEntidade
import br.com.eduponto.domain.model.Chamada
import br.com.eduponto.domain.repository.ChamadaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChamadaRepositoryRoom(private val dao: ChamadaDao) : ChamadaRepository {

    override suspend fun criar(chamada: Chamada): Long = dao.inserir(chamada.paraEntidade())

    override suspend fun buscarPorId(id: Long): Chamada? = dao.buscarPorId(id)?.paraDominio()

    override suspend fun buscarAtiva(professor: String): Chamada? =
        dao.buscarAtiva(professor)?.paraDominio()

    override suspend fun encerrar(id: Long, agora: Long) {
        dao.encerrar(id, agora)
    }

    override fun observar(id: Long): Flow<Chamada?> =
        dao.observar(id).map { it?.paraDominio() }

    override fun observarAtiva(professor: String): Flow<Chamada?> =
        dao.observarAtiva(professor).map { it?.paraDominio() }

    override fun observarDoProfessor(professor: String): Flow<List<Chamada>> =
        dao.observarDoProfessor(professor).map { lista -> lista.map { it.paraDominio() } }
}
