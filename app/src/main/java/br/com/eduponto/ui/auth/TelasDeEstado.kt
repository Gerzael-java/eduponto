package br.com.eduponto.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import br.com.eduponto.R
import br.com.eduponto.domain.model.BlockReason
import br.com.eduponto.ui.theme.Espacamento

@Composable
fun TelaCarregando() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun TelaNaoConfigurado() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Espacamento.l),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.not_configured_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.not_configured_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Espacamento.s)
        )
    }
}

@Composable
fun TelaContaBloqueada(
    motivo: BlockReason,
    onTentarNovamente: () -> Unit,
    onSair: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Espacamento.l),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.blocked_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(motivo.textoRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = Espacamento.m)
        )
        Column(verticalArrangement = Arrangement.spacedBy(Espacamento.s)) {
            if (motivo == BlockReason.LOAD_FAILED) {
                Button(onClick = onTentarNovamente) {
                    Text(stringResource(R.string.common_retry))
                }
            }
            OutlinedButton(onClick = onSair) {
                Text(stringResource(R.string.common_sign_out))
            }
        }
    }
}
