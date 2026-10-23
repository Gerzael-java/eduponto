package br.com.eduponto.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.eduponto.AppContainer
import br.com.eduponto.alunos.AlunoHomeScreen
import br.com.eduponto.domain.model.Perfil
import br.com.eduponto.professores.ProfessorHomeScreen
import br.com.eduponto.ui.login.LoginScreen
import br.com.eduponto.ui.login.LoginViewModel
import br.com.eduponto.ui.sessao.SessaoViewModel

@Composable
fun EduPontoNavHost(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val usuario by container.sessao.usuario.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Rotas.LOGIN,
        modifier = modifier
    ) {
        composable(Rotas.LOGIN) {
            LoginScreen(viewModel = viewModel(factory = LoginViewModel.factory(container)))
        }
        composable(Rotas.PROFESSOR) {
            ProfessorHomeScreen(viewModel = viewModel(factory = SessaoViewModel.factory(container)))
        }
        composable(Rotas.ALUNO) {
            AlunoHomeScreen(viewModel = viewModel(factory = SessaoViewModel.factory(container)))
        }
    }

    // A tela mostrada depende de quem está com a sessão aberta.
    LaunchedEffect(usuario) {
        val destino = when (usuario?.perfil) {
            Perfil.PROFESSOR -> Rotas.PROFESSOR
            Perfil.ALUNO -> Rotas.ALUNO
            null -> Rotas.LOGIN
        }
        val atual = navController.currentDestination?.route
        if (atual != null && atual != destino) {
            navController.navigate(destino) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
