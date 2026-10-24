package br.com.eduponto.chamadas

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
import br.com.eduponto.domain.usecase.RegistrarComprovanteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Feedback(val texto: String, val sucesso: Boolean)

data class ChamadaAtivaUiState(
    val chamada: Chamada? = null,
    val presencas: List<Presenca> = emptyList(),
    val feedback: Feedback? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ChamadaAtivaViewModel(
    private val chamadaId: Long,
    private val chamadas: ChamadaRepository,
    presencas: PresencaRepository,
    private val registrarComprovante: RegistrarComprovanteUseCase
) : ViewModel() {

    private val feedback = MutableStateFlow<Feedback?>(null)

    private val chamada: Flow<Chamada?> = chamadas.observar(chamadaId)

    private val listaPresencas: Flow<List<Presenca>> = chamada.flatMapLatest { atual ->
        if (atual == null) {
            flowOf(emptyList<Presenca>())
        } else {
            presencas.observarDaChamada(atual.codigo)
        }
    }

    val estado: StateFlow<ChamadaAtivaUiState> =
        combine(chamada, listaPresencas, feedback) { atual, lista, mensagem ->
            ChamadaAtivaUiState(chamada = atual, presencas = lista, feedback = mensagem)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChamadaAtivaUiState()
        )

    fun aoLerTexto(texto: String) {
        viewModelScope.launch {
            feedback.value = when (val resultado = registrarComprovante(chamadaId, texto)) {
                is RegistrarComprovanteUseCase.Resultado.Sucesso ->
                    Feedback(resultado.mensagem, sucesso = true)
                is RegistrarComprovanteUseCase.Resultado.Erro ->
                    Feedback(resultado.mensagem, sucesso = false)
            }
        }
    }

    fun limparFeedback() {
        feedback.value = null
    }

    fun encerrar() {
        viewModelScope.launch {
            chamadas.encerrar(chamadaId, System.currentTimeMillis())
        }
    }

    companion object {
        fun factory(container: AppContainer, chamadaId: Long): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ChamadaAtivaViewModel(
                        chamadaId,
                        container.chamadas,
                        container.presencas,
                        container.registrarComprovante
                    )
                }
            }
    }
}
