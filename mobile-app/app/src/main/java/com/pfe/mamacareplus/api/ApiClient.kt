package com.pfe.mamacareplus.api

import android.content.Context
import android.os.Build
import com.pfe.mamacareplus.utils.session.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // IMPORTANT:
    // Android Emulator -> 10.0.2.2
    // Real phone -> your PC local IP

    private const val EMULATOR_BASE_URL = "http://10.0.2.2:8090/"
    private const val PHONE_BASE_URL = "http://192.168.100.234:8090/"

    val BASE_URL: String
        get() = if (isRunningOnEmulator()) EMULATOR_BASE_URL else PHONE_BASE_URL

    fun create(context: Context, includeAuth: Boolean = true): Retrofit {

        val tokenManager = TokenManager(context)

        val authInterceptor = Interceptor { chain ->

            val requestBuilder = chain.request().newBuilder()

            val token = tokenManager.getToken()

            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader(
                    "Authorization",
                    "Bearer $token"
                )
            }

            chain.proceed(requestBuilder.build())
        }

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val clientBuilder = OkHttpClient.Builder()

        if (includeAuth) {
            clientBuilder.addInterceptor(authInterceptor)
        }

        val client = clientBuilder
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun isRunningOnEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk", ignoreCase = true) ||
                Build.MODEL.contains("Emulator", ignoreCase = true) ||
                Build.MODEL.contains("Android SDK built for x86", ignoreCase = true) ||
                Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
                (Build.BRAND.startsWith("generic", ignoreCase = true) &&
                        Build.DEVICE.startsWith("generic", ignoreCase = true)) ||
                Build.PRODUCT == "google_sdk"
    }
}



