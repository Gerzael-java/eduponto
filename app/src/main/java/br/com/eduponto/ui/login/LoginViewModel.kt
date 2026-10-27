package br.com.eduponto.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.model.AuthError
import br.com.eduponto.domain.model.AuthResult
import br.com.eduponto.domain.usecase.SignInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val mostrarSenha: Boolean = false,
    val carregando: Boolean = false,
    val erro: AuthError? = null
)

class LoginViewModel(private val entrar: SignInUseCase) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun aoMudarEmail(valor: String) {
        _estado.update { it.copy(email = valor, erro = null) }
    }

    fun aoMudarSenha(valor: String) {
        _estado.update { it.copy(senha = valor, erro = null) }
    }

    fun alternarSenhaVisivel() {
        _estado.update { it.copy(mostrarSenha = !it.mostrarSenha) }
    }

    fun aoEntrar() {
        val atual = _estado.value
        if (atual.carregando) return
        _estado.update { it.copy(carregando = true, erro = null) }

        viewModelScope.launch {
            when (val resultado = entrar(atual.email, atual.senha)) {
                // A tela troca sozinha quando a sessão abre; limpa o que foi digitado.
                is AuthResult.Success -> _estado.value = LoginUiState()
                is AuthResult.Failure -> _estado.update {
                    it.copy(carregando = false, erro = resultado.error, senha = "")
                }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { LoginViewModel(container.signIn) }
        }
    }
}
