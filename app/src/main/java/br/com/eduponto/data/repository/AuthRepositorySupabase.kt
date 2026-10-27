package br.com.eduponto.data.repository

import br.com.eduponto.domain.model.AuthError
import br.com.eduponto.domain.model.AuthResult
import br.com.eduponto.domain.model.AuthState
import br.com.eduponto.domain.model.BlockReason
import br.com.eduponto.domain.model.EscolhaDePerfil
import br.com.eduponto.domain.model.Perfil
import br.com.eduponto.domain.model.UserProfile
import br.com.eduponto.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class LinhaUsuario(
    val id: String,
    @SerialName("full_name") val nomeCompleto: String,
    val status: String,
    @SerialName("must_change_password") val trocarSenha: Boolean
)

@Serializable
private data class LinhaPapel(val role: String)

/** Login e perfil reais, via Supabase. Os papéis vêm do servidor. */
class AuthRepositorySupabase(
    private val cliente: SupabaseClient?,
    escopo: CoroutineScope
) : AuthRepository {

    private val _state = MutableStateFlow<AuthState>(
        if (cliente == null) AuthState.NotConfigured else AuthState.Loading
    )
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        if (cliente != null) {
            escopo.launch {
                cliente.auth.sessionStatus.collect { status ->
                    _state.value = when (status) {
                        is SessionStatus.Authenticated -> {
                            val idUsuario = status.session.user?.id
                            if (idUsuario == null) {
                                AuthState.SignedOut
                            } else {
                                carregarEstado(cliente, idUsuario)
                            }
                        }
                        is SessionStatus.Initializing -> AuthState.Loading
                        else -> AuthState.SignedOut
                    }
                }
            }
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        val c = cliente ?: return AuthResult.Failure(AuthError.NOT_CONFIGURED)
        return try {
            c.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            AuthResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure(mapearErro(e, aoEntrar = true))
        }
    }

    override suspend fun changePassword(newPassword: String): AuthResult {
        val c = cliente ?: return AuthResult.Failure(AuthError.NOT_CONFIGURED)
        return try {
            c.auth.updateUser { password = newPassword }
            c.postgrest.rpc("mark_password_changed")
            reloadProfile()
            AuthResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure(mapearErro(e, aoEntrar = false))
        }
    }

    override suspend fun reloadProfile() {
        val c = cliente ?: return
        val idUsuario = c.auth.currentUserOrNull()?.id
        if (idUsuario == null) {
            _state.value = AuthState.SignedOut
            return
        }
        _state.value = AuthState.Loading
        _state.value = carregarEstado(c, idUsuario)
    }

    override suspend fun signOut() {
        val c = cliente ?: return
        try {
            c.auth.signOut()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sem rede, o encerramento no servidor falha; o app volta ao login mesmo assim.
            _state.value = AuthState.SignedOut
        }
    }

    private suspend fun carregarEstado(c: SupabaseClient, idUsuario: String): AuthState {
        return try {
            val linha = c.from("users")
                .select(Columns.list("id", "full_name", "status", "must_change_password")) {
                    filter { eq("id", idUsuario) }
                }
                .decodeSingleOrNull<LinhaUsuario>()
                ?: return AuthState.Blocked(BlockReason.NO_PROFILE)

            if (linha.status != "active") {
                return AuthState.Blocked(BlockReason.INACTIVE)
            }

            val papeis = c.from("user_roles")
                .select(Columns.list("role")) {
                    filter { eq("user_id", idUsuario) }
                }
                .decodeList<LinhaPapel>()
                .mapNotNull { papelDe(it.role) }
                .toSet()

            val ativo = EscolhaDePerfil.escolher(papeis)
                ?: return AuthState.Blocked(BlockReason.NO_ROLE)

            val perfil = UserProfile(
                id = idUsuario,
                fullName = linha.nomeCompleto,
                roles = papeis,
                mustChangePassword = linha.trocarSenha
            )
            if (perfil.mustChangePassword) {
                AuthState.MustChangePassword(perfil)
            } else {
                AuthState.SignedIn(perfil, ativo)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthState.Blocked(BlockReason.LOAD_FAILED)
        }
    }

    private fun papelDe(texto: String): Perfil? = when (texto) {
        "admin" -> Perfil.ADMINISTRADOR
        "teacher" -> Perfil.PROFESSOR
        "student" -> Perfil.ALUNO
        else -> null
    }

    private fun mapearErro(e: Exception, aoEntrar: Boolean): AuthError = when (e) {
        is RestException -> when (e.statusCode) {
            400, 401 -> if (aoEntrar) AuthError.INVALID_CREDENTIALS else AuthError.UNKNOWN
            422 -> AuthError.PASSWORD_REJECTED
            429 -> AuthError.TOO_MANY_REQUESTS
            else -> AuthError.UNKNOWN
        }
        is HttpRequestException -> AuthError.NO_CONNECTION
        is IOException -> AuthError.NO_CONNECTION
        else -> AuthError.UNKNOWN
    }
}
