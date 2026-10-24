package br.com.eduponto.chamadas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.eduponto.domain.model.Chamada
import br.com.eduponto.domain.model.Horario
import br.com.eduponto.ui.components.EstadoVazio
import br.com.eduponto.ui.components.comoHora
import br.com.eduponto.ui.components.formatado
import br.com.eduponto.ui.components.nomeCompleto

@Composable
fun ChamadasConteudo(
    viewModel: ChamadasViewModel,
    onAbrirChamada: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { id -> onAbrirChamada(id) }
    }

    val horarios = estado.horarios
    val ativa = estado.ativa

    when {
        horarios == null -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        horarios.isEmpty() && ativa == null -> EstadoVazio(
            titulo = "Nenhum horário para chamada",
            descricao = "Cadastre um horário na aba Horários para poder iniciar uma chamada.",
            modifier = modifier
        )

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ativa != null) {
                item { CartaoChamadaAtiva(ativa) { onAbrirChamada(ativa.id) } }
            }
            estado.erro?.let { mensagem ->
                item {
                    Text(
                        text = mensagem,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (horarios.isNotEmpty()) {
                item {
                    Text(
                        text = "Iniciar chamada de:",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                items(items = horarios, key = { it.id }) { horario ->
                    CartaoIniciarChamada(
                        horario = horario,
                        habilitado = ativa == null,
                        onIniciar = { viewModel.iniciar(horario) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartaoChamadaAtiva(chamada: Chamada, onAbrir: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Chamada em andamento",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = chamada.disciplina,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Turma ${chamada.turma} · iniciada às ${chamada.inicioEm.comoHora()}",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onAbrir) { Text("Abrir chamada") }
        }
    }
}

@Composable
private fun CartaoIniciarChamada(
    horario: Horario,
    habilitado: Boolean,
    onIniciar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = horario.disciplina,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Turma ${horario.turma}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${horario.diaSemana.nomeCompleto} · " +
                        "${horario.inicio.formatado()} – ${horario.fim.formatado()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            OutlinedButton(onClick = onIniciar, enabled = habilitado) {
                Text("Iniciar")
            }
        }
    }
}
