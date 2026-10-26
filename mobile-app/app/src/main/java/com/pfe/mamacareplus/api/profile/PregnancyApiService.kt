    package com.pfe.mamacareplus.api.profile

    import com.pfe.mamacareplus.model.request.profile.PregnancyProfileRequest
    import com.pfe.mamacareplus.model.response.profile.PregnancyProfileResponse
    import retrofit2.Response
    import retrofit2.http.*

    interface PregnancyApiService {

        @GET("api/pregnancy-profiles/femme/{femmeId}")
        suspend fun getProfileByFemmeId(
            @Path("femmeId") femmeId: Int
        ): Response<PregnancyProfileResponse>

        @POST("api/pregnancy-profiles")
        suspend fun createProfile(
            @Body request: PregnancyProfileRequest
        ): Response<PregnancyProfileResponse>

        @PUT("api/pregnancy-profiles/{profileId}")
        suspend fun updateProfile(
            @Path("profileId") profileId: Int,
            @Body request: PregnancyProfileRequest
        ): Response<PregnancyProfileResponse>
    }