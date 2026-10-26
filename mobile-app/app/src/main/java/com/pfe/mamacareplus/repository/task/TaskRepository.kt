package com.pfe.mamacareplus.repository.task

import android.content.Context
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.api.task.TaskApiService
import com.pfe.mamacareplus.model.request.task.TaskRequest
import com.pfe.mamacareplus.model.response.task.TaskResponse
import com.pfe.mamacareplus.utils.session.TokenManager

class TaskRepository(context: Context) {

    private val tokenManager = TokenManager(context)

    private val api = ApiClient
        .create(context)
        .create(TaskApiService::class.java)

    suspend fun getTasks(): Result<List<TaskResponse>> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.getTasksByFemmeId(femmeId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de charger les tâches"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTask(
        title: String,
        description: String?,
        dueDate: String?
    ): Result<TaskResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.createTask(
                TaskRequest(
                    femmeId = femmeId,
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    isCompleted = false
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de créer la tâche"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(
        taskId: Int,
        title: String,
        description: String?,
        dueDate: String?,
        isCompleted: Boolean
    ): Result<TaskResponse> {
        return try {
            val femmeId = tokenManager.getFemmeId()
                ?: return Result.failure(Exception("Identifiant utilisatrice introuvable"))

            val response = api.updateTask(
                taskId,
                TaskRequest(
                    femmeId = femmeId,
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    isCompleted = isCompleted
                )
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de modifier la tâche"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeTask(taskId: Int): Result<TaskResponse> {
        return try {
            val response = api.markCompleted(taskId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de terminer la tâche"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uncompleteTask(taskId: Int): Result<TaskResponse> {
        return try {
            val response = api.markNotCompleted(taskId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Impossible de réactiver la tâche"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: Int): Result<Unit> {
        return try {
            val response = api.deleteTask(taskId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Impossible de supprimer la tâche"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
