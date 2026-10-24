package br.com.eduponto.presencas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.model.Chamada
import br.com.eduponto.domain.model.Presenca
import br.com.eduponto.domain.repository.ChamadaRepository
import br.com.eduponto.domain.repository.PresencaRepository
import br.com.eduponto.domain.repository.SessaoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class ChamadaComPresencas(
    val chamada: Chamada,
    val presencas: List<Presenca>
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoricoProfessorViewModel(
    sessao: SessaoRepository,
    chamadas: ChamadaRepository,
    presencas: PresencaRepository
) : ViewModel() {

    /** null = carregando. */
    val itens: StateFlow<List<ChamadaComPresencas>?> = sessao.usuario
        .flatMapLatest { usuario ->
            if (usuario == null) {
                flowOf(emptyList<ChamadaComPresencas>())
            } else {
                combine(
                    chamadas.observarDoProfessor(usuario.nome),
                    presencas.observarDoProfessor(usuario.nome)
                ) { listaChamadas, listaPresencas ->
                    val porCodigo = listaPresencas.groupBy { it.codigoChamada }
                    listaChamadas.map { chamada ->
                        ChamadaComPresencas(chamada, porCodigo[chamada.codigo].orEmpty())
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HistoricoProfessorViewModel(
                    container.sessao,
                    container.chamadas,
                    container.presencas
                )
            }
        }
    }
}
