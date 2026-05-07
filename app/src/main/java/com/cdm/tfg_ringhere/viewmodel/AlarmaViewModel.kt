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
