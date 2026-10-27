package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.AuthError
import br.com.eduponto.domain.model.AuthResult
import br.com.eduponto.domain.repository.AuthRepository

/** Regras de senha do app (o servidor também aplica as dele). */
class ChangePasswordUseCase(private val auth: AuthRepository) {

    suspend operator fun invoke(novaSenha: String, confirmacao: String): AuthResult {
        if (novaSenha.length < TAMANHO_MINIMO) {
            return AuthResult.Failure(AuthError.PASSWORD_TOO_SHORT)
        }
        val temLetra = novaSenha.any { it.isLetter() }
        val temNumero = novaSenha.any { it.isDigit() }
        if (!temLetra || !temNumero) {
            return AuthResult.Failure(AuthError.PASSWORD_NEEDS_LETTER_AND_DIGIT)
        }
        if (novaSenha != confirmacao) {
            return AuthResult.Failure(AuthError.PASSWORD_MISMATCH)
        }
        return auth.changePassword(novaSenha)
    }

    private companion object {
        const val TAMANHO_MINIMO = 8
    }
}
