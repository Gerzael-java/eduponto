package br.com.eduponto.data.local

import br.com.eduponto.domain.model.Chamada
import br.com.eduponto.domain.model.Perfil
import br.com.eduponto.domain.model.Presenca

fun ChamadaEntity.paraDominio(): Chamada = Chamada(
    id = id,
    horarioId = horarioId,
    professor = professor,
    disciplina = disciplina,
    turma = turma,
    codigo = codigo,
    inicioEm = inicioEm,
    expiraEm = expiraEm,
    encerradaEm = encerradaEm
)

fun Chamada.paraEntidade(): ChamadaEntity = ChamadaEntity(
    id = id,
    horarioId = horarioId,
    professor = professor,
    disciplina = disciplina,
    turma = turma,
    codigo = codigo,
    inicioEm = inicioEm,
    expiraEm = expiraEm,
    encerradaEm = encerradaEm
)

fun PresencaEntity.paraDominio(): Presenca = Presenca(
    id = id,
    codigoChamada = codigoChamada,
    professor = professor,
    disciplina = disciplina,
    turma = turma,
    aluno = aluno,
    registradoEm = registradoEm,
    perfil = Perfil.valueOf(perfil)
)

fun Presenca.paraEntidade(): PresencaEntity = PresencaEntity(
    id = id,
    codigoChamada = codigoChamada,
    professor = professor,
    disciplina = disciplina,
    turma = turma,
    aluno = aluno,
    registradoEm = registradoEm,
    perfil = perfil.name
)
