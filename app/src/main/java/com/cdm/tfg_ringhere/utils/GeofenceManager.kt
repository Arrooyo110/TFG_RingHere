package com.cdm.tfg_ringhere.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cdm.tfg_ringhere.model.Alarma
import com.cdm.tfg_ringhere.receiver.GeofenceBroadcastReceiver
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class GeofenceManager(context: Context) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val applicationContext = context.applicationContext

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(applicationContext, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    // Helper compartido: construye siempre el mismo requestId
    // IMPORTANTE: quitarAlarmaDelRadar usa esta misma función para que coincidan
    fun buildRequestId(alarma: Alarma) = "${alarma.id}|${alarma.nombre}"

    @SuppressLint("MissingPermission")
    fun anadirAlarmaAlRadar(alarma: Alarma) {
        // Para "al entrar" usamos DWELL en vez de ENTER:
        // ENTER dispara en cuanto el GPS dice que cruzaste, incluso si es
        // un bounce puntual o vas en coche a alta velocidad.
        // DWELL espera loiteringDelay ms dentro del área antes de confirmar,
        // filtrando falsas entradas y pasos rápidos sin intención de quedarse.
        // Para "al salir" mantenemos EXIT (no hay equivalente de dwell para salida).
        val tipoTransicion = if (alarma.isAlEntrar) {
            Geofence.GEOFENCE_TRANSITION_DWELL
        } else {
            Geofence.GEOFENCE_TRANSITION_EXIT
        }

        // Radio mínimo 100m — por debajo Android es muy poco fiable con geofencing
        // Radio mínimo 200m: con 100m y velocidad de coche (~50km/h) el sistema
        // de geofencing puede no muestrear a tiempo y perder la transición
        val radioEfectivo = alarma.radio.coerceAtLeast(200f)
        if (alarma.radio < 200f) {
            Log.w("RADAR_MANAGER", "⚠️ Radio ${alarma.radio}m aumentado a 200m por fiabilidad")
        }

        val geofence = Geofence.Builder()
            .setRequestId(buildRequestId(alarma))
            .setCircularRegion(alarma.latitud, alarma.longitud, radioEfectivo)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(tipoTransicion)
            // FIX: por defecto Android espera ~5 min para notificar, con 1000ms es casi inmediato
            .setNotificationResponsiveness(1_000)
            // loiteringDelay: tiempo que debes estar dentro antes de confirmar DWELL.
            // Solo aplica a alarmas "al entrar". 15s es suficiente para filtrar
            // pasos rápidos en coche pero no demasiado para uso peatonal.
            .setLoiteringDelay(15_000)
            .build()

        // FIX: setInitialTrigger(0) es correcto para el comportamiento deseado:
        // - Alarma "al entrar" creada estando dentro → no dispara hasta salir y volver a entrar
        // - Alarma "al salir" creada estando fuera → no dispara hasta entrar y luego salir
        // Sin embargo, dejarlo en 0 hace que Android no inicialice el estado del geofence
        // hasta recibir una muestra de ubicación pasiva (puede tardar mucho en Doze Mode).
        // Por eso llamamos a forzarActualizacionUbicacion() después de registrar.
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnSuccessListener {
                Log.d("RADAR_MANAGER", "📍 Geovalla [${alarma.nombre}] activada. Radio: ${radioEfectivo}m")
                // REVERTIDO: forzarActualizacionUbicacion() causaba falsos positivos
                // en interior porque el GPS con poca precisión colocaba al usuario
                // fuera del área, disparando alarmas "al salir" sin haberse movido.
                // Android inicializa el geofence con la siguiente muestra pasiva,
                // que con notificationResponsiveness(1_000) llega en pocos segundos.
            }
            .addOnFailureListener {
                Log.e("RADAR_MANAGER", "❌ Error al activar [${alarma.nombre}]: ${it.message}")
            }
    }

    // FIX: ahora recibe la Alarma completa para poder construir el requestId correcto
    // Antes recibía solo alarmaId (String) y no coincidía con el "id|nombre" registrado
    fun quitarAlarmaDelRadar(alarma: Alarma) {
        geofencingClient.removeGeofences(listOf(buildRequestId(alarma)))
            .addOnSuccessListener {
                Log.d("RADAR_MANAGER", "🛑 Geovalla [${alarma.nombre}] borrada del GPS.")
            }
            .addOnFailureListener {
                Log.e("RADAR_MANAGER", "❌ Error al borrar [${alarma.nombre}]: ${it.message}")
            }
    }

    // Reregistra una lista completa de alarmas — usado por BootReceiver y AlarmaSyncWorker
    @SuppressLint("MissingPermission")
    fun reregistrarTodas(alarmas: List<Alarma>) {
        if (alarmas.isEmpty()) {
            Log.d("RADAR_MANAGER", "🔄 Sin alarmas activas para reregistrar")
            return
        }
        alarmas.forEach { anadirAlarmaAlRadar(it) }
        Log.d("RADAR_MANAGER", "🔄 Reregistradas ${alarmas.size} geovallas")
    }

    // Pide UNA lectura de ubicación de alta precisión tras registrar un geofence.
    // Esto "despierta" el sistema de geofencing y le da contexto de posición inmediato,
    // evitando que el geofence quede en estado "limbo" hasta que Doze Mode lo permita.
    @SuppressLint("MissingPermission")
    private fun forzarActualizacionUbicacion() {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)   // No usar caché, leer ahora mismo
            .setDurationMillis(10_000)  // Esperar máximo 10s para obtenerla
            .build()

        fusedClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("RADAR_MANAGER", "📡 Ubicación forzada OK: ${location.latitude}, ${location.longitude}")
                } else {
                    Log.w("RADAR_MANAGER", "⚠️ No se obtuvo ubicación para inicializar geofence")
                }
            }
            .addOnFailureListener {
                Log.w("RADAR_MANAGER", "⚠️ Error al forzar ubicación: ${it.message}")
            }
    }
}