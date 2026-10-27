package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.AuthError
import br.com.eduponto.domain.model.AuthResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInUseCaseTest {

    @Test
    fun `e-mail invalido e recusado sem chamar o servidor`() = runBlocking {
        val auth = FakeAuthRepository()
        val resultado = SignInUseCase(auth)("isso-nao-e-email", "senha123")

        assertEquals(AuthResult.Failure(AuthError.EMAIL_INVALID), resultado)
        assertTrue(auth.chamadasSignIn.isEmpty())
    }

    @Test
    fun `senha vazia e recusada sem chamar o servidor`() = runBlocking {
        val auth = FakeAuthRepository()
        val resultado = SignInUseCase(auth)("ana@escola.org", "")

        assertEquals(AuthResult.Failure(AuthError.PASSWORD_REQUIRED), resultado)
        assertTrue(auth.chamadasSignIn.isEmpty())
    }

    @Test
    fun `espacos ao redor do e-mail sao removidos`() = runBlocking {
        val auth = FakeAuthRepository()
        SignInUseCase(auth)("  ana@escola.org  ", "senha123")

        assertEquals(listOf("ana@escola.org" to "senha123"), auth.chamadasSignIn)
    }

    @Test
    fun `a senha nao e alterada`() = runBlocking {
        val auth = FakeAuthRepository()
        SignInUseCase(auth)("ana@escola.org", "  senha com espaco ")

        assertEquals("  senha com espaco ", auth.chamadasSignIn.single().second)
    }

    @Test
    fun `devolve o resultado do servidor`() = runBlocking {
        val falha = AuthResult.Failure(AuthError.INVALID_CREDENTIALS)
        val resultado = SignInUseCase(FakeAuthRepository(falha))("ana@escola.org", "errada")

        assertEquals(falha, resultado)
    }
}
