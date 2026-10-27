package br.com.eduponto.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EscolhaDePerfilTest {

    @Test
    fun `administrador tem prioridade`() {
        val papeis = setOf(Perfil.ALUNO, Perfil.PROFESSOR, Perfil.ADMINISTRADOR)
        assertEquals(Perfil.ADMINISTRADOR, EscolhaDePerfil.escolher(papeis))
    }

    @Test
    fun `professor vem antes de aluno`() {
        assertEquals(
            Perfil.PROFESSOR,
            EscolhaDePerfil.escolher(setOf(Perfil.ALUNO, Perfil.PROFESSOR))
        )
    }

    @Test
    fun `um unico papel e usado`() {
        assertEquals(Perfil.ALUNO, EscolhaDePerfil.escolher(setOf(Perfil.ALUNO)))
    }

    @Test
    fun `sem papeis nao ha perfil`() {
        assertNull(EscolhaDePerfil.escolher(emptySet()))
    }
}
