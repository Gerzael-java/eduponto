package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.Perfil
import br.com.eduponto.domain.model.Presenca
import br.com.eduponto.domain.repository.ChamadaRepository
import br.com.eduponto.domain.repository.PresencaRepository
import br.com.eduponto.qrcode.QrPayload
import br.com.eduponto.qrcode.QrPayloadCodec

/** Lado do professor: valida o comprovante do aluno e confirma a presença na chamada aberta. */
class RegistrarComprovanteUseCase(
    private val chamadas: ChamadaRepository,
    private val presencas: PresencaRepository
) {

    sealed interface Resultado {
        data class Sucesso(val mensagem: String) : Resultado
        data class Erro(val mensagem: String) : Resultado
    }

    suspend operator fun invoke(chamadaId: Long, texto: String): Resultado {
        val chamada = chamadas.buscarPorId(chamadaId)
            ?: return Resultado.Erro("Chamada não encontrada.")
        if (!chamada.ativa) {
            return Resultado.Erro("A chamada já foi encerrada.")
        }

        val comprovante = QrPayloadCodec.decodificar(texto) as? QrPayload.Comprovante
            ?: return Resultado.Erro("Este não é um comprovante de presença do EduPonto.")

        if (comprovante.codigo != chamada.codigo) {
            return Resultado.Erro("Este comprovante é de outra chamada.")
        }
        val aluno = comprovante.aluno.trim()
        if (aluno.isEmpty()) {
            return Resultado.Erro("Comprovante sem nome de aluno.")
        }
        val cedo = comprovante.registradoEm < chamada.inicioEm - TOLERANCIA_MS
        val tarde = comprovante.registradoEm > chamada.expiraEm + TOLERANCIA_MS
        if (cedo || tarde) {
            return Resultado.Erro("Comprovante fora do período desta chamada.")
        }

        val nova = Presenca(
            codigoChamada = chamada.codigo,
            professor = chamada.professor,
            disciplina = chamada.disciplina,
            turma = chamada.turma,
            aluno = aluno,
            registradoEm = comprovante.registradoEm,
            perfil = Perfil.PROFESSOR
        )
        return if (presencas.registrar(nova)) {
            Resultado.Sucesso("Presença de $aluno confirmada.")
        } else {
            Resultado.Erro("$aluno já está na lista desta chamada.")
        }
    }

    private companion object {
        const val TOLERANCIA_MS = 2L * 60L * 1000L
    }
}
