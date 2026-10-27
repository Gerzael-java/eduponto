package br.com.eduponto.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.eduponto.AppContainer
import br.com.eduponto.admin.AdminHomeScreen
import br.com.eduponto.alunos.AlunoHomeScreen
import br.com.eduponto.alunos.LeitorAlunoScreen
import br.com.eduponto.alunos.LeitorAlunoViewModel
import br.com.eduponto.chamadas.ChamadaAtivaScreen
import br.com.eduponto.chamadas.ChamadaAtivaViewModel
import br.com.eduponto.chamadas.ChamadasViewModel
import br.com.eduponto.horarios.HorarioFormScreen
import br.com.eduponto.horarios.HorarioFormViewModel
import br.com.eduponto.horarios.HorariosViewModel
import br.com.eduponto.presencas.HistoricoAlunoViewModel
import br.com.eduponto.presencas.HistoricoProfessorViewModel
import br.com.eduponto.professores.ProfessorHomeScreen
import br.com.eduponto.ui.sessao.SessaoViewModel

/** Navegação de quem já está autenticado. O login é tratado em [AuthGate]. */
@Composable
fun EduPontoNavHost(
    container: AppContainer,
    rotaInicial: String,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = rotaInicial,
        modifier = modifier
    ) {
        composable(Rotas.ADMIN) {
            AdminHomeScreen(viewModel = viewModel(factory = SessaoViewModel.factory(container)))
        }
        composable(Rotas.PROFESSOR) {
            ProfessorHomeScreen(
                viewModel = viewModel(factory = SessaoViewModel.factory(container)),
                horariosViewModel = viewModel(factory = HorariosViewModel.factory(container)),
                chamadasViewModel = viewModel(factory = ChamadasViewModel.factory(container)),
                historicoViewModel = viewModel(
                    factory = HistoricoProfessorViewModel.factory(container)
                ),
                onNovoHorario = { navController.navigate(Rotas.NOVO_HORARIO) },
                onAbrirChamada = { id -> navController.navigate(Rotas.chamada(id)) }
            )
        }
        composable(Rotas.NOVO_HORARIO) {
            HorarioFormScreen(
                viewModel = viewModel(factory = HorarioFormViewModel.factory(container)),
                onVoltar = { navController.popBackStack() }
            )
        }
        composable(
            route = Rotas.CHAMADA,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            ChamadaAtivaScreen(
                viewModel = viewModel(
                    key = "chamada_$id",
                    factory = ChamadaAtivaViewModel.factory(container, id)
                ),
                onVoltar = { navController.popBackStack() }
            )
        }
        composable(Rotas.ALUNO) {
            AlunoHomeScreen(
                viewModel = viewModel(factory = SessaoViewModel.factory(container)),
                historicoViewModel = viewModel(factory = HistoricoAlunoViewModel.factory(container)),
                onLerQrCode = { navController.navigate(Rotas.LEITOR_ALUNO) }
            )
        }
        composable(Rotas.LEITOR_ALUNO) {
            LeitorAlunoScreen(
                viewModel = viewModel(factory = LeitorAlunoViewModel.factory(container)),
                onConcluir = { navController.popBackStack() }
            )
        }
    }
}
