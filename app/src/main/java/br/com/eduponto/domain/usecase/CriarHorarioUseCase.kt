package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.Horario
import br.com.eduponto.domain.repository.HorarioRepository
import java.time.DayOfWeek
import java.time.LocalTime

/** Valida os dados de um novo horário e o grava no banco local. */
class CriarHorarioUseCase(private val repositorio: HorarioRepository) {

    sealed interface Resultado {
        data object Sucesso : Resultado
        data class Erro(val mensagem: String) : Resultado
    }

    suspend operator fun invoke(
        professor: String,
        disciplina: String,
        turma: String,
        diaSemana: DayOfWeek,
        inicio: LocalTime,
        fim: LocalTime
    ): Resultado {
        val disciplinaLimpa = disciplina.trim()
        val turmaLimpa = turma.trim()

        if (disciplinaLimpa.isEmpty()) return Resultado.Erro("Informe a disciplina.")
        if (disciplinaLimpa.length > LIMITE_TEXTO) {
            return Resultado.Erro("A disciplina deve ter no máximo $LIMITE_TEXTO caracteres.")
        }
        if (turmaLimpa.isEmpty()) return Resultado.Erro("Informe a turma.")
        if (turmaLimpa.length > LIMITE_TEXTO) {
            return Resultado.Erro("A turma deve ter no máximo $LIMITE_TEXTO caracteres.")
        }
        if (!fim.isAfter(inicio)) {
            return Resultado.Erro("O término deve ser depois do início.")
        }

        repositorio.salvar(
            Horario(
                professor = professor,
                disciplina = disciplinaLimpa,
                turma = turmaLimpa,
                diaSemana = diaSemana,
                inicio = inicio,
                fim = fim
            )
        )
        return Resultado.Sucesso
    }

    private companion object {
        const val LIMITE_TEXTO = 60
    }
}
