package br.com.eduponto.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.model.Perfil
import br.com.eduponto.domain.usecase.EntrarUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val nome: String = "",
    val perfil: Perfil = Perfil.PROFESSOR,
    val erro: String? = null
)

class LoginViewModel(private val entrar: EntrarUseCase) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun aoMudarNome(nome: String) {
        _estado.update { it.copy(nome = nome, erro = null) }
    }

    fun aoMudarPerfil(perfil: Perfil) {
        _estado.update { it.copy(perfil = perfil) }
    }

    fun aoEntrar() {
        val atual = _estado.value
        val resultado = entrar(atual.nome, atual.perfil)
        if (resultado is EntrarUseCase.Resultado.Erro) {
            _estado.update { it.copy(erro = resultado.mensagem) }
        }
        // Em caso de sucesso, a navegação reage à sessão aberta (ver EduPontoNavHost).
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { LoginViewModel(container.entrar) }
        }
    }
}
