package com.pfe.mamacareplus.api.onboarding

import com.pfe.mamacareplus.model.request.onboarding.OnboardingRequest
import com.pfe.mamacareplus.model.response.onboarding.OnboardingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OnboardingApiService {

    @POST("api/onboarding/complete")
    suspend fun completeOnboarding(
        @Body request: OnboardingRequest
    ): Response<OnboardingResponse>

    @GET("api/onboarding/me")
    suspend fun getMyOnboarding(): Response<OnboardingResponse>
}
