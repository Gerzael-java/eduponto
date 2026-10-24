package br.com.eduponto.presencas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.eduponto.domain.model.Presenca
import br.com.eduponto.qrcode.QrCodeImagem
import br.com.eduponto.qrcode.QrPayloadCodec
import br.com.eduponto.qrcode.paraComprovante
import br.com.eduponto.ui.components.EstadoVazio
import br.com.eduponto.ui.components.comoDataHora
import br.com.eduponto.ui.components.copiarTexto

@Composable
fun HistoricoAlunoConteudo(
    viewModel: HistoricoAlunoViewModel,
    modifier: Modifier = Modifier
) {
    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val lista = registros

    when {
        lista == null -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        lista.isEmpty() -> EstadoVazio(
            titulo = "Nenhuma presença registrada",
            descricao = "Suas presenças confirmadas aparecerão aqui.",
            modifier = modifier
        )

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = lista, key = { it.id }) { presenca ->
                CartaoPresenca(presenca)
            }
        }
    }
}

@Composable
private fun CartaoPresenca(presenca: Presenca) {
    val contexto = LocalContext.current
    var mostrarComprovante by remember { mutableStateOf(false) }
    val comprovante = remember(presenca) { QrPayloadCodec.codificar(presenca.paraComprovante()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = presenca.disciplina,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Turma ${presenca.turma} · Professor(a) ${presenca.professor}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = presenca.registradoEm.comoDataHora(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = { mostrarComprovante = !mostrarComprovante }) {
                Text(if (mostrarComprovante) "Ocultar comprovante" else "Ver comprovante")
            }
            if (mostrarComprovante) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(color = Color.White, shape = MaterialTheme.shapes.medium) {
                        QrCodeImagem(texto = comprovante, modifier = Modifier.padding(12.dp))
                    }
                    OutlinedButton(
                        onClick = { copiarTexto(contexto, "EduPonto comprovante", comprovante) }
                    ) {
                        Text("Copiar comprovante")
                    }
                }
            }
        }
    }
}
