package br.com.eduponto.domain.repository

import br.com.eduponto.domain.model.Usuario
import kotlinx.coroutines.flow.StateFlow

/**
 * Visão simplificada de "quem está logado", usada pelas funcionalidades locais
 * que ainda serão migradas para o servidor.
 */
interface SessaoRepository {
    val usuario: StateFlow<Usuario?>
    fun sair()
}
