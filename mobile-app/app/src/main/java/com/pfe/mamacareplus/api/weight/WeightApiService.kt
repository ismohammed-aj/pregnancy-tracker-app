package com.pfe.mamacareplus.api.weight

import com.pfe.mamacareplus.model.request.weight.WeightRecordRequest
import com.pfe.mamacareplus.model.response.weight.WeightRecordResponse
import retrofit2.Response
import retrofit2.http.*

interface WeightApiService {

    @GET("api/weight-records/pregnancy-profile/{pregnancyProfileId}")
    suspend fun getWeightRecordsByPregnancyProfileId(
        @Path("pregnancyProfileId") pregnancyProfileId: Int
    ): Response<List<WeightRecordResponse>>

    @POST("api/weight-records")
    suspend fun createWeightRecord(
        @Body request: WeightRecordRequest
    ): Response<WeightRecordResponse>

    @PUT("api/weight-records/{weightRecordId}")
    suspend fun updateWeightRecord(
        @Path("weightRecordId") weightRecordId: Int,
        @Body request: WeightRecordRequest
    ): Response<WeightRecordResponse>

    @DELETE("api/weight-records/{weightRecordId}")
    suspend fun deleteWeightRecord(
        @Path("weightRecordId") weightRecordId: Int
    ): Response<Unit>
}