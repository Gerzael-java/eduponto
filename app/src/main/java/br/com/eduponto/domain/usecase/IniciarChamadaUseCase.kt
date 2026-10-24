package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.Chamada
import br.com.eduponto.domain.model.Horario
import br.com.eduponto.domain.repository.ChamadaRepository
import java.security.SecureRandom

/** Abre uma chamada para um horário. Só pode haver uma chamada em andamento por professor. */
class IniciarChamadaUseCase(private val chamadas: ChamadaRepository) {

    sealed interface Resultado {
        data class Sucesso(val chamadaId: Long) : Resultado
        data class Erro(val mensagem: String) : Resultado
    }

    suspend operator fun invoke(horario: Horario): Resultado {
        val agora = System.currentTimeMillis()
        val emAndamento = chamadas.buscarAtiva(horario.professor)
        if (emAndamento != null) {
            if (emAndamento.expiraEm < agora) {
                // A chamada anterior já venceu e ficou aberta: encerra no horário em que venceu.
                chamadas.encerrar(emAndamento.id, emAndamento.expiraEm)
            } else {
                return Resultado.Erro("Encerre a chamada em andamento antes de iniciar outra.")
            }
        }
        val id = chamadas.criar(
            Chamada(
                horarioId = horario.id,
                professor = horario.professor,
                disciplina = horario.disciplina,
                turma = horario.turma,
                codigo = gerarCodigo(),
                inicioEm = agora,
                expiraEm = agora + DURACAO_MS
            )
        )
        return Resultado.Sucesso(id)
    }

    private fun gerarCodigo(): String {
        val aleatorio = SecureRandom()
        return buildString {
            repeat(TAMANHO_CODIGO) { append(ALFABETO[aleatorio.nextInt(ALFABETO.length)]) }
        }
    }

    private companion object {
        const val DURACAO_MS = 20L * 60L * 1000L
        const val TAMANHO_CODIGO = 6
        const val ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
