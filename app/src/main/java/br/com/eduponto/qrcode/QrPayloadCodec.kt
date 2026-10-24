package br.com.eduponto.qrcode

import org.json.JSONException
import org.json.JSONObject

/** Converte os payloads em texto (JSON compacto) e de volta. Sem assinatura: não é à prova de fraude. */
object QrPayloadCodec {

    private const val VERSAO = 1

    fun codificar(payload: QrPayload): String {
        val json = JSONObject()
        json.put("v", VERSAO)
        when (payload) {
            is QrPayload.Chamada -> {
                json.put("t", "c")
                json.put("k", payload.codigo)
                json.put("p", payload.professor)
                json.put("d", payload.disciplina)
                json.put("u", payload.turma)
                json.put("i", payload.inicioEm)
                json.put("e", payload.expiraEm)
            }
            is QrPayload.Comprovante -> {
                json.put("t", "p")
                json.put("k", payload.codigo)
                json.put("a", payload.aluno)
                json.put("r", payload.registradoEm)
            }
        }
        return json.toString()
    }

    /** Retorna null se o texto não for um payload válido do EduPonto. */
    fun decodificar(texto: String): QrPayload? {
        return try {
            val json = JSONObject(texto.trim())
            if (json.getInt("v") != VERSAO) {
                null
            } else {
                when (json.getString("t")) {
                    "c" -> QrPayload.Chamada(
                        codigo = json.getString("k"),
                        professor = json.getString("p"),
                        disciplina = json.getString("d"),
                        turma = json.getString("u"),
                        inicioEm = json.getLong("i"),
                        expiraEm = json.getLong("e")
                    )
                    "p" -> QrPayload.Comprovante(
                        codigo = json.getString("k"),
                        aluno = json.getString("a"),
                        registradoEm = json.getLong("r")
                    )
                    else -> null
                }
            }
        } catch (e: JSONException) {
            null
        }
    }
}
