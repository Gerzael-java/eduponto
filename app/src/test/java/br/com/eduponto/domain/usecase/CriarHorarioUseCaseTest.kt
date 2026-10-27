package br.com.eduponto.domain.usecase

import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CriarHorarioUseCaseTest {

    private val inicio = LocalTime.of(8, 0)
    private val fim = LocalTime.of(9, 0)

    private fun criar(
        repositorio: FakeHorarioRepository,
        disciplina: String = "Disciplina X",
        turma: String = "T1",
        i: LocalTime = inicio,
        f: LocalTime = fim
    ) = runBlocking {
        CriarHorarioUseCase(repositorio)("Prof", disciplina, turma, DayOfWeek.MONDAY, i, f)
    }

    @Test
    fun `disciplina vazia e recusada`() {
        val repo = FakeHorarioRepository()
        val resultado = criar(repo, disciplina = "   ")

        assertTrue(resultado is CriarHorarioUseCase.Resultado.Erro)
        assertTrue(repo.salvos.isEmpty())
    }

    @Test
    fun `turma vazia e recusada`() {
        val repo = FakeHorarioRepository()

        assertTrue(criar(repo, turma = "") is CriarHorarioUseCase.Resultado.Erro)
        assertTrue(repo.salvos.isEmpty())
    }

    @Test
    fun `termino antes do inicio e recusado`() {
        val repo = FakeHorarioRepository()

        assertTrue(criar(repo, i = fim, f = inicio) is CriarHorarioUseCase.Resultado.Erro)
        assertTrue(repo.salvos.isEmpty())
    }

    @Test
    fun `termino igual ao inicio e recusado`() {
        val repo = FakeHorarioRepository()

        assertTrue(criar(repo, i = inicio, f = inicio) is CriarHorarioUseCase.Resultado.Erro)
    }

    @Test
    fun `texto muito longo e recusado`() {
        val repo = FakeHorarioRepository()

        assertTrue(criar(repo, disciplina = "a".repeat(61)) is CriarHorarioUseCase.Resultado.Erro)
    }

    @Test
    fun `horario valido e salvo com textos limpos`() {
        val repo = FakeHorarioRepository()
        val resultado = criar(repo, disciplina = "  Disciplina X ", turma = " T1 ")

        assertEquals(CriarHorarioUseCase.Resultado.Sucesso, resultado)
        assertEquals("Disciplina X", repo.salvos.single().disciplina)
        assertEquals("T1", repo.salvos.single().turma)
    }
}
