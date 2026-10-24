package br.com.eduponto.alunos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.model.Presenca
import br.com.eduponto.domain.repository.SessaoRepository
import br.com.eduponto.domain.usecase.RegistrarPresencaAlunoUseCase
import br.com.eduponto.qrcode.QrPayloadCodec
import br.com.eduponto.qrcode.paraComprovante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LeitorAlunoEstado {
    data class Lendo(val erro: String? = null) : LeitorAlunoEstado
    data class Registrada(
        val presenca: Presenca,
        val comprovante: String,
        val jaRegistrada: Boolean
    ) : LeitorAlunoEstado
}

class LeitorAlunoViewModel(
    private val sessao: SessaoRepository,
    private val registrarPresenca: RegistrarPresencaAlunoUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow<LeitorAlunoEstado>(LeitorAlunoEstado.Lendo())
    val estado: StateFlow<LeitorAlunoEstado> = _estado.asStateFlow()

    private var processando = false

    fun aoLerTexto(texto: String) {
        if (processando || _estado.value is LeitorAlunoEstado.Registrada) return

        val aluno = sessao.usuario.value?.nome
        if (aluno == null) {
            _estado.value = LeitorAlunoEstado.Lendo("Sessão encerrada. Entre novamente.")
            return
        }

        processando = true
        viewModelScope.launch {
            _estado.value = when (val resultado = registrarPresenca(aluno, texto)) {
                is RegistrarPresencaAlunoUseCase.Resultado.Sucesso -> LeitorAlunoEstado.Registrada(
                    presenca = resultado.presenca,
                    comprovante = QrPayloadCodec.codificar(resultado.presenca.paraComprovante()),
                    jaRegistrada = resultado.jaRegistrada
                )
                is RegistrarPresencaAlunoUseCase.Resultado.Erro ->
                    LeitorAlunoEstado.Lendo(resultado.mensagem)
            }
            processando = false
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { LeitorAlunoViewModel(container.sessao, container.registrarPresencaAluno) }
        }
    }
}
