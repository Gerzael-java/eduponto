package br.com.eduponto.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chamadas",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class ChamadaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val horarioId: Long,
    val professor: String,
    val disciplina: String,
    val turma: String,
    val codigo: String,
    val inicioEm: Long,
    val expiraEm: Long,
    val encerradaEm: Long? = null
)
