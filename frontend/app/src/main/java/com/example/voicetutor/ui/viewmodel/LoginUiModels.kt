package com.example.voicetutor.ui.viewmodel

enum class LoginField {
    EMAIL,
    PASSWORD
}

sealed class LoginError {
    data class Input(val field: LoginField, val message: String) : LoginError()

    sealed class General(open val message: String, open val canRetry: Boolean) : LoginError() {
        data class InvalidCredentials(override val message: String) : General(message, true)
        data class AccountNotFound(override val message: String) : General(message, false)
        data class AccountLocked(override val message: String) : General(message, false)
        data class Server(override val message: String) : General(message, true)
        data class Network(override val message: String) : General(message, true)
        data class Unknown(override val message: String) : General(message, true)
    }
}

