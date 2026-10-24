package br.com.eduponto.ui.components

import java.time.DayOfWeek
import java.time.LocalTime

val DayOfWeek.nomeCompleto: String
    get() = when (this) {
        DayOfWeek.MONDAY -> "Segunda-feira"
        DayOfWeek.TUESDAY -> "Terça-feira"
        DayOfWeek.WEDNESDAY -> "Quarta-feira"
        DayOfWeek.THURSDAY -> "Quinta-feira"
        DayOfWeek.FRIDAY -> "Sexta-feira"
        DayOfWeek.SATURDAY -> "Sábado"
        DayOfWeek.SUNDAY -> "Domingo"
    }

val DayOfWeek.abreviacao: String
    get() = when (this) {
        DayOfWeek.MONDAY -> "Seg"
        DayOfWeek.TUESDAY -> "Ter"
        DayOfWeek.WEDNESDAY -> "Qua"
        DayOfWeek.THURSDAY -> "Qui"
        DayOfWeek.FRIDAY -> "Sex"
        DayOfWeek.SATURDAY -> "Sáb"
        DayOfWeek.SUNDAY -> "Dom"
    }

fun LocalTime.formatado(): String = "%02d:%02d".format(hour, minute)
