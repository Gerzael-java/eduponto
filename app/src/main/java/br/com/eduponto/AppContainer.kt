package br.com.eduponto

import android.content.Context
import br.com.eduponto.data.local.AppDatabase
import br.com.eduponto.data.repository.HorarioRepositoryRoom
import br.com.eduponto.data.repository.SessaoRepositoryEmMemoria
import br.com.eduponto.domain.repository.HorarioRepository
import br.com.eduponto.domain.repository.SessaoRepository
import br.com.eduponto.domain.usecase.CriarHorarioUseCase
import br.com.eduponto.domain.usecase.EntrarUseCase

/** Contêiner de dependências manual (sem Hilt). Cresce a cada etapa. */
class AppContainer(context: Context) {

    private val contextoApp: Context = context.applicationContext

    private val bancoDeDados: AppDatabase by lazy { AppDatabase.criar(contextoApp) }

    val sessao: SessaoRepository = SessaoRepositoryEmMemoria()
    val entrar: EntrarUseCase = EntrarUseCase(sessao)

    val horarios: HorarioRepository by lazy { HorarioRepositoryRoom(bancoDeDados.horarioDao()) }
    val criarHorario: CriarHorarioUseCase by lazy { CriarHorarioUseCase(horarios) }
}
