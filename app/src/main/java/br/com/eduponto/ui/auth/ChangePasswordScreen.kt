package br.com.eduponto.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.eduponto.R
import br.com.eduponto.ui.theme.Espacamento

@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel,
    onSair: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val foco = LocalFocusManager.current
    val transformacao = if (estado.mostrarSenha) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    Scaffold { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Espacamento.l, vertical = Espacamento.xl),
            verticalArrangement = Arrangement.spacedBy(Espacamento.m)
        ) {
            Spacer(modifier = Modifier.height(Espacamento.l))

            Text(
                text = stringResource(R.string.change_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.change_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = estado.novaSenha,
                onValueChange = viewModel::aoMudarNovaSenha,
                label = { Text(stringResource(R.string.change_new)) },
                singleLine = true,
                enabled = !estado.carregando,
                visualTransformation = transformacao,
                trailingIcon = {
                    TextButton(onClick = viewModel::alternarSenhaVisivel) {
                        Text(
                            stringResource(
                                if (estado.mostrarSenha) {
                                    R.string.login_hide_password
                                } else {
                                    R.string.login_show_password
                                }
                            )
                        )
                    }
                },
                supportingText = { Text(stringResource(R.string.change_rules)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.confirmacao,
                onValueChange = viewModel::aoMudarConfirmacao,
                label = { Text(stringResource(R.string.change_confirm)) },
                singleLine = true,
                enabled = !estado.carregando,
                visualTransformation = transformacao,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        foco.clearFocus()
                        viewModel.aoSalvar()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            estado.erro?.let { erro ->
                Text(
                    text = stringResource(erro.textoRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    foco.clearFocus()
                    viewModel.aoSalvar()
                },
                enabled = !estado.carregando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.change_submit))
                }
            }

            OutlinedButton(
                onClick = onSair,
                enabled = !estado.carregando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_sign_out))
            }
        }
    }
}
