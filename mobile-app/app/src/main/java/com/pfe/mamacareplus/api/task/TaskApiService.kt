package com.pfe.mamacareplus.api.task

import com.pfe.mamacareplus.model.request.task.TaskRequest
import com.pfe.mamacareplus.model.response.task.TaskResponse
import retrofit2.Response
import retrofit2.http.*

interface TaskApiService {

    @GET("api/tasks")
    suspend fun getAllTasks(): Response<List<TaskResponse>>

    @GET("api/tasks/femme/{femmeId}")
    suspend fun getTasksByFemmeId(
        @Path("femmeId") femmeId: Int
    ): Response<List<TaskResponse>>

    @POST("api/tasks")
    suspend fun createTask(
        @Body request: TaskRequest
    ): Response<TaskResponse>

    @PUT("api/tasks/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: Int,
        @Body request: TaskRequest
    ): Response<TaskResponse>

    @PATCH("api/tasks/{taskId}/complete")
    suspend fun markCompleted(
        @Path("taskId") taskId: Int
    ): Response<TaskResponse>

    @PATCH("api/tasks/{taskId}/uncomplete")
    suspend fun markNotCompleted(
        @Path("taskId") taskId: Int
    ): Response<TaskResponse>

    @DELETE("api/tasks/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: Int
    ): Response<Unit>
}