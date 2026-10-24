package br.com.eduponto.chamadas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.eduponto.domain.model.Presenca
import br.com.eduponto.qrcode.CampoCodigoManual
import br.com.eduponto.qrcode.ExigirPermissaoCamera
import br.com.eduponto.qrcode.LeitorQrCode
import br.com.eduponto.qrcode.QrCodeImagem
import br.com.eduponto.qrcode.QrPayloadCodec
import br.com.eduponto.qrcode.paraPayload
import br.com.eduponto.ui.components.EstadoVazio
import br.com.eduponto.ui.components.comoHora
import br.com.eduponto.ui.components.copiarTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamadaAtivaScreen(
    viewModel: ChamadaAtivaViewModel,
    onVoltar: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var lendo by rememberSaveable { mutableStateOf(false) }
    var confirmarEncerramento by remember { mutableStateOf(false) }
    var copiado by remember { mutableStateOf(false) }
    val chamada = estado.chamada

    LaunchedEffect(lendo) {
        if (lendo) viewModel.limparFeedback()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chamada") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingInterno ->
        if (chamada == null) {
            EstadoVazio(
                titulo = "Carregando chamada…",
                descricao = "",
                modifier = Modifier.padding(paddingInterno)
            )
        } else {
            val textoQr = QrPayloadCodec.codificar(chamada.paraPayload())

            LazyColumn(
                modifier = Modifier
                    .padding(paddingInterno)
                    .fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = chamada.disciplina,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Turma ${chamada.turma} · iniciada às ${chamada.inicioEm.comoHora()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (chamada.ativa) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color.White,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                QrCodeImagem(
                                    texto = textoQr,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Text(
                                text = "Código ${chamada.codigo}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Os alunos leem este QR Code. Válido até " +
                                    "${chamada.expiraEm.comoHora()}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                copiarTexto(contexto, "EduPonto chamada", textoQr)
                                copiado = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (copiado) "Dados copiados" else "Copiar dados do QR Code")
                        }
                    }
                    item {
                        Button(
                            onClick = { lendo = !lendo },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (lendo) "Fechar leitor" else "Ler comprovante de aluno")
                        }
                    }
                    if (lendo) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ExigirPermissaoCamera {
                                    LeitorQrCode(
                                        onTextoLido = viewModel::aoLerTexto,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(MaterialTheme.shapes.large)
                                    )
                                }
                                estado.feedback?.let { aviso ->
                                    Text(
                                        text = aviso.texto,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (aviso.sucesso) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                    )
                                }
                                CampoCodigoManual(
                                    rotulo = "Colar comprovante do aluno",
                                    onConfirmar = viewModel::aoLerTexto
                                )
                            }
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = { confirmarEncerramento = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Encerrar chamada", color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Chamada encerrada às ${chamada.encerradaEm?.comoHora().orEmpty()}.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    Text(
                        text = "Presenças confirmadas (${estado.presencas.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (estado.presencas.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma presença confirmada ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(items = estado.presencas, key = { it.id }) { presenca ->
                        LinhaPresenca(presenca)
                    }
                }
            }

            if (confirmarEncerramento) {
                AlertDialog(
                    onDismissRequest = { confirmarEncerramento = false },
                    title = { Text("Encerrar chamada?") },
                    text = {
                        Text("Depois de encerrada, novos comprovantes não serão aceitos.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.encerrar()
                                lendo = false
                                confirmarEncerramento = false
                            }
                        ) { Text("Encerrar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmarEncerramento = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LinhaPresenca(presenca: Presenca) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = presenca.aluno, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = presenca.registradoEm.comoHora(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
