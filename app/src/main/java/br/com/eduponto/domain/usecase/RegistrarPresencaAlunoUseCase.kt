package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.Perfil
import br.com.eduponto.domain.model.Presenca
import br.com.eduponto.domain.repository.PresencaRepository
import br.com.eduponto.qrcode.QrPayload
import br.com.eduponto.qrcode.QrPayloadCodec

/** Lado do aluno: valida o QR Code da chamada e registra a presença no próprio aparelho. */
class RegistrarPresencaAlunoUseCase(private val presencas: PresencaRepository) {

    sealed interface Resultado {
        data class Sucesso(val presenca: Presenca, val jaRegistrada: Boolean) : Resultado
        data class Erro(val mensagem: String) : Resultado
    }

    suspend operator fun invoke(
        aluno: String,
        texto: String,
        agora: Long = System.currentTimeMillis()
    ): Resultado {
        val chamada = QrPayloadCodec.decodificar(texto) as? QrPayload.Chamada
            ?: return Resultado.Erro("Este não é um QR Code de chamada do EduPonto.")

        if (agora > chamada.expiraEm) {
            return Resultado.Erro("Esta chamada já expirou.")
        }
        if (agora < chamada.inicioEm - TOLERANCIA_MS) {
            return Resultado.Erro("O relógio do seu celular parece estar errado.")
        }

        val nova = Presenca(
            codigoChamada = chamada.codigo,
            professor = chamada.professor,
            disciplina = chamada.disciplina,
            turma = chamada.turma,
            aluno = aluno,
            registradoEm = agora,
            perfil = Perfil.ALUNO
        )

        if (presencas.registrar(nova)) {
            return Resultado.Sucesso(nova, jaRegistrada = false)
        }
        val existente = presencas.buscar(chamada.codigo, aluno, Perfil.ALUNO)
            ?: return Resultado.Erro("Não foi possível registrar a presença.")
        return Resultado.Sucesso(existente, jaRegistrada = true)
    }

    private companion object {
        const val TOLERANCIA_MS = 2L * 60L * 1000L
    }
}
