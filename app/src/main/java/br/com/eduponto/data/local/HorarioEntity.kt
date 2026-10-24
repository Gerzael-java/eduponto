package br.com.eduponto.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "horarios")
data class HorarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val professor: String,
    val disciplina: String,
    val turma: String,
    /** 1 = segunda ... 7 = domingo (mesmo valor de java.time.DayOfWeek). */
    val diaSemana: Int,
    val inicioMinutos: Int,
    val fimMinutos: Int
)
