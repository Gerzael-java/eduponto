package br.com.eduponto.qrcode

import br.com.eduponto.domain.model.Chamada
import br.com.eduponto.domain.model.Presenca

fun Chamada.paraPayload(): QrPayload.Chamada = QrPayload.Chamada(
    codigo = codigo,
    professor = professor,
    disciplina = disciplina,
    turma = turma,
    inicioEm = inicioEm,
    expiraEm = expiraEm
)

fun Presenca.paraComprovante(): QrPayload.Comprovante = QrPayload.Comprovante(
    codigo = codigoChamada,
    aluno = aluno,
    registradoEm = registradoEm
)
