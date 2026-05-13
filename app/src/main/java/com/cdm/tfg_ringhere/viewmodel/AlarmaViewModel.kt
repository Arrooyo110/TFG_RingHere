package com.cdm.tfg_ringhere.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cdm.tfg_ringhere.data.network.RetrofitClient
import com.cdm.tfg_ringhere.data.repository.AlarmaRepository
import com.cdm.tfg_ringhere.model.Alarma
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class AlarmaViewModel(private val repository: AlarmaRepository) : ViewModel() {

    // Transformamos el Flow de Room en un StateFlow que Compose puede "observar" en tiempo real
    val alarmas: StateFlow<List<Alarma>> = repository.allAlarmas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Un "chivato" que nos dirá si el usuario ha iniciado sesión correctamente
    private val _loginExitoso = MutableStateFlow(false)
    val loginExitoso = _loginExitoso.asStateFlow()

    // Chivato para el mensaje de error
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError = _mensajeError.asStateFlow()

    // Lanzamos las operaciones de base de datos en hilos secundarios (viewModelScope)
    fun insert(alarma: Alarma) = viewModelScope.launch {
        repository.insert(alarma)
    }

    fun delete(alarma: Alarma) = viewModelScope.launch {
        repository.delete(alarma)
    }

    fun loginUsuario(email: String, contrasena: String, context: android.content.Context) {
        viewModelScope.launch {
            try {
                // 0. Limpiamos cualquier error anterior al darle al botón
                _mensajeError.value = null

                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(context)
                val response = apiService.login(email, contrasena)

                val sessionManager = com.cdm.tfg_ringhere.utils.SessionManager(context)
                sessionManager.saveAuthToken(response.access_token)

                _loginExitoso.value = true

            } catch (e: Exception) {
                // ¡Si falla la petición, le damos un valor al mensaje de error!
                Log.e("API_TEST", "Error de login: ${e.message}")
                _mensajeError.value = "Correo o contraseña incorrectos. Inténtalo de nuevo."
            }
        }
    }

    fun guardarNuevaAlarma(
        nombre: String,
        lat: Double,
        lng: Double,
        radio: Float,
        alEntrar: Boolean,
        context: android.content.Context
    ) {
        viewModelScope.launch {
            // 1. Creamos la alarma. Kotlin ya le asigna un UUID
            val nuevaAlarma = Alarma(
                nombre = nombre,
                latitud = lat,
                longitud = lng,
                radio = radio,
                isAlEntrar = alEntrar,
                isActive = true
            )

            // 2. ¡LA GUARDAMOS INMEDIATAMENTE EN EL MÓVIL! (Offline-First 100%)
            repository.insert(nuevaAlarma)

            // 3. EN SEGUNDO PLANO, intentamos enviársela a Python
            try {
                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(context)
                val response = apiService.crearAlarma(nuevaAlarma)

                if (response.isSuccessful) {
                    android.util.Log.d("API_SYNC", "Alarma sincronizada en la nube con ID: ${nuevaAlarma.id}")
                }
            } catch (e: Exception) {
                // Si no hay internet, no pasa nada. La alarma YA está guardada y funcionando en el móvil.
                android.util.Log.e("API_SYNC", "Sin internet. Guardada solo en local: ${e.message}")
            }
        }
    }

    fun eliminarAlarma(alarma: Alarma, context: android.content.Context) {
        viewModelScope.launch {
            // 1. Borramos localmente
            repository.delete(alarma)

            // 2. Borramos en la Nube
            try {
                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(context)
                val response = apiService.borrarAlarma(alarma.id)
                if (response.isSuccessful) {
                    android.util.Log.d("API_SYNC", "Alarma eliminada del servidor")
                }
            } catch (e: Exception) {
                android.util.Log.e("API_SYNC", "Error borrando en servidor: ${e.message}")
            }
        }
    }

    fun actualizarEstadoAlarma(alarma: Alarma, nuevoEstado: Boolean, context: android.content.Context) {
        viewModelScope.launch {
            // 1. Actualizamos localmente
            val alarmaActualizada = alarma.copy(isActive = nuevoEstado)
            repository.insert(alarmaActualizada)

            // 2. Actualizamos en la Nube
            try {
                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(context)
                val response = apiService.actualizarAlarma(alarma.id, alarmaActualizada)
                if (response.isSuccessful) {
                    android.util.Log.d("API_SYNC", "Estado actualizado en el servidor")
                }
            } catch (e: Exception) {
                android.util.Log.e("API_SYNC", "Error actualizando en servidor: ${e.message}")
            }
        }
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
