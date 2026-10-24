package br.com.eduponto.ui.components

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

private val FORMATO_DATA_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

/** Milissegundos desde 1970 -> "HH:mm" no fuso do aparelho. */
fun Long.comoHora(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalTime().formatado()

/** Milissegundos desde 1970 -> "dd/MM/yyyy HH:mm" no fuso do aparelho. */
fun Long.comoDataHora(): String =
    FORMATO_DATA_HORA.format(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()))
