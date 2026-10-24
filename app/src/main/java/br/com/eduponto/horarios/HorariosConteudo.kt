package br.com.eduponto.horarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.eduponto.domain.model.Horario
import br.com.eduponto.ui.components.EstadoVazio
import br.com.eduponto.ui.components.formatado
import br.com.eduponto.ui.components.nomeCompleto

@Composable
fun HorariosConteudo(
    viewModel: HorariosViewModel,
    modifier: Modifier = Modifier
) {
    val horarios by viewModel.horarios.collectAsStateWithLifecycle()
    var horarioParaExcluir by remember { mutableStateOf<Horario?>(null) }
    val lista = horarios

    when {
        lista == null -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        lista.isEmpty() -> EstadoVazio(
            titulo = "Nenhum horário cadastrado",
            descricao = "Toque em \"Novo horário\" para cadastrar as suas aulas.",
            modifier = modifier
        )

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = lista, key = { it.id }) { horario ->
                CartaoHorario(
                    horario = horario,
                    onExcluir = { horarioParaExcluir = horario }
                )
            }
        }
    }

    horarioParaExcluir?.let { horario ->
        AlertDialog(
            onDismissRequest = { horarioParaExcluir = null },
            title = { Text("Excluir horário?") },
            text = {
                Text(
                    "${horario.disciplina} (turma ${horario.turma}), " +
                        "${horario.diaSemana.nomeCompleto}. Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.excluir(horario.id)
                        horarioParaExcluir = null
                    }
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { horarioParaExcluir = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun CartaoHorario(
    horario: Horario,
    onExcluir: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = horario.disciplina,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Turma ${horario.turma}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${horario.diaSemana.nomeCompleto} · " +
                    "${horario.inicio.formatado()} – ${horario.fim.formatado()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onExcluir) {
                    Text(text = "Excluir", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
