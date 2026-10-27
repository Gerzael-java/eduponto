package br.com.eduponto

import android.content.Context
import br.com.eduponto.data.local.AppDatabase
import br.com.eduponto.data.remote.SupabaseProvider
import br.com.eduponto.data.repository.AuthRepositorySupabase
import br.com.eduponto.data.repository.ChamadaRepositoryRoom
import br.com.eduponto.data.repository.HorarioRepositoryRoom
import br.com.eduponto.data.repository.PresencaRepositoryRoom
import br.com.eduponto.data.repository.SessaoRepositoryAuth
import br.com.eduponto.domain.repository.AuthRepository
import br.com.eduponto.domain.repository.ChamadaRepository
import br.com.eduponto.domain.repository.HorarioRepository
import br.com.eduponto.domain.repository.PresencaRepository
import br.com.eduponto.domain.repository.SessaoRepository
import br.com.eduponto.domain.usecase.ChangePasswordUseCase
import br.com.eduponto.domain.usecase.CriarHorarioUseCase
import br.com.eduponto.domain.usecase.IniciarChamadaUseCase
import br.com.eduponto.domain.usecase.RegistrarComprovanteUseCase
import br.com.eduponto.domain.usecase.RegistrarPresencaAlunoUseCase
import br.com.eduponto.domain.usecase.SignInUseCase
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Contêiner de dependências manual (sem Hilt). Cresce a cada etapa. */
class AppContainer(context: Context) {

    private val contextoApp: Context = context.applicationContext
    private val escopoApp = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val bancoDeDados: AppDatabase by lazy { AppDatabase.criar(contextoApp) }
    private val supabase: SupabaseClient? by lazy { SupabaseProvider.criar() }

    // ----- autenticação (servidor) -----
    val auth: AuthRepository by lazy { AuthRepositorySupabase(supabase, escopoApp) }
    val signIn: SignInUseCase by lazy { SignInUseCase(auth) }
    val changePassword: ChangePasswordUseCase by lazy { ChangePasswordUseCase(auth) }

    // ----- funcionalidades locais (serão migradas para o servidor) -----
    val sessao: SessaoRepository by lazy { SessaoRepositoryAuth(auth, escopoApp) }

    val horarios: HorarioRepository by lazy { HorarioRepositoryRoom(bancoDeDados.horarioDao()) }
    val chamadas: ChamadaRepository by lazy { ChamadaRepositoryRoom(bancoDeDados.chamadaDao()) }
    val presencas: PresencaRepository by lazy { PresencaRepositoryRoom(bancoDeDados.presencaDao()) }

    val criarHorario: CriarHorarioUseCase by lazy { CriarHorarioUseCase(horarios) }
    val iniciarChamada: IniciarChamadaUseCase by lazy { IniciarChamadaUseCase(chamadas) }
    val registrarComprovante: RegistrarComprovanteUseCase by lazy {
        RegistrarComprovanteUseCase(chamadas, presencas)
    }
    val registrarPresencaAluno: RegistrarPresencaAlunoUseCase by lazy {
        RegistrarPresencaAlunoUseCase(presencas)
    }
}
