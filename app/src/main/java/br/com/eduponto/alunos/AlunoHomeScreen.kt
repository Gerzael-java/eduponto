package br.com.eduponto.alunos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.eduponto.ui.components.EstadoVazio
import br.com.eduponto.ui.components.rotulo
import br.com.eduponto.ui.sessao.SessaoViewModel

private data class AbaAluno(val titulo: String, val icone: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlunoHomeScreen(viewModel: SessaoViewModel) {
    val usuario by viewModel.usuario.collectAsStateWithLifecycle()
    var abaSelecionada by rememberSaveable { mutableIntStateOf(0) }

    val abas = listOf(
        AbaAluno("Início", Icons.Filled.Home),
        AbaAluno("Histórico", Icons.AutoMirrored.Filled.List)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Olá, ${usuario?.nome.orEmpty()}",
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
                    TextButton(onClick = viewModel::sair) { Text("Sair") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                abas.forEachIndexed { indice, aba ->
                    NavigationBarItem(
                        selected = indice == abaSelecionada,
                        onClick = { abaSelecionada = indice },
                        icon = { Icon(imageVector = aba.icone, contentDescription = null) },
                        label = { Text(aba.titulo) }
                    )
                }
            }
        }
    ) { paddingInterno ->
        Box(
            modifier = Modifier
                .padding(paddingInterno)
                .fillMaxSize()
        ) {
            when (abaSelecionada) {
                0 -> EstadoVazio(
                    titulo = "Nenhuma chamada aberta",
                    descricao = "Quando o professor iniciar a chamada, você lerá o QR Code por aqui."
                )
                else -> EstadoVazio(
                    titulo = "Nenhuma presença registrada",
                    descricao = "Suas presenças confirmadas aparecerão aqui."
                )
            }
        }
    }
}
