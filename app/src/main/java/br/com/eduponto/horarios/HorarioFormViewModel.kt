package br.com.eduponto.horarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.repository.SessaoRepository
import br.com.eduponto.domain.usecase.CriarHorarioUseCase
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HorarioFormState(
    val disciplina: String = "",
    val turma: String = "",
    val dia: DayOfWeek = DayOfWeek.MONDAY,
    val inicio: LocalTime = LocalTime.of(8, 0),
    val fim: LocalTime = LocalTime.of(9, 0),
    val erro: String? = null,
    val salvo: Boolean = false
)

class HorarioFormViewModel(
    private val sessao: SessaoRepository,
    private val criarHorario: CriarHorarioUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(HorarioFormState())
    val estado: StateFlow<HorarioFormState> = _estado.asStateFlow()

    fun aoMudarDisciplina(valor: String) {
        _estado.update { it.copy(disciplina = valor, erro = null) }
    }

    fun aoMudarTurma(valor: String) {
        _estado.update { it.copy(turma = valor, erro = null) }
    }

    fun aoMudarDia(dia: DayOfWeek) {
        _estado.update { it.copy(dia = dia, erro = null) }
    }

    fun aoMudarInicio(inicio: LocalTime) {
        _estado.update { it.copy(inicio = inicio, erro = null) }
    }

    fun aoMudarFim(fim: LocalTime) {
        _estado.update { it.copy(fim = fim, erro = null) }
    }

    fun salvar() {
        val professor = sessao.usuario.value?.nome
        if (professor == null) {
            _estado.update { it.copy(erro = "Sessão encerrada. Entre novamente.") }
            return
        }
        val atual = _estado.value
        viewModelScope.launch {
            val resultado = criarHorario(
                professor = professor,
                disciplina = atual.disciplina,
                turma = atual.turma,
                diaSemana = atual.dia,
                inicio = atual.inicio,
                fim = atual.fim
            )
            when (resultado) {
                is CriarHorarioUseCase.Resultado.Sucesso ->
                    _estado.update { it.copy(salvo = true) }
                is CriarHorarioUseCase.Resultado.Erro ->
                    _estado.update { it.copy(erro = resultado.mensagem) }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HorarioFormViewModel(container.sessao, container.criarHorario) }
        }
    }
}
