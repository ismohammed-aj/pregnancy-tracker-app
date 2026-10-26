package com.pfe.mamacareplus.api.medication

import com.pfe.mamacareplus.model.request.medication.MedicationReminderRequest
import com.pfe.mamacareplus.model.response.medication.MedicationReminderResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MedicationReminderApiService {

    @POST("api/medication-reminders")
    suspend fun createReminder(
        @Body request: MedicationReminderRequest
    ): Response<MedicationReminderResponse>

    @GET("api/medication-reminders/user/{userId}")
    suspend fun getRemindersByUserId(
        @Path("userId") userId: Int
    ): Response<List<MedicationReminderResponse>>

    @GET("api/medication-reminders/femme/{femmeId}")
    suspend fun getRemindersByFemmeId(
        @Path("femmeId") femmeId: Int
    ): Response<List<MedicationReminderResponse>>

    @GET("api/medication-reminders/today/{userId}")
    suspend fun getTodayRemindersByUserId(
        @Path("userId") userId: Int
    ): Response<List<MedicationReminderResponse>>

    @GET("api/medication-reminders/today/femme/{femmeId}")
    suspend fun getTodayRemindersByFemmeId(
        @Path("femmeId") femmeId: Int
    ): Response<List<MedicationReminderResponse>>

    @PUT("api/medication-reminders/{id}")
    suspend fun updateReminder(
        @Path("id") id: Int,
        @Body request: MedicationReminderRequest
    ): Response<MedicationReminderResponse>

    @PATCH("api/medication-reminders/{id}/done")
    suspend fun markDone(
        @Path("id") id: Int
    ): Response<MedicationReminderResponse>

    @DELETE("api/medication-reminders/{id}")
    suspend fun deleteReminder(
        @Path("id") id: Int
    ): Response<Unit>
}
