package br.com.eduponto.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun copiarTexto(contexto: Context, rotulo: String, texto: String) {
    val gerenciador = contexto.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    gerenciador.setPrimaryClip(ClipData.newPlainText(rotulo, texto))
}
