package com.pfe.mamacareplus.repository.onboarding

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.onboarding.OnboardingApiService
import com.pfe.mamacareplus.model.request.onboarding.OnboardingRequest
import com.pfe.mamacareplus.model.response.onboarding.OnboardingResponse
import org.json.JSONObject
import retrofit2.Response

class OnboardingRepository(context: Context) {

    private val api = ApiClient
        .create(context)
        .create(OnboardingApiService::class.java)

    suspend fun completeOnboarding(request: OnboardingRequest): Result<OnboardingResponse> {
        return try {
            val response = api.completeOnboarding(request)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body)
            } else {
                Result.failure(Exception(body?.message ?: buildHttpError("Sauvegarde onboarding", response).message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyOnboarding(): Result<OnboardingResponse> {
        return try {
            val response = api.getMyOnboarding()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(buildHttpError("Lecture onboarding", response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isOnboardingCompleted(): Result<Boolean> {
        return getMyOnboarding().map { it.onboardingCompleted == true }
    }

    private fun buildHttpError(action: String, response: Response<*>): Exception {
        val errorBody = runCatching { response.errorBody()?.string() }.getOrNull()
        val message = errorBody?.let { extractServerMessage(it) }
            ?: "$action impossible (${response.code()})."
        return Exception(message)
    }

    private fun extractServerMessage(body: String): String? {
        val trimmed = body.trim()
        if (trimmed.isBlank()) return null

        return runCatching {
            val json = JSONObject(trimmed)
            json.optString("message")
                .ifBlank { json.optString("error") }
                .takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
