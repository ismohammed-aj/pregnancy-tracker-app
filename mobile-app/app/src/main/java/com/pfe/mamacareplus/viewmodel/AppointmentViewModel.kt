package com.pfe.mamacareplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pfe.mamacareplus.model.response.appointment.AppointmentResponse
import com.pfe.mamacareplus.repository.appointment.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppointmentViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AppointmentRepository(application)

    private val _appointmentState =
        MutableStateFlow<AppointmentUiState>(AppointmentUiState.Loading)

    val appointmentState: StateFlow<AppointmentUiState> = _appointmentState

    fun loadAppointments() {
        _appointmentState.value = AppointmentUiState.Loading

        viewModelScope.launch {
            val result = repository.getAppointments()

            _appointmentState.value = result.fold(
                onSuccess = { AppointmentUiState.Success(it) },
                onFailure = { AppointmentUiState.Error(it.message ?: "Impossible de charger les rendez-vous") }
            )
        }
    }

    fun createAppointment(
        title: String,
        appointmentDate: String,
        location: String?,
        doctorName: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val result = repository.createAppointment(
                title = title,
                appointmentDate = appointmentDate,
                location = location,
                doctorName = doctorName,
                notes = notes
            )

            result.onSuccess {
                loadAppointments()
            }.onFailure {
                _appointmentState.value =
                    AppointmentUiState.Error(it.message ?: "Impossible de créer le rendez-vous")
            }
        }
    }

    fun markCompleted(appointmentId: Int) {
        viewModelScope.launch {
            val result = repository.markCompleted(appointmentId)

            result.onSuccess {
                loadAppointments()
            }.onFailure {
                _appointmentState.value =
                    AppointmentUiState.Error(it.message ?: "Impossible de modifier le rendez-vous")
            }
        }
    }

    fun updateAppointment(
        appointmentId: Int,
        title: String,
        appointmentDate: String,
        location: String?,
        doctorName: String?,
        notes: String?,
        status: String
    ) {
        viewModelScope.launch {
            val result = repository.updateAppointment(
                appointmentId = appointmentId,
                title = title,
                appointmentDate = appointmentDate,
                location = location,
                doctorName = doctorName,
                notes = notes,
                status = status
            )

            result.onSuccess {
                loadAppointments()
            }.onFailure {
                _appointmentState.value =
                    AppointmentUiState.Error(it.message ?: "Impossible de modifier le rendez-vous")
            }
        }
    }

    fun cancelAppointment(appointmentId: Int) {
        viewModelScope.launch {
            val result = repository.cancelAppointment(appointmentId)

            result.onSuccess {
                loadAppointments()
            }.onFailure {
                _appointmentState.value =
                    AppointmentUiState.Error(it.message ?: "Impossible d'annuler le rendez-vous")
            }
        }
    }

    fun deleteAppointment(appointmentId: Int) {
        viewModelScope.launch {
            val result = repository.deleteAppointment(appointmentId)

            result.onSuccess {
                loadAppointments()
            }.onFailure {
                _appointmentState.value =
                    AppointmentUiState.Error(it.message ?: "Impossible de supprimer le rendez-vous")
            }
        }
    }
}

sealed class AppointmentUiState {
    object Loading : AppointmentUiState()
    data class Success(val appointments: List<AppointmentResponse>) : AppointmentUiState()
    data class Error(val message: String) : AppointmentUiState()
}
