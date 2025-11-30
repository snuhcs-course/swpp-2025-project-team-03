package com.example.voicetutor.ui.viewmodel

enum class SignupField {
    NAME,
    EMAIL,
    PASSWORD,
    CONFIRM_PASSWORD,
}

sealed class SignupError {
    data class Input(val field: SignupField, val message: String) : SignupError()

    sealed class General(open val message: String, open val canRetry: Boolean) : SignupError() {
        data class DuplicateEmail(override val message: String) : General(message, false)
        data class Server(override val message: String) : General(message, true)
        data class Network(override val message: String) : General(message, true)
        data class Unknown(override val message: String) : General(message, true)
    }
}
