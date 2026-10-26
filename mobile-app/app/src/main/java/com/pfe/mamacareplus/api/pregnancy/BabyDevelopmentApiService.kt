package com.pfe.mamacareplus.api.pregnancy

import com.pfe.mamacareplus.model.response.pregnancy.BabyDevelopmentResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface BabyDevelopmentApiService {

    @GET("api/baby-developments")
    suspend fun getAllBabyDevelopments(): Response<List<BabyDevelopmentResponse>>

    @GET("api/baby-developments/{babyDevelopmentId}")
    suspend fun getBabyDevelopmentById(
        @Path("babyDevelopmentId") babyDevelopmentId: Int
    ): Response<BabyDevelopmentResponse>

    @GET("api/baby-developments/week/{weekNumber}")
    suspend fun getBabyDevelopmentByWeek(
        @Path("weekNumber") weekNumber: Int
    ): Response<BabyDevelopmentResponse>
}