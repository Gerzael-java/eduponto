package br.com.eduponto.qrcode

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/** Mostra [conteudo] quando a permissão da câmera foi concedida; senão, explica e pede a permissão. */
@Composable
fun ExigirPermissaoCamera(conteudo: @Composable () -> Unit) {
    val contexto = LocalContext.current
    var concedida by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val pedirPermissao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { resultado -> concedida = resultado }

    if (concedida) {
        conteudo()
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "O EduPonto precisa da câmera para ler o QR Code. " +
                    "Se você negou antes, libere em Configurações > Apps > EduPonto > Permissões.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { pedirPermissao.launch(Manifest.permission.CAMERA) }) {
                Text("Permitir câmera")
            }
        }
    }
}
