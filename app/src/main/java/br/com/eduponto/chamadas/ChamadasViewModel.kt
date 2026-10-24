package br.com.eduponto.chamadas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.model.Chamada
import br.com.eduponto.domain.model.Horario
import br.com.eduponto.domain.repository.ChamadaRepository
import br.com.eduponto.domain.repository.HorarioRepository
import br.com.eduponto.domain.repository.SessaoRepository
import br.com.eduponto.domain.usecase.IniciarChamadaUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChamadasUiState(
    /** null = carregando. */
    val horarios: List<Horario>? = null,
    val ativa: Chamada? = null,
    val erro: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ChamadasViewModel(
    sessao: SessaoRepository,
    horariosRepositorio: HorarioRepository,
    chamadasRepositorio: ChamadaRepository,
    private val iniciarChamada: IniciarChamadaUseCase
) : ViewModel() {

    private val horarios: Flow<List<Horario>> = sessao.usuario.flatMapLatest { usuario ->
        if (usuario == null) {
            flowOf(emptyList<Horario>())
        } else {
            horariosRepositorio.observarDoProfessor(usuario.nome)
        }
    }

    private val ativa: Flow<Chamada?> = sessao.usuario.flatMapLatest { usuario ->
        if (usuario == null) {
            flowOf<Chamada?>(null)
        } else {
            chamadasRepositorio.observarAtiva(usuario.nome)
        }
    }

    private val erro = MutableStateFlow<String?>(null)

    val estado: StateFlow<ChamadasUiState> =
        combine(horarios, ativa, erro) { listaHorarios, chamadaAtiva, mensagemErro ->
            ChamadasUiState(
                horarios = listaHorarios,
                ativa = chamadaAtiva,
                erro = mensagemErro
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChamadasUiState()
        )

    private val _eventos = Channel<Long>(Channel.BUFFERED)

    /** Emite o id da chamada recém-criada, para a tela abri-la. */
    val eventos: Flow<Long> = _eventos.receiveAsFlow()

    fun iniciar(horario: Horario) {
        viewModelScope.launch {
            when (val resultado = iniciarChamada(horario)) {
                is IniciarChamadaUseCase.Resultado.Sucesso -> {
                    erro.value = null
                    _eventos.send(resultado.chamadaId)
                }
                is IniciarChamadaUseCase.Resultado.Erro -> erro.value = resultado.mensagem
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ChamadasViewModel(
                    container.sessao,
                    container.horarios,
                    container.chamadas,
                    container.iniciarChamada
                )
            }
        }
    }
}
