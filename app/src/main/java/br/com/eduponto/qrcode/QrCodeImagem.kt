package br.com.eduponto.qrcode

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun QrCodeImagem(
    texto: String,
    modifier: Modifier = Modifier,
    tamanho: Dp = 240.dp
) {
    val bitmap = remember(texto) { GeradorQrCode.gerarBitmap(texto, 720) }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "QR Code",
        filterQuality = FilterQuality.None,
        modifier = modifier.size(tamanho)
    )
}
