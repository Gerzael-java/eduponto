package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.Chamada
import br.com.eduponto.domain.model.Horario
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IniciarChamadaUseCaseTest {

    private val horario = Horario(
        id = 1,
        professor = "Prof",
        disciplina = "Disciplina X",
        turma = "T1",
        diaSemana = DayOfWeek.MONDAY,
        inicio = LocalTime.of(8, 0),
        fim = LocalTime.of(9, 0)
    )

    private fun chamadaEmAndamento(expiraEm: Long) = Chamada(
        horarioId = 1,
        professor = "Prof",
        disciplina = "Disciplina X",
        turma = "T1",
        codigo = "ABC234",
        inicioEm = expiraEm - 1_200_000L,
        expiraEm = expiraEm
    )

    @Test
    fun `inicia uma chamada quando nao ha outra aberta`() = runBlocking {
        val repo = FakeChamadaRepository()
        val resultado = IniciarChamadaUseCase(repo)(horario)

        assertTrue(resultado is IniciarChamadaUseCase.Resultado.Sucesso)
        val chamada = repo.chamadas.single()
        assertEquals(6, chamada.codigo.length)
        assertTrue(chamada.expiraEm > chamada.inicioEm)
        assertNull(chamada.encerradaEm)
    }

    @Test
    fun `recusa iniciar outra enquanto ha uma valida em andamento`() = runBlocking {
        val repo = FakeChamadaRepository()
        repo.criar(chamadaEmAndamento(expiraEm = System.currentTimeMillis() + 600_000L))

        val resultado = IniciarChamadaUseCase(repo)(horario)

        assertTrue(resultado is IniciarChamadaUseCase.Resultado.Erro)
        assertEquals(1, repo.chamadas.size)
    }

    @Test
    fun `encerra a chamada vencida e inicia a nova`() = runBlocking {
        val repo = FakeChamadaRepository()
        val vencimento = System.currentTimeMillis() - 60_000L
        repo.criar(chamadaEmAndamento(expiraEm = vencimento))

        val resultado = IniciarChamadaUseCase(repo)(horario)

        assertTrue(resultado is IniciarChamadaUseCase.Resultado.Sucesso)
        assertEquals(2, repo.chamadas.size)
        assertEquals(vencimento, repo.chamadas.first().encerradaEm)
        assertNotNull(repo.chamadas.last().codigo)
    }

    @Test
    fun `os codigos gerados usam apenas caracteres sem ambiguidade`() = runBlocking {
        val repo = FakeChamadaRepository()
        repeat(20) {
            IniciarChamadaUseCase(repo)(horario)
            repo.encerrar(repo.chamadas.last().id, 1L)
        }

        val permitidos = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toSet()
        assertTrue(repo.chamadas.all { c -> c.codigo.all { it in permitidos } })
    }
}
