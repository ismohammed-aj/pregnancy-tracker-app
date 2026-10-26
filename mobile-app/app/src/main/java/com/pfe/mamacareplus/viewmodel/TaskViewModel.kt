package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.request.medication.MedicationReminderRequest
import com.pfe.mamacareplus.model.response.medication.MedicationReminderResponse
import com.pfe.mamacareplus.model.response.task.TaskResponse
import com.pfe.mamacareplus.repository.medication.MedicationReminderRepository
import com.pfe.mamacareplus.repository.task.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = TaskRepository(application)
    private val medicationReminderRepository = MedicationReminderRepository(application)

    private val _taskState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val taskState: StateFlow<TaskUiState> = _taskState

    fun loadTasks() {
        _taskState.value = TaskUiState.Loading

        viewModelScope.launch {
            val result = repository.getTasks()
            val medicationResult = medicationReminderRepository.getTodayReminders()

            _taskState.value = result.fold(
                onSuccess = { tasks ->
                    TaskUiState.Success(
                        tasks = tasks,
                        medicationReminders = medicationResult.getOrElse { emptyList() }
                    )
                },
                onFailure = { TaskUiState.Error(it.message ?: "Impossible de charger les tâches") }
            )
        }
    }

    fun createTask(title: String, description: String?, dueDate: String?) {
        viewModelScope.launch {
            val result = repository.createTask(title, description, dueDate)

            result.onSuccess {
                loadTasks()
            }.onFailure {
                _taskState.value = TaskUiState.Error(it.message ?: "Impossible de créer la tâche")
            }
        }
    }

    fun updateTask(
        taskId: Int,
        title: String,
        description: String?,
        dueDate: String?,
        isCompleted: Boolean
    ) {
        viewModelScope.launch {
            val result = repository.updateTask(taskId, title, description, dueDate, isCompleted)

            result.onSuccess {
                loadTasks()
            }.onFailure {
                _taskState.value = TaskUiState.Error(it.message ?: "Impossible de modifier la tâche")
            }
        }
    }

    fun completeTask(taskId: Int) {
        viewModelScope.launch {
            val result = repository.completeTask(taskId)

            result.onSuccess {
                loadTasks()
            }.onFailure {
                _taskState.value = TaskUiState.Error(it.message ?: "Impossible de terminer la tâche")
            }
        }
    }

    fun uncompleteTask(taskId: Int) {
        viewModelScope.launch {
            val result = repository.uncompleteTask(taskId)

            result.onSuccess {
                loadTasks()
            }.onFailure {
                _taskState.value = TaskUiState.Error(it.message ?: "Impossible de réactiver la tâche")
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            val result = repository.deleteTask(taskId)

            result.onSuccess {
                loadTasks()
            }.onFailure {
                _taskState.value = TaskUiState.Error(it.message ?: "Impossible de supprimer la tâche")
            }
        }
    }

    fun completeMedicationReminder(reminderId: Int) {
        viewModelScope.launch {
            val result = medicationReminderRepository.markDone(reminderId)

            result.onSuccess {
                loadTasks()
            }.onFailure {
                _taskState.value = TaskUiState.Error(it.message ?: "Impossible de terminer le rappel médicament")
            }
        }
    }

    fun updateMedicationReminder(
        reminderId: Int,
        request: MedicationReminderRequest
    ) {
        viewModelScope.launch {
            val result = medicationReminderRepository.updateReminder(reminderId, request)

            result.onSuccess {
                loadTasks()
            }.onFailure {
                _taskState.value = TaskUiState.Error(it.message ?: "Impossible de modifier le rappel médicament")
            }
        }
    }

    fun deleteMedicationReminder(reminderId: Int) {
        viewModelScope.launch {
            val result = medicationReminderRepository.deleteReminder(reminderId)

            result.onSuccess {
                loadTasks()
            }.onFailure {
                _taskState.value = TaskUiState.Error(it.message ?: "Impossible de supprimer le rappel médicament")
            }
        }
    }
}

sealed class TaskUiState {
    object Loading : TaskUiState()
    data class Success(
        val tasks: List<TaskResponse>,
        val medicationReminders: List<MedicationReminderResponse>
    ) : TaskUiState()
    data class Error(val message: String) : TaskUiState()
}
