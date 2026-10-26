package com.pfe.mamacareplus.repository.appointment

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.appointment.AppointmentApiService
import com.pfe.mamacareplus.model.request.appointment.AppointmentRequest
import com.pfe.mamacareplus.model.response.appointment.AppointmentResponse
import com.pfe.mamacareplus.utils.session.TokenManager

class AppointmentRepository(context: Context) {

    private val tokenManager = TokenManager(context)

    private val api = ApiClient
        .create(context)
        .create(AppointmentApiService::class.java)

    suspend fun getAppointments(): Result<List<AppointmentResponse>> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.getAppointmentsByFemmeId(femmeId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger les rendez-vous"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAppointment(
        title: String,
        appointmentDate: String,
        location: String?,
        doctorName: String?,
        notes: String?
    ): Result<AppointmentResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.createAppointment(
                AppointmentRequest(
                    femmeId = femmeId,
                    title = title,
                    appointmentDate = appointmentDate,
                    location = location,
                    doctorName = doctorName,
                    notes = notes,
                    status = "pending"
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de créer le rendez-vous"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointment(
        appointmentId: Int,
        title: String,
        appointmentDate: String,
        location: String?,
        doctorName: String?,
        notes: String?,
        status: String
    ): Result<AppointmentResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.updateAppointment(
                appointmentId,
                AppointmentRequest(
                    femmeId = femmeId,
                    title = title,
                    appointmentDate = appointmentDate,
                    location = location,
                    doctorName = doctorName,
                    notes = notes,
                    status = status
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de modifier le rendez-vous"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markCompleted(appointmentId: Int): Result<AppointmentResponse> {
        return try {
            val response = api.updateStatus(appointmentId, "completed")

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de modifier le rendez-vous"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelAppointment(appointmentId: Int): Result<AppointmentResponse> {
        return try {
            val response = api.updateStatus(appointmentId, "cancelled")

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible d'annuler le rendez-vous"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAppointment(appointmentId: Int): Result<Unit> {
        return try {
            val response = api.deleteAppointment(appointmentId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Impossible de supprimer le rendez-vous"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
