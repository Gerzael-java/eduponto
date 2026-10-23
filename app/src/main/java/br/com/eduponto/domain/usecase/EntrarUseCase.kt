package br.com.eduponto.domain.usecase

import br.com.eduponto.domain.model.Perfil
import br.com.eduponto.domain.model.Usuario
import br.com.eduponto.domain.repository.SessaoRepository

/** Valida o nome informado e abre a sessão local. Não há senha nem verificação de identidade. */
class EntrarUseCase(private val sessao: SessaoRepository) {

    sealed interface Resultado {
        data object Sucesso : Resultado
        data class Erro(val mensagem: String) : Resultado
    }

    operator fun invoke(nome: String, perfil: Perfil): Resultado {
        val nomeLimpo = nome.trim()
        if (nomeLimpo.length < 2) {
            return Resultado.Erro("Digite seu nome com pelo menos 2 letras.")
        }
        sessao.entrar(Usuario(nome = nomeLimpo, perfil = perfil))
        return Resultado.Sucesso
    }
}
