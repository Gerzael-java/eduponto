package br.com.eduponto.domain.repository

import br.com.eduponto.domain.model.AuthResult
import br.com.eduponto.domain.model.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val state: StateFlow<AuthState>
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun changePassword(newPassword: String): AuthResult
    suspend fun reloadProfile()
    suspend fun signOut()
}
