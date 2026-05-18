package com.cdm.tfg_ringhere.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cdm.tfg_ringhere.data.repository.AlarmaRepository
import com.cdm.tfg_ringhere.model.Alarma
import com.cdm.tfg_ringhere.utils.GeofenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmaViewModel(private val repository: AlarmaRepository) : ViewModel() {

    private val _alarmas = MutableStateFlow<List<Alarma>>(emptyList())
    val alarmas: StateFlow<List<Alarma>> = _alarmas.asStateFlow()

    // --- NUEVO: Control del indicador visual de arrastrar para actualizar (Pull-to-refresh) ---
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var jobAlarmas: kotlinx.coroutines.Job? = null

    private val _loginExitoso = MutableStateFlow(false)
    val loginExitoso = _loginExitoso.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError = _mensajeError.asStateFlow()

    private var geofenceManager: GeofenceManager? = null

    private fun getManager(context: android.content.Context): GeofenceManager {
        if (geofenceManager == null) {
            geofenceManager = GeofenceManager(context)
        }
        return geofenceManager!!
    }

    fun cargarAlarmasDelUsuario(context: android.content.Context) {
        val sessionManager = com.cdm.tfg_ringhere.utils.SessionManager(context)
        val email = sessionManager.getUserEmail() ?: ""

        if (email.isEmpty()) return

        jobAlarmas?.cancel()
        jobAlarmas = viewModelScope.launch {
            repository.getAlarmasByUser(email).collect { misAlarmas ->
                _alarmas.value = misAlarmas
            }
        }
    }

    // --- NUEVO: Sincronización Remota con FastAPI de Render ---
    fun sincronizarAlarmas(context: android.content.Context) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val sessionManager = com.cdm.tfg_ringhere.utils.SessionManager(context)
                val email = sessionManager.getUserEmail() ?: ""

                if (email.isNotEmpty()) {
                    val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(context)
                    val response = apiService.obtenerAlarmas()

                    if (response.isSuccessful && response.body() != null) {
                        val alarmasNube = response.body()!!

                        // Sincronizamos Room eliminando lo viejo de este dueño e insertando la lista limpia
                        repository.clearAlarmasByUser(email)
                        repository.insertAlarmas(alarmasNube.filter { it.userEmail == email })
                    }
                }
            } catch (e: Exception) {
                Log.e("API_SYNC", "Error sincronizando datos con Render: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun insert(alarma: Alarma) = viewModelScope.launch {
        repository.insert(alarma)
    }

    fun delete(alarma: Alarma) = viewModelScope.launch {
        repository.delete(alarma)
    }

    fun loginUsuario(email: String, contrasena: String, context: android.content.Context) {
        viewModelScope.launch {
            try {
                _mensajeError.value = null

                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(context)
                val response = apiService.login(email, contrasena)

                val sessionManager = com.cdm.tfg_ringhere.utils.SessionManager(context)
                sessionManager.saveAuthToken(response.access_token)
                sessionManager.saveUserEmail(email)

                cargarAlarmasDelUsuario(context)
                sincronizarAlarmas(context) // Bajamos sus alarmas inmediatamente al entrar

                _loginExitoso.value = true

            } catch (e: Exception) {
                Log.e("API_TEST", "Error de login: ${e.message}")
                _mensajeError.value = "Correo o contraseña incorrectos. Inténtalo de nuevo."
            }
        }
    }

    fun guardarNuevaAlarma(
        nombre: String, lat: Double, lng: Double, radio: Float, alEntrar: Boolean, context: android.content.Context
    ) {
        viewModelScope.launch {
            val sessionManager = com.cdm.tfg_ringhere.utils.SessionManager(context)
            val emailDueño = sessionManager.getUserEmail() ?: ""

            val nuevaAlarma = Alarma(
                nombre = nombre,
                latitud = lat,
                longitud = lng,
                radio = radio,
                isAlEntrar = alEntrar,
                isActive = true,
                userEmail = emailDueño
            )

            repository.insert(nuevaAlarma)
            getManager(context).anadirAlarmaAlRadar(nuevaAlarma)

            try {
                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(context)
                val response = apiService.crearAlarma(nuevaAlarma)

                if (response.isSuccessful) {
                    Log.d("API_SYNC", "Alarma sincronizada en la nube con ID: ${nuevaAlarma.id}")
                }
            } catch (e: Exception) {
                Log.e("API_SYNC", "Sin internet. Guardada solo localmente: ${e.message}")
            }
        }
    }

    fun eliminarAlarma(alarma: Alarma, context: android.content.Context) {
        viewModelScope.launch {
            repository.delete(alarma)
            getManager(context).quitarAlarmaDelRadar(alarma.id)
            try {
                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(context)
                apiService.borrarAlarma(alarma.id)
            } catch (e: Exception) {
                Log.e("API_SYNC", "Error borrando en servidor: ${e.message}")
            }
        }
    }

    fun actualizarEstadoAlarma(alarma: Alarma, nuevoEstado: Boolean, context: android.content.Context) {
        viewModelScope.launch {
            val alarmaActualizada = alarma.copy(isActive = nuevoEstado)
            repository.insert(alarmaActualizada)

            if (nuevoEstado) {
                getManager(context).anadirAlarmaAlRadar(alarmaActualizada)
            } else {
                getManager(context).quitarAlarmaDelRadar(alarma.id)
            }

            try {
                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(context)
                apiService.actualizarAlarma(alarma.id, alarmaActualizada)
            } catch (e: Exception) {
                Log.e("API_SYNC", "Error actualizando en servidor: ${e.message}")
            }
        }
    }

    fun logout() {
        _loginExitoso.value = false
        _mensajeError.value = null
        _alarmas.value = emptyList()
    }
}

class AlarmaViewModelFactory(private val repository: AlarmaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmaViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}