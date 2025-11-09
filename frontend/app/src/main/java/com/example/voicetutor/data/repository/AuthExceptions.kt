package com.example.voicetutor.data.repository

sealed class SignupException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class DuplicateEmail(message: String) : SignupException(message)
    class Server(message: String) : SignupException(message)
    class Network(message: String, cause: Throwable? = null) : SignupException(message, cause)
    class Unknown(message: String) : SignupException(message)
}

sealed class LoginException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidCredentials(message: String) : LoginException(message)
    class AccountNotFound(message: String) : LoginException(message)
    class AccountLocked(message: String) : LoginException(message)
    class Server(message: String) : LoginException(message)
    class Network(message: String, cause: Throwable? = null) : LoginException(message, cause)
    class Unknown(message: String) : LoginException(message)
}

