package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.AuthResult
import br.com.eduponto.domain.model.AuthState
import br.com.eduponto.domain.model.Chamada
import br.com.eduponto.domain.model.Horario
import br.com.eduponto.domain.repository.AuthRepository
import br.com.eduponto.domain.repository.ChamadaRepository
import br.com.eduponto.domain.repository.HorarioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf

/** Implementações em memória, só para os testes. */
class FakeAuthRepository(
    private val resultado: AuthResult = AuthResult.Success
) : AuthRepository {
    override val state: StateFlow<AuthState> = MutableStateFlow(AuthState.SignedOut)
    var chamadasSignIn = mutableListOf<Pair<String, String>>()
    var novasSenhas = mutableListOf<String>()

    override suspend fun signIn(email: String, password: String): AuthResult {
        chamadasSignIn.add(email to password)
        return resultado
    }

    override suspend fun changePassword(newPassword: String): AuthResult {
        novasSenhas.add(newPassword)
        return resultado
    }

    override suspend fun reloadProfile() = Unit
    override suspend fun signOut() = Unit
}

class FakeHorarioRepository : HorarioRepository {
    val salvos = mutableListOf<Horario>()

    override fun observarDoProfessor(professor: String): Flow<List<Horario>> = flowOf(salvos.toList())

    override suspend fun salvar(horario: Horario) {
        salvos.add(horario)
    }

    override suspend fun excluir(id: Long) {
        salvos.removeAll { it.id == id }
    }
}

class FakeChamadaRepository : ChamadaRepository {
    val chamadas = mutableListOf<Chamada>()
    private var proximoId = 1L

    override suspend fun criar(chamada: Chamada): Long {
        val id = proximoId++
        chamadas.add(chamada.copy(id = id))
        return id
    }

    override suspend fun buscarPorId(id: Long): Chamada? = chamadas.firstOrNull { it.id == id }

    override suspend fun buscarAtiva(professor: String): Chamada? =
        chamadas.firstOrNull { it.professor == professor && it.encerradaEm == null }

    override suspend fun encerrar(id: Long, agora: Long) {
        val indice = chamadas.indexOfFirst { it.id == id }
        if (indice >= 0) chamadas[indice] = chamadas[indice].copy(encerradaEm = agora)
    }

    override fun observar(id: Long): Flow<Chamada?> = flowOf(chamadas.firstOrNull { it.id == id })

    override fun observarAtiva(professor: String): Flow<Chamada?> =
        flowOf(chamadas.firstOrNull { it.professor == professor && it.encerradaEm == null })

    override fun observarDoProfessor(professor: String): Flow<List<Chamada>> =
        flowOf(chamadas.filter { it.professor == professor })
}
