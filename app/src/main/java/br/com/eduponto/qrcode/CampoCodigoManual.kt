package br.com.eduponto.qrcode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Alternativa à câmera: colar o texto do QR Code (útil se a câmera falhar ou para testes). */
@Composable
fun CampoCodigoManual(
    rotulo: String,
    onConfirmar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var texto by remember { mutableStateOf("") }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text(rotulo) },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = {
                onConfirmar(texto)
                texto = ""
            },
            enabled = texto.isNotBlank()
        ) {
            Text("Validar código")
        }
    }
}
