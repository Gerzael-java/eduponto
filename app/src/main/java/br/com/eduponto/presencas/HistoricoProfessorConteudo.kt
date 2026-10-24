package br.com.eduponto.presencas

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
import br.com.eduponto.ui.components.EstadoVazio
import br.com.eduponto.ui.components.comoDataHora
import br.com.eduponto.ui.components.comoHora

@Composable
fun HistoricoProfessorConteudo(
    viewModel: HistoricoProfessorViewModel,
    modifier: Modifier = Modifier
) {
    val itens by viewModel.itens.collectAsStateWithLifecycle()
    val lista = itens

    when {
        lista == null -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        lista.isEmpty() -> EstadoVazio(
            titulo = "Histórico vazio",
            descricao = "As chamadas que você iniciar, e suas presenças, aparecerão aqui.",
            modifier = modifier
        )

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = lista, key = { it.chamada.id }) { item ->
                CartaoChamadaHistorico(item)
            }
        }
    }
}

@Composable
private fun CartaoChamadaHistorico(item: ChamadaComPresencas) {
    var aberto by remember { mutableStateOf(false) }
    val chamada = item.chamada

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = chamada.disciplina,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Turma ${chamada.turma} · ${chamada.inicioEm.comoDataHora()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (chamada.ativa) "Em andamento" else "Encerrada",
                style = MaterialTheme.typography.labelMedium,
                color = if (chamada.ativa) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.presencas.size} presença(s)",
                    style = MaterialTheme.typography.labelLarge
                )
                if (item.presencas.isNotEmpty()) {
                    TextButton(onClick = { aberto = !aberto }) {
                        Text(if (aberto) "Ocultar" else "Ver alunos")
                    }
                }
            }
            if (aberto) {
                item.presencas.forEach { presenca ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = presenca.aluno, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = presenca.registradoEm.comoHora(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
