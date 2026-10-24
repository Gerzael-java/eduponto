package br.com.eduponto.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "presencas",
    indices = [Index(value = ["codigoChamada", "aluno", "perfil"], unique = true)]
)
data class PresencaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigoChamada: String,
    val professor: String,
    val disciplina: String,
    val turma: String,
    val aluno: String,
    val registradoEm: Long,
    /** Nome do enum Perfil: ALUNO ou PROFESSOR. */
    val perfil: String
)
