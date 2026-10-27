package br.com.eduponto.data.remote

import android.util.Base64
import br.com.eduponto.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseProvider {

    /**
     * Cria o cliente do Supabase com a URL e a chave PÚBLICA definidas em local.properties.
     * Retorna null se faltar configuração ou se a chave informada parecer uma chave secreta
     * (chaves secretas nunca podem estar no app).
     */
    fun criar(): SupabaseClient? {
        val url = BuildConfig.SUPABASE_URL
        val chave = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        if (url.isBlank() || chave.isBlank() || pareceChaveSecreta(chave)) return null

        return createSupabaseClient(supabaseUrl = url, supabaseKey = chave) {
            install(Auth)
            install(Postgrest)
        }
    }

    private fun pareceChaveSecreta(chave: String): Boolean {
        if (chave.startsWith("sb_secret_")) return true
        val partes = chave.split('.')
        if (partes.size != 3) return false
        return try {
            val corpo = Base64.decode(
                partes[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
            String(corpo).contains("\"service_role\"")
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
