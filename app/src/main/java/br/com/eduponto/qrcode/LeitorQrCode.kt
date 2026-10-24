package br.com.eduponto.qrcode

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private const val TAG = "LeitorQrCode"
private const val INTERVALO_REPETICAO_MS = 3_000L

private class UltimaLeitura {
    var texto: String? = null
    var instante: Long = 0L
}

/**
 * Pré-visualização da câmera que lê QR Codes. Chama [onTextoLido] na thread principal.
 * O mesmo texto só é entregue de novo depois de alguns segundos.
 * Requer a permissão de câmera já concedida (ver [ExigirPermissaoCamera]).
 */
@Composable
fun LeitorQrCode(
    onTextoLido: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current
    val aoLer by rememberUpdatedState(onTextoLido)

    val previewView = remember { PreviewView(contexto) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val leitor = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }
    val ultima = remember { UltimaLeitura() }

    DisposableEffect(ciclo) {
        val futuroProvedor = ProcessCameraProvider.getInstance(contexto)
        futuroProvedor.addListener(
            {
                try {
                    val provedor = futuroProvedor.get()
                    val previa = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analise = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analise.setAnalyzer(executor) { imagem ->
                        analisarImagem(imagem, leitor) { texto ->
                            val agora = System.currentTimeMillis()
                            val repetido = texto == ultima.texto &&
                                agora - ultima.instante < INTERVALO_REPETICAO_MS
                            if (!repetido) {
                                ultima.texto = texto
                                ultima.instante = agora
                                previewView.post { aoLer(texto) }
                            }
                        }
                    }
                    provedor.unbindAll()
                    provedor.bindToLifecycle(
                        ciclo,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        previa,
                        analise
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Falha ao iniciar a câmera", e)
                }
            },
            ContextCompat.getMainExecutor(contexto)
        )
        onDispose {
            if (futuroProvedor.isDone) {
                try {
                    futuroProvedor.get().unbindAll()
                } catch (e: Exception) {
                    Log.e(TAG, "Falha ao liberar a câmera", e)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            leitor.close()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

@OptIn(ExperimentalGetImage::class)
private fun analisarImagem(
    imagemProxy: ImageProxy,
    leitor: BarcodeScanner,
    aoEncontrar: (String) -> Unit
) {
    val imagemMidia = imagemProxy.image
    if (imagemMidia == null) {
        imagemProxy.close()
        return
    }
    val entrada = InputImage.fromMediaImage(imagemMidia, imagemProxy.imageInfo.rotationDegrees)
    leitor.process(entrada)
        .addOnSuccessListener { codigos ->
            val valor = codigos.firstOrNull { !it.rawValue.isNullOrEmpty() }?.rawValue
            if (valor != null) aoEncontrar(valor)
        }
        .addOnCompleteListener { imagemProxy.close() }
}
