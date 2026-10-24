package br.com.eduponto.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Horario(
    val id: Long = 0,
    val professor: String,
    val disciplina: String,
    val turma: String,
    val diaSemana: DayOfWeek,
    val inicio: LocalTime,
    val fim: LocalTime
)
