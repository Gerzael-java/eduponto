package br.com.eduponto.presencas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.model.Presenca
import br.com.eduponto.domain.repository.PresencaRepository
import br.com.eduponto.domain.repository.SessaoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HistoricoAlunoViewModel(
    sessao: SessaoRepository,
    presencas: PresencaRepository
) : ViewModel() {

    /** null = carregando. */
    val registros: StateFlow<List<Presenca>?> = sessao.usuario
        .flatMapLatest { usuario ->
            if (usuario == null) {
                flowOf(emptyList<Presenca>())
            } else {
                presencas.observarDoAluno(usuario.nome)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HistoricoAlunoViewModel(container.sessao, container.presencas) }
        }
    }
}
