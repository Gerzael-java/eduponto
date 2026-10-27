package br.com.eduponto.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.eduponto.AppContainer
import br.com.eduponto.domain.model.AuthState
import br.com.eduponto.domain.model.Perfil
import br.com.eduponto.ui.auth.AuthViewModel
import br.com.eduponto.ui.auth.ChangePasswordScreen
import br.com.eduponto.ui.auth.ChangePasswordViewModel
import br.com.eduponto.ui.auth.TelaCarregando
import br.com.eduponto.ui.auth.TelaContaBloqueada
import br.com.eduponto.ui.auth.TelaNaoConfigurado
import br.com.eduponto.ui.login.LoginScreen
import br.com.eduponto.ui.login.LoginViewModel

/** Decide o que mostrar conforme o estado da autenticação (vindo do servidor). */
@Composable
fun AuthGate(container: AppContainer) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(container))
    val estado by authViewModel.estado.collectAsStateWithLifecycle()

    when (val atual = estado) {
        is AuthState.Loading -> TelaCarregando()
        is AuthState.NotConfigured -> TelaNaoConfigurado()
        is AuthState.SignedOut -> LoginScreen(
            viewModel = viewModel(
                key = "login",
                factory = LoginViewModel.factory(container)
            )
        )
        is AuthState.MustChangePassword -> ChangePasswordScreen(
            viewModel = viewModel(
                key = "trocar_senha",
                factory = ChangePasswordViewModel.factory(container)
            ),
            onSair = authViewModel::sair
        )
        is AuthState.Blocked -> TelaContaBloqueada(
            motivo = atual.reason,
            onTentarNovamente = authViewModel::recarregar,
            onSair = authViewModel::sair
        )
        is AuthState.SignedIn -> key(atual.profile.id, atual.activeRole) {
            EduPontoNavHost(
                container = container,
                rotaInicial = rotaInicialDe(atual.activeRole)
            )
        }
    }
}

private fun rotaInicialDe(perfil: Perfil): String = when (perfil) {
    Perfil.ADMINISTRADOR -> Rotas.ADMIN
    Perfil.PROFESSOR -> Rotas.PROFESSOR
    Perfil.ALUNO -> Rotas.ALUNO
}
