package br.com.eduponto.horarios

import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.eduponto.ui.components.abreviacao
import br.com.eduponto.ui.components.formatado
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioFormScreen(
    viewModel: HorarioFormViewModel,
    onVoltar: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.salvo) {
        if (estado.salvo) onVoltar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo horário") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = estado.disciplina,
                onValueChange = viewModel::aoMudarDisciplina,
                label = { Text("Disciplina") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.turma,
                onValueChange = viewModel::aoMudarTurma,
                label = { Text("Turma") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text(text = "Dia da semana", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DayOfWeek.entries.forEach { dia ->
                    FilterChip(
                        selected = estado.dia == dia,
                        onClick = { viewModel.aoMudarDia(dia) },
                        label = { Text(dia.abreviacao) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BotaoHorario(
                    rotulo = "Início",
                    valor = estado.inicio,
                    aoEscolher = viewModel::aoMudarInicio,
                    modifier = Modifier.weight(1f)
                )
                BotaoHorario(
                    rotulo = "Término",
                    valor = estado.fim,
                    aoEscolher = viewModel::aoMudarFim,
                    modifier = Modifier.weight(1f)
                )
            }

            estado.erro?.let { mensagem ->
                Text(
                    text = mensagem,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = viewModel::salvar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar horário")
            }
        }
    }
}

@Composable
private fun BotaoHorario(
    rotulo: String,
    valor: LocalTime,
    aoEscolher: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    OutlinedButton(
        onClick = {
            TimePickerDialog(
                contexto,
                { _, hora, minuto -> aoEscolher(LocalTime.of(hora, minuto)) },
                valor.hour,
                valor.minute,
                true
            ).show()
        },
        modifier = modifier
    ) {
        Text("$rotulo: ${valor.formatado()}")
    }
}
