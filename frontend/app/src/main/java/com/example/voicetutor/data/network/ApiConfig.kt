package com.example.voicetutor.data.network

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiConfig @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("api_config", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BASE_URL = "base_url"

        const val LOCALHOST_URL = "http://10.0.2.2:8000/api/"
        const val LOCALHOST_URL_DEVICE = "http://192.168.35.202:8000/api/"
        const val PROD_URL = "http://147.46.78.61:8003/api/"

        const val SERVER_TYPE_LOCALHOST = "localhost"
        const val SERVER_TYPE_PROD = "prod"
    }

    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, PROD_URL) ?: PROD_URL
    }
}

data class ServerOption(
    val name: String,
    val url: String,
    val type: String,
)
