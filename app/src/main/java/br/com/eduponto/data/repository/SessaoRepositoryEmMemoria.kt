package br.com.eduponto.data.repository

import br.com.eduponto.domain.model.Usuario
import br.com.eduponto.domain.repository.SessaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Sessão guardada só na memória: ao fechar o app, volta para a tela de login. */
class SessaoRepositoryEmMemoria : SessaoRepository {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    override val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    override fun entrar(usuario: Usuario) {
        _usuario.value = usuario
    }

    override fun sair() {
        _usuario.value = null
    }
}
