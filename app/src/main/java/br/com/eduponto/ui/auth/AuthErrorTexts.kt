package br.com.eduponto.ui.auth

import androidx.annotation.StringRes
import br.com.eduponto.R
import br.com.eduponto.domain.model.AuthError
import br.com.eduponto.domain.model.BlockReason

@StringRes
fun AuthError.textoRes(): Int = when (this) {
    AuthError.EMAIL_INVALID -> R.string.err_email_invalid
    AuthError.PASSWORD_REQUIRED -> R.string.err_password_required
    AuthError.INVALID_CREDENTIALS -> R.string.err_invalid_credentials
    AuthError.NO_CONNECTION -> R.string.err_no_connection
    AuthError.TOO_MANY_REQUESTS -> R.string.err_too_many_requests
    AuthError.NOT_CONFIGURED -> R.string.err_not_configured
    AuthError.PASSWORD_TOO_SHORT -> R.string.err_password_short
    AuthError.PASSWORD_NEEDS_LETTER_AND_DIGIT -> R.string.err_password_letters_digits
    AuthError.PASSWORD_MISMATCH -> R.string.err_password_mismatch
    AuthError.PASSWORD_REJECTED -> R.string.err_password_rejected
    AuthError.UNKNOWN -> R.string.err_unknown
}

@StringRes
fun BlockReason.textoRes(): Int = when (this) {
    BlockReason.NO_PROFILE -> R.string.blocked_no_profile
    BlockReason.INACTIVE -> R.string.blocked_inactive
    BlockReason.NO_ROLE -> R.string.blocked_no_role
    BlockReason.LOAD_FAILED -> R.string.blocked_load_failed
}
