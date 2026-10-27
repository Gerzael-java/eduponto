package br.com.eduponto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Tema escuro é o padrão. (Uma opção de tema nas configurações entra depois.) */
@Composable
fun EduPontoTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaEscuro else EsquemaClaro,
        typography = TipografiaEduPonto,
        shapes = FormasEduPonto,
        content = content
    )
}
