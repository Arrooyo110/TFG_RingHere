package com.cdm.tfg_ringhere.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cdm.tfg_ringhere.data.repository.AlarmaRepository
import com.cdm.tfg_ringhere.model.Alarma
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmaViewModel(private val repository: AlarmaRepository) : ViewModel() {

    // Transformamos el Flow de Room en un StateFlow que Compose puede "observar" en tiempo real
    val alarmas: StateFlow<List<Alarma>> = repository.allAlarmas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Lanzamos las operaciones de base de datos en hilos secundarios (viewModelScope)
    fun insert(alarma: Alarma) = viewModelScope.launch {
        repository.insert(alarma)
    }

    fun delete(alarma: Alarma) = viewModelScope.launch {
        repository.delete(alarma)
    }
}

// Necesitamos este "Factory" para poder pasarle el Repositorio al ViewModel cuando lo creemos
class AlarmaViewModelFactory(private val repository: AlarmaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmaViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}