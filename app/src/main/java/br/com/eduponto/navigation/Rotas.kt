package br.com.eduponto.navigation

object Rotas {
    const val LOGIN = "login"
    const val PROFESSOR = "professor"
    const val ALUNO = "aluno"
    const val NOVO_HORARIO = "novo_horario"
    const val CHAMADA = "chamada/{id}"
    const val LEITOR_ALUNO = "leitor_aluno"

    fun chamada(id: Long): String = "chamada/$id"
}
