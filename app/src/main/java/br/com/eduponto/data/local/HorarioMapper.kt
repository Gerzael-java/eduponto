package br.com.eduponto.data.local

import br.com.eduponto.domain.model.Horario
import java.time.DayOfWeek
import java.time.LocalTime

fun HorarioEntity.paraDominio(): Horario = Horario(
    id = id,
    professor = professor,
    disciplina = disciplina,
    turma = turma,
    diaSemana = DayOfWeek.of(diaSemana),
    inicio = LocalTime.ofSecondOfDay(inicioMinutos * 60L),
    fim = LocalTime.ofSecondOfDay(fimMinutos * 60L)
)

fun Horario.paraEntidade(): HorarioEntity = HorarioEntity(
    id = id,
    professor = professor,
    disciplina = disciplina,
    turma = turma,
    diaSemana = diaSemana.value,
    inicioMinutos = inicio.toSecondOfDay() / 60,
    fimMinutos = fim.toSecondOfDay() / 60
)
