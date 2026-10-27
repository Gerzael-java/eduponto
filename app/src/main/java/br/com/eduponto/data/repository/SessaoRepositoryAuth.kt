package br.com.eduponto.data.repository

import br.com.eduponto.domain.model.AuthState
import br.com.eduponto.domain.model.Usuario
import br.com.eduponto.domain.repository.AuthRepository
import br.com.eduponto.domain.repository.SessaoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Ponte entre o login real e as telas que ainda usam o modelo simples de usuário. */
class SessaoRepositoryAuth(
    private val auth: AuthRepository,
    private val escopo: CoroutineScope
) : SessaoRepository {

    override val usuario: StateFlow<Usuario?> = auth.state
        .map { estado ->
            (estado as? AuthState.SignedIn)?.let {
                Usuario(nome = it.profile.fullName, perfil = it.activeRole)
            }
        }
        .stateIn(escopo, SharingStarted.Eagerly, null)

    override fun sair() {
        escopo.launch { auth.signOut() }
    }
}
