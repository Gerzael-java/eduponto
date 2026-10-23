package br.com.eduponto.ui.sessao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.model.Usuario
import br.com.eduponto.domain.repository.SessaoRepository
import kotlinx.coroutines.flow.StateFlow

class SessaoViewModel(private val sessao: SessaoRepository) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sessao.usuario

    fun sair() {
        sessao.sair()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { SessaoViewModel(container.sessao) }
        }
    }
}
