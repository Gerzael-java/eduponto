package br.com.eduponto.alunos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.eduponto.qrcode.CampoCodigoManual
import br.com.eduponto.qrcode.ExigirPermissaoCamera
import br.com.eduponto.qrcode.LeitorQrCode
import br.com.eduponto.qrcode.QrCodeImagem
import br.com.eduponto.ui.components.comoDataHora
import br.com.eduponto.ui.components.copiarTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeitorAlunoScreen(
    viewModel: LeitorAlunoViewModel,
    onConcluir: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar presença") },
                navigationIcon = {
                    IconButton(onClick = onConcluir) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .padding(paddingInterno)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val atual = estado) {
                is LeitorAlunoEstado.Lendo -> {
                    Text(
                        text = "Aponte a câmera para o QR Code exibido pelo professor.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    ExigirPermissaoCamera {
                        LeitorQrCode(
                            onTextoLido = viewModel::aoLerTexto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.large)
                        )
                    }
                    atual.erro?.let { mensagem ->
                        Text(
                            text = mensagem,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    CampoCodigoManual(
                        rotulo = "Ou cole os dados do QR Code",
                        onConfirmar = viewModel::aoLerTexto
                    )
                }

                is LeitorAlunoEstado.Registrada -> Comprovante(
                    estado = atual,
                    onConcluir = onConcluir
                )
            }
        }
    }
}

@Composable
private fun Comprovante(
    estado: LeitorAlunoEstado.Registrada,
    onConcluir: () -> Unit
) {
    val contexto = LocalContext.current
    var copiado by remember { mutableStateOf(false) }
    val presenca = estado.presenca

    Text(
        text = if (estado.jaRegistrada) {
            "Presença já registrada"
        } else {
            "Presença registrada!"
        },
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Text(
        text = "${presenca.disciplina} · turma ${presenca.turma}\n" +
            "Professor(a): ${presenca.professor}\n" +
            presenca.registradoEm.comoDataHora(),
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = "Para o professor confirmar, mostre este QR Code a ele.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(color = Color.White, shape = MaterialTheme.shapes.medium) {
            QrCodeImagem(texto = estado.comprovante, modifier = Modifier.padding(12.dp))
        }
    }
    OutlinedButton(
        onClick = {
            copiarTexto(contexto, "EduPonto comprovante", estado.comprovante)
            copiado = true
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (copiado) "Comprovante copiado" else "Copiar comprovante")
    }
    Button(onClick = onConcluir, modifier = Modifier.fillMaxWidth()) {
        Text("Concluir")
    }
}
