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
import br.com.eduponto.horarios.HorarioFormScreen
import br.com.eduponto.horarios.HorarioFormViewModel
import br.com.eduponto.horarios.HorariosViewModel
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
            ProfessorHomeScreen(
                viewModel = viewModel(factory = SessaoViewModel.factory(container)),
                horariosViewModel = viewModel(factory = HorariosViewModel.factory(container)),
                onNovoHorario = { navController.navigate(Rotas.NOVO_HORARIO) }
            )
        }
        composable(Rotas.NOVO_HORARIO) {
            HorarioFormScreen(
                viewModel = viewModel(factory = HorarioFormViewModel.factory(container)),
                onVoltar = { navController.popBackStack() }
            )
        }
        composable(Rotas.ALUNO) {
            AlunoHomeScreen(viewModel = viewModel(factory = SessaoViewModel.factory(container)))
        }
    }

    // Só sai do login quando há sessão e só volta ao login quando a sessão acaba.
    // (Não força a rota de início em outras situações, para não perder a tela
    // aberta ao girar o aparelho.)
    LaunchedEffect(usuario) {
        val atual = navController.currentDestination?.route ?: return@LaunchedEffect
        val noLogin = atual == Rotas.LOGIN
        val usuarioAtual = usuario

        if (usuarioAtual == null && !noLogin) {
            navController.navigate(Rotas.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (usuarioAtual != null && noLogin) {
            val destino = when (usuarioAtual.perfil) {
                Perfil.PROFESSOR -> Rotas.PROFESSOR
                Perfil.ALUNO -> Rotas.ALUNO
            }
            navController.navigate(destino) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
