package br.com.eduponto.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.model.AuthError
import br.com.eduponto.domain.model.AuthResult
import br.com.eduponto.domain.usecase.ChangePasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val novaSenha: String = "",
    val confirmacao: String = "",
    val mostrarSenha: Boolean = false,
    val carregando: Boolean = false,
    val erro: AuthError? = null
)

class ChangePasswordViewModel(
    private val trocarSenha: ChangePasswordUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(ChangePasswordUiState())
    val estado: StateFlow<ChangePasswordUiState> = _estado.asStateFlow()

    fun aoMudarNovaSenha(valor: String) {
        _estado.update { it.copy(novaSenha = valor, erro = null) }
    }

    fun aoMudarConfirmacao(valor: String) {
        _estado.update { it.copy(confirmacao = valor, erro = null) }
    }

    fun alternarSenhaVisivel() {
        _estado.update { it.copy(mostrarSenha = !it.mostrarSenha) }
    }

    fun aoSalvar() {
        val atual = _estado.value
        if (atual.carregando) return
        _estado.update { it.copy(carregando = true, erro = null) }

        viewModelScope.launch {
            when (val resultado = trocarSenha(atual.novaSenha, atual.confirmacao)) {
                is AuthResult.Success -> _estado.value = ChangePasswordUiState()
                is AuthResult.Failure -> _estado.update {
                    it.copy(carregando = false, erro = resultado.error)
                }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ChangePasswordViewModel(container.changePassword) }
        }
    }
}
