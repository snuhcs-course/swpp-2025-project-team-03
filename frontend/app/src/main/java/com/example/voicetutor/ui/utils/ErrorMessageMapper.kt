package com.example.voicetutor.ui.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object ErrorMessageMapper {

    fun getErrorMessage(exception: Throwable?): String {
        if (exception == null) {
            return "알 수 없는 오류가 발생했습니다."
        }

        return when (exception) {
            is ConnectException,
            is UnknownHostException,
            -> "네트워크 연결에 실패했습니다. 인터넷 연결을 확인하고 다시 시도해주세요."

            is SocketTimeoutException -> "요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."

            is SSLException -> "보안 연결에 실패했습니다. 잠시 후 다시 시도해주세요."

            else -> {
                val message = exception.message ?: "알 수 없는 오류가 발생했습니다."

                when {
                    message.contains("Failed to connect", ignoreCase = true) ||
                        message.contains("Unable to resolve host", ignoreCase = true) ||
                        message.contains("Connection refused", ignoreCase = true) -> {
                        "네트워크 연결에 실패했습니다. 인터넷 연결을 확인하고 다시 시도해주세요."
                    }

                    message.contains("timeout", ignoreCase = true) ||
                        message.contains("timed out", ignoreCase = true) -> {
                        "요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."
                    }

                    message.contains("HTTP", ignoreCase = true) && message.contains("500", ignoreCase = true) -> {
                        "서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    }

                    message.contains("HTTP", ignoreCase = true) && message.contains("404", ignoreCase = true) -> {
                        "요청한 정보를 찾을 수 없습니다."
                    }

                    message.contains("HTTP", ignoreCase = true) && message.contains("403", ignoreCase = true) -> {
                        "접근 권한이 없습니다."
                    }

                    message.contains("HTTP", ignoreCase = true) && message.contains("401", ignoreCase = true) -> {
                        "인증이 필요합니다. 다시 로그인해주세요."
                    }

                    message.contains("java.", ignoreCase = true) ||
                        message.contains("kotlin.", ignoreCase = true) ||
                        message.contains("Exception", ignoreCase = true) -> {
                        "오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    }

                    else -> {
                        if (message.length > 100 || message.contains("at ", ignoreCase = true)) {
                            "오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                        } else {
                            message
                        }
                    }
                }
            }
        }
    }

    fun getErrorMessage(message: String?): String {
        if (message.isNullOrBlank()) {
            return "알 수 없는 오류가 발생했습니다."
        }

        return when {
            message.contains("Failed to connect", ignoreCase = true) ||
                message.contains("Unable to resolve host", ignoreCase = true) ||
                message.contains("Connection refused", ignoreCase = true) -> {
                "네트워크 연결에 실패했습니다. 인터넷 연결을 확인하고 다시 시도해주세요."
            }

            message.contains("timeout", ignoreCase = true) ||
                message.contains("timed out", ignoreCase = true) -> {
                "요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."
            }

            message.contains("HTTP", ignoreCase = true) && message.contains("500", ignoreCase = true) -> {
                "서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            }

            message.contains("HTTP", ignoreCase = true) && message.contains("404", ignoreCase = true) -> {
                "요청한 정보를 찾을 수 없습니다."
            }

            message.contains("HTTP", ignoreCase = true) && message.contains("403", ignoreCase = true) -> {
                "접근 권한이 없습니다."
            }

            message.contains("HTTP", ignoreCase = true) && message.contains("401", ignoreCase = true) -> {
                "인증이 필요합니다. 다시 로그인해주세요."
            }

            message.contains("java.", ignoreCase = true) ||
                message.contains("kotlin.", ignoreCase = true) ||
                message.contains("Exception", ignoreCase = true) -> {
                "오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            }

            else -> {
                if (message.length > 100 || message.contains("at ", ignoreCase = true)) {
                    "오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                } else {
                    message
                }
            }
        }
    }

    /**
     * 에러 메시지가 네트워크 관련 에러인지 확인합니다.
     */
    fun isNetworkError(errorMessage: String?): Boolean {
        if (errorMessage == null) return false

        val networkErrorKeywords = listOf(
            "네트워크",
            "연결",
            "timeout",
            "timed out",
            "Failed to connect",
            "Unable to resolve host",
            "Connection refused",
            "SSL",
            "보안 연결",
            "요청 시간이 초과",
            "네트워크 연결에 실패",
        )

        return networkErrorKeywords.any { keyword ->
            errorMessage.contains(keyword, ignoreCase = true)
        }
    }

    /**
     * 예외가 네트워크 관련 에러인지 확인합니다.
     */
    fun isNetworkError(exception: Throwable?): Boolean {
        if (exception == null) return false

        return when (exception) {
            is java.net.ConnectException,
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is javax.net.ssl.SSLException,
            -> true
            else -> {
                val message = exception.message ?: ""
                isNetworkError(message)
            }
        }
    }
}
