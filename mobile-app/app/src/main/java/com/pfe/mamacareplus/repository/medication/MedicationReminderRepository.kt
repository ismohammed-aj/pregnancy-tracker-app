package com.pfe.mamacareplus.repository.medication

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.medication.MedicationReminderApiService
import com.pfe.mamacareplus.model.request.medication.MedicationReminderRequest
import com.pfe.mamacareplus.model.response.medication.MedicationReminderResponse
import com.pfe.mamacareplus.notification.LocalNotificationScheduler
import com.pfe.mamacareplus.utils.session.TokenManager

class MedicationReminderRepository(context: Context) {

    private val tokenManager = TokenManager(context)
    private val notificationScheduler = LocalNotificationScheduler(context.applicationContext)

    private val api = ApiClient
        .create(context)
        .create(MedicationReminderApiService::class.java)

    suspend fun getReminders(): Result<List<MedicationReminderResponse>> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))
            val response = api.getRemindersByFemmeId(femmeId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger les rappels médicament"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodayReminders(): Result<List<MedicationReminderResponse>> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))
            val response = api.getTodayRemindersByFemmeId(femmeId)

            if (response.isSuccessful && response.body() != null) {
                val reminders = response.body()!!
                reminders.forEach(notificationScheduler::scheduleMedicationReminder)
                Result.success(reminders)
            } else {
                Result.failure(Exception("Impossible de charger les rappels médicament du jour"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createReminder(request: MedicationReminderRequest): Result<MedicationReminderResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))
            val response = api.createReminder(request.copy(femmeId = femmeId))

            if (response.isSuccessful && response.body() != null) {
                val reminder = response.body()!!
                notificationScheduler.scheduleMedicationReminder(reminder)
                Result.success(reminder)
            } else {
                val errorMessage = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    ?: response.message()
                    ?: "Impossible de créer le rappel médicament"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReminder(
        reminderId: Int,
        request: MedicationReminderRequest
    ): Result<MedicationReminderResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))
            val response = api.updateReminder(reminderId, request.copy(femmeId = femmeId))

            if (response.isSuccessful && response.body() != null) {
                val reminder = response.body()!!
                notificationScheduler.scheduleMedicationReminder(reminder)
                Result.success(reminder)
            } else {
                Result.failure(Exception("Impossible de modifier le rappel médicament"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markDone(reminderId: Int): Result<MedicationReminderResponse> {
        return try {
            val response = api.markDone(reminderId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de terminer le rappel médicament"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReminder(reminderId: Int): Result<Unit> {
        return try {
            val response = api.deleteReminder(reminderId)

            if (response.isSuccessful) {
                notificationScheduler.cancelMedicationReminder(reminderId)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Impossible de supprimer le rappel médicament"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
