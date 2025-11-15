package com.example.voicetutor.ui.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 예외를 사용자 친화적인 에러 메시지로 변환하는 유틸리티
 */
object ErrorMessageMapper {
    
    /**
     * 예외를 사용자 친화적인 메시지로 변환
     */
    fun getErrorMessage(exception: Throwable?): String {
        if (exception == null) {
            return "알 수 없는 오류가 발생했습니다."
        }
        
        return when (exception) {
            is ConnectException,
            is UnknownHostException -> "네트워크 연결에 실패했습니다. 인터넷 연결을 확인하고 다시 시도해주세요."
            
            is SocketTimeoutException -> "요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."
            
            is SSLException -> "보안 연결에 실패했습니다. 잠시 후 다시 시도해주세요."
            
            else -> {
                // 예외 메시지에서 기술적인 부분 제거
                val message = exception.message ?: "알 수 없는 오류가 발생했습니다."
                
                // 기술적인 에러 메시지 패턴 확인
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
                        // 이미 사용자 친화적인 메시지일 수 있으므로 그대로 반환
                        // 하지만 너무 길거나 기술적인 경우 기본 메시지 반환
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
    
    /**
     * 에러 메시지 문자열을 사용자 친화적으로 변환
     */
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
}

