package br.com.eduponto.domain.model

/**
 * Registro de presença. No celular do aluno, [perfil] é ALUNO (o próprio aluno registrou).
 * No celular do professor, [perfil] é PROFESSOR (o professor confirmou um comprovante).
 */
data class Presenca(
    val id: Long = 0,
    val codigoChamada: String,
    val professor: String,
    val disciplina: String,
    val turma: String,
    val aluno: String,
    val registradoEm: Long,
    val perfil: Perfil
)
