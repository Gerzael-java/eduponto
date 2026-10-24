package br.com.eduponto.qrcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object GeradorQrCode {

    /** Gera o QR Code como imagem quadrada, preto sobre branco. */
    fun gerarBitmap(texto: String, tamanho: Int): Bitmap {
        val dicas = mapOf<EncodeHintType, Any>(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
        )
        val matriz = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamanho, tamanho, dicas)
        val largura = matriz.width
        val altura = matriz.height
        val pixels = IntArray(largura * altura)
        for (y in 0 until altura) {
            val deslocamento = y * largura
            for (x in 0 until largura) {
                pixels[deslocamento + x] = if (matriz.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, largura, altura, Bitmap.Config.ARGB_8888)
    }
}
