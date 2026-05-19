package com.cdm.tfg_ringhere.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cdm.tfg_ringhere.data.repository.AlarmaRepository
import com.cdm.tfg_ringhere.model.Alarma
import com.cdm.tfg_ringhere.utils.GeofenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmaViewModel(private val repository: AlarmaRepository) : ViewModel() {

    private val _alarmas = MutableStateFlow<List<Alarma>>(emptyList())
    val alarmas: StateFlow<List<Alarma>> = _alarmas.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // --- ESTADOS PARA EL RADAR EN TIEMPO REAL ---
    private val _radarActivo = MutableStateFlow(true)
    val radarActivo: StateFlow<Boolean> = _radarActivo.asStateFlow()

    private val _alarmaCercana = MutableStateFlow<Alarma?>(null)
    val alarmaCercana: StateFlow<Alarma?> = _alarmaCercana.asStateFlow()

    private val _distanciaCercanaMetros = MutableStateFlow<Float?>(null)
    val distanciaCercanaMetros: StateFlow<Float?> = _distanciaCercanaMetros.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var ubicacionActual: Location? = null
    // --------------------------------------------

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

    // --- NUEVO: Motor de Rastreo GPS ---
    @SuppressLint("MissingPermission") // Los permisos ya se piden en la UI
    fun iniciarRastreoUbicacion(context: android.content.Context) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        // Configuramos la petición: Alta precisión, actualiza cada 5 segundos
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateDistanceMeters(5f) // Solo actualiza si el usuario se mueve 5 metros
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { loc ->
                    ubicacionActual = loc
                    calcularAlarmaMasCercana()
                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun detenerRastreoUbicacion() {
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
    }

    // --- NUEVO: Matemática de Distancias ---
    private fun calcularAlarmaMasCercana() {
        val miPosicion = ubicacionActual
        val listaActivas = _alarmas.value.filter { it.isActive }

        // Si el radar está pausado, no hay GPS, o no hay alarmas activas, reseteamos la tarjeta
        if (!_radarActivo.value || miPosicion == null || listaActivas.isEmpty()) {
            _alarmaCercana.value = null
            _distanciaCercanaMetros.value = null
            return
        }

        var alarmaMasCerca: Alarma? = null
        var distanciaMinima = Float.MAX_VALUE

        for (alarma in listaActivas) {
            val locationAlarma = Location("").apply {
                latitude = alarma.latitud
                longitude = alarma.longitud
            }
            // Math magic nativa de Android: Distancia en línea recta en metros
            val distancia = miPosicion.distanceTo(locationAlarma)

            if (distancia < distanciaMinima) {
                distanciaMinima = distancia
                alarmaMasCerca = alarma
            }
        }

        _alarmaCercana.value = alarmaMasCerca
        _distanciaCercanaMetros.value = distanciaMinima
    }
    // --------------------------------------------

    fun alternarEstadoRadar(context: android.content.Context) {
        _radarActivo.value = !_radarActivo.value

        if (_radarActivo.value) {
            iniciarRastreoUbicacion(context)
        } else {
            detenerRastreoUbicacion()
            _alarmaCercana.value = null
            _distanciaCercanaMetros.value = null
        }
        calcularAlarmaMasCercana()
    }

    fun cargarAlarmasDelUsuario(context: android.content.Context) {
        val sessionManager = com.cdm.tfg_ringhere.utils.SessionManager(context)
        val email = sessionManager.getUserEmail() ?: ""

        if (email.isEmpty()) return

        jobAlarmas?.cancel()
        jobAlarmas = viewModelScope.launch {
            repository.getAlarmasByUser(email).collect { misAlarmas ->
                _alarmas.value = misAlarmas
                calcularAlarmaMasCercana() // Recalcula si borramos/añadimos una alarma
            }
        }
    }

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

    fun insert(alarma: Alarma) = viewModelScope.launch { repository.insert(alarma) }
    fun delete(alarma: Alarma) = viewModelScope.launch { repository.delete(alarma) }

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
                sincronizarAlarmas(context)
                _loginExitoso.value = true

            } catch (e: Exception) {
                Log.e("API_TEST", "Error de login: ${e.message}")
                _mensajeError.value = "Correo o contraseña incorrectos."
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
                nombre = nombre, latitud = lat, longitud = lng, radio = radio,
                isAlEntrar = alEntrar, isActive = true, userEmail = emailDueño
            )

            repository.insert(nuevaAlarma)
            getManager(context).anadirAlarmaAlRadar(nuevaAlarma)

            try {
                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(context)
                apiService.crearAlarma(nuevaAlarma)
            } catch (e: Exception) {
                Log.e("API_SYNC", "Sin internet: ${e.message}")
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
                Log.e("API_SYNC", "Error borrando servidor: ${e.message}")
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
                Log.e("API_SYNC", "Error actualizando servidor: ${e.message}")
            }
        }
    }

    fun logout() {
        _loginExitoso.value = false
        _mensajeError.value = null
        _alarmas.value = emptyList()
        detenerRastreoUbicacion()
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