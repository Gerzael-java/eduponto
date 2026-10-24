package br.com.eduponto.horarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.model.Horario
import br.com.eduponto.domain.repository.HorarioRepository
import br.com.eduponto.domain.repository.SessaoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HorariosViewModel(
    sessao: SessaoRepository,
    private val repositorio: HorarioRepository
) : ViewModel() {

    /** null = ainda carregando; lista vazia = nenhum horário cadastrado. */
    val horarios: StateFlow<List<Horario>?> = sessao.usuario
        .flatMapLatest { usuario ->
            if (usuario == null) {
                flowOf(emptyList<Horario>())
            } else {
                repositorio.observarDoProfessor(usuario.nome)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun excluir(id: Long) {
        viewModelScope.launch { repositorio.excluir(id) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HorariosViewModel(container.sessao, container.horarios) }
        }
    }
}
