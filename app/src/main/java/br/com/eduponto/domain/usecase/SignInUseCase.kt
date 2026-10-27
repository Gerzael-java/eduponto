package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.AuthError
import br.com.eduponto.domain.model.AuthResult
import br.com.eduponto.domain.repository.AuthRepository

/** Valida os campos e pede o login ao servidor. */
class SignInUseCase(private val auth: AuthRepository) {

    suspend operator fun invoke(email: String, senha: String): AuthResult {
        val emailLimpo = email.trim()
        if (!EMAIL_REGEX.matches(emailLimpo)) {
            return AuthResult.Failure(AuthError.EMAIL_INVALID)
        }
        if (senha.isEmpty()) {
            return AuthResult.Failure(AuthError.PASSWORD_REQUIRED)
        }
        return auth.signIn(emailLimpo, senha)
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
