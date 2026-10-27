package br.com.eduponto.domain.model

/**
 * Quando a pessoa tem mais de um papel, o app abre no de maior responsabilidade.
 * (Um seletor de papel entra em uma etapa futura.)
 */
object EscolhaDePerfil {
    private val prioridade = listOf(Perfil.ADMINISTRADOR, Perfil.PROFESSOR, Perfil.ALUNO)

    fun escolher(papeis: Set<Perfil>): Perfil? = prioridade.firstOrNull { it in papeis }
}
