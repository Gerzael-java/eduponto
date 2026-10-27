package br.com.eduponto.domain.model

/** Dados do usuário autenticado (vêm do servidor; nunca do próprio app). */
data class UserProfile(
    val id: String,
    val fullName: String,
    val roles: Set<Perfil>,
    val mustChangePassword: Boolean
)

enum class BlockReason {
    NO_PROFILE,
    INACTIVE,
    NO_ROLE,
    LOAD_FAILED
}

sealed interface AuthState {
    data object Loading : AuthState
    data object NotConfigured : AuthState
    data object SignedOut : AuthState
    data class MustChangePassword(val profile: UserProfile) : AuthState
    data class Blocked(val reason: BlockReason) : AuthState
    data class SignedIn(val profile: UserProfile, val activeRole: Perfil) : AuthState
}

enum class AuthError {
    EMAIL_INVALID,
    PASSWORD_REQUIRED,
    INVALID_CREDENTIALS,
    NO_CONNECTION,
    TOO_MANY_REQUESTS,
    NOT_CONFIGURED,
    PASSWORD_TOO_SHORT,
    PASSWORD_NEEDS_LETTER_AND_DIGIT,
    PASSWORD_MISMATCH,
    PASSWORD_REJECTED,
    UNKNOWN
}

sealed interface AuthResult {
    data object Success : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}
