package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.AuthError
import br.com.eduponto.domain.model.AuthResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangePasswordUseCaseTest {

    private fun falha(erro: AuthError) = AuthResult.Failure(erro)

    @Test
    fun `senha curta e recusada`() = runBlocking {
        val auth = FakeAuthRepository()
        val resultado = ChangePasswordUseCase(auth)("abc123", "abc123")

        assertEquals(falha(AuthError.PASSWORD_TOO_SHORT), resultado)
        assertTrue(auth.novasSenhas.isEmpty())
    }

    @Test
    fun `senha sem numero e recusada`() = runBlocking {
        val resultado = ChangePasswordUseCase(FakeAuthRepository())("somenteletras", "somenteletras")

        assertEquals(falha(AuthError.PASSWORD_NEEDS_LETTER_AND_DIGIT), resultado)
    }

    @Test
    fun `senha sem letra e recusada`() = runBlocking {
        val resultado = ChangePasswordUseCase(FakeAuthRepository())("12345678", "12345678")

        assertEquals(falha(AuthError.PASSWORD_NEEDS_LETTER_AND_DIGIT), resultado)
    }

    @Test
    fun `confirmacao diferente e recusada`() = runBlocking {
        val auth = FakeAuthRepository()
        val resultado = ChangePasswordUseCase(auth)("senhaboa123", "senhaboa124")

        assertEquals(falha(AuthError.PASSWORD_MISMATCH), resultado)
        assertTrue(auth.novasSenhas.isEmpty())
    }

    @Test
    fun `senha valida chama o servidor`() = runBlocking {
        val auth = FakeAuthRepository()
        val resultado = ChangePasswordUseCase(auth)("senhaboa123", "senhaboa123")

        assertEquals(AuthResult.Success, resultado)
        assertEquals(listOf("senhaboa123"), auth.novasSenhas)
    }
}
