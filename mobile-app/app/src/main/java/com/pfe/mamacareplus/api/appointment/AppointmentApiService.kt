package com.pfe.mamacareplus.api.appointment

import com.pfe.mamacareplus.model.request.appointment.AppointmentRequest
import com.pfe.mamacareplus.model.response.appointment.AppointmentResponse
import retrofit2.Response
import retrofit2.http.*

interface AppointmentApiService {

    @GET("api/appointments/femme/{femmeId}")
    suspend fun getAppointmentsByFemmeId(
        @Path("femmeId") femmeId: Int
    ): Response<List<AppointmentResponse>>

    @POST("api/appointments")
    suspend fun createAppointment(
        @Body request: AppointmentRequest
    ): Response<AppointmentResponse>

    @PUT("api/appointments/{appointmentId}")
    suspend fun updateAppointment(
        @Path("appointmentId") appointmentId: Int,
        @Body request: AppointmentRequest
    ): Response<AppointmentResponse>

    @PATCH("api/appointments/{appointmentId}/status/{status}")
    suspend fun updateStatus(
        @Path("appointmentId") appointmentId: Int,
        @Path("status") status: String
    ): Response<AppointmentResponse>

    @DELETE("api/appointments/{appointmentId}")
    suspend fun deleteAppointment(
        @Path("appointmentId") appointmentId: Int
    ): Response<Unit>
}
