package br.com.eduponto.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.eduponto.R
import br.com.eduponto.ui.components.EstadoVazio
import br.com.eduponto.ui.components.rotulo
import br.com.eduponto.ui.sessao.SessaoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(viewModel: SessaoViewModel) {
    val usuario by viewModel.usuario.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = usuario?.nome.orEmpty(),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = usuario?.perfil?.rotulo.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::sair) {
                        Text(stringResource(R.string.common_sign_out))
                    }
                }
            )
        }
    ) { paddingInterno ->
        EstadoVazio(
            titulo = stringResource(R.string.admin_in_progress_title),
            descricao = stringResource(R.string.admin_in_progress_body),
            modifier = Modifier.padding(paddingInterno)
        )
    }
}
