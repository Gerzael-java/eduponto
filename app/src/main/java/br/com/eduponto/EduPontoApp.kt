package br.com.eduponto

import android.app.Application

class EduPontoApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
