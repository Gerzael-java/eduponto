// Arquivo raiz: apenas declara os plugins (sem aplicá-los).
// AGP 9 já traz o Kotlin embutido, por isso NÃO existe o plugin
// "org.jetbrains.kotlin.android". O plugin do Compose, na versão do Kotlin
// definida no catálogo, também faz o Gradle usar essa versão do Kotlin.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
