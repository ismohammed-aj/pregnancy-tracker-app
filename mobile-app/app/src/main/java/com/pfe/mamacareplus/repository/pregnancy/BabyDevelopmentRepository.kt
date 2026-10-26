package com.pfe.mamacareplus.repository.pregnancy

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.pregnancy.BabyDevelopmentApiService
import com.pfe.mamacareplus.model.response.pregnancy.BabyDevelopmentResponse

class BabyDevelopmentRepository(context: Context) {

    private val api = ApiClient
        .create(context)
        .create(BabyDevelopmentApiService::class.java)

    suspend fun getDevelopmentByWeek(weekNumber: Int): Result<BabyDevelopmentResponse> {
        return try {
            val response = api.getBabyDevelopmentByWeek(weekNumber)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Développement du bébé introuvable pour la semaine $weekNumber"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllDevelopments(): Result<List<BabyDevelopmentResponse>> {
        return try {
            val response = api.getAllBabyDevelopments()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger le développement du bébé"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
