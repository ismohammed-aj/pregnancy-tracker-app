package com.pfe.mamacareplus.model.response.task

data class TaskResponse(
    val taskId: Int,
    val femmeId: Int,
    val username: String?,
    val title: String,
    val description: String?,
    val dueDate: String?,
    val isCompleted: Boolean,
    val createdAt: String?
)