package br.com.eduponto.qrcode

/** Conteúdo dos QR Codes trocados entre professor e aluno. */
sealed interface QrPayload {

    /** QR exibido pelo professor: identifica a chamada aberta. */
    data class Chamada(
        val codigo: String,
        val professor: String,
        val disciplina: String,
        val turma: String,
        val inicioEm: Long,
        val expiraEm: Long
    ) : QrPayload

    /** QR exibido pelo aluno depois de registrar presença: comprovante para o professor. */
    data class Comprovante(
        val codigo: String,
        val aluno: String,
        val registradoEm: Long
    ) : QrPayload
}
