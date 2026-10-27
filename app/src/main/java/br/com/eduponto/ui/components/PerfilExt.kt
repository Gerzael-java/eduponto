package br.com.eduponto.ui.components

import br.com.eduponto.domain.model.Perfil

val Perfil.rotulo: String
    get() = when (this) {
        Perfil.ADMINISTRADOR -> "Administrador"
        Perfil.PROFESSOR -> "Professor"
        Perfil.ALUNO -> "Aluno"
    }
