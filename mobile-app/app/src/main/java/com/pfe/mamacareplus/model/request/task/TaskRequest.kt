package com.pfe.mamacareplus.model.request.task

data class TaskRequest(
    val femmeId: Int,
    val title: String,
    val description: String? = null,
    val dueDate: String? = null,
    val isCompleted: Boolean? = false
)