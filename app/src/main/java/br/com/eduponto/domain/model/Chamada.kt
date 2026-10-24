package br.com.eduponto.domain.model

data class Chamada(
    val id: Long = 0,
    val horarioId: Long,
    val professor: String,
    val disciplina: String,
    val turma: String,
    val codigo: String,
    val inicioEm: Long,
    val expiraEm: Long,
    val encerradaEm: Long? = null
) {
    val ativa: Boolean get() = encerradaEm == null
}
