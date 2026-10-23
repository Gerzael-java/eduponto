package br.com.eduponto.domain.repository

import br.com.eduponto.domain.model.Usuario
import kotlinx.coroutines.flow.StateFlow

/** Guarda quem está "logado" no aparelho. Login demonstrativo, sem segurança real. */
interface SessaoRepository {
    val usuario: StateFlow<Usuario?>
    fun entrar(usuario: Usuario)
    fun sair()
}
