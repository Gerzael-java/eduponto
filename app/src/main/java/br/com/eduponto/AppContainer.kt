package br.com.eduponto

import br.com.eduponto.data.repository.SessaoRepositoryEmMemoria
import br.com.eduponto.domain.repository.SessaoRepository
import br.com.eduponto.domain.usecase.EntrarUseCase

/** Contêiner de dependências manual (sem Hilt). Cresce nas próximas etapas. */
class AppContainer {
    val sessao: SessaoRepository = SessaoRepositoryEmMemoria()
    val entrar: EntrarUseCase = EntrarUseCase(sessao)
}
