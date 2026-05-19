package com.cdm.tfg_ringhere.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cdm.tfg_ringhere.model.Alarma
import com.cdm.tfg_ringhere.receiver.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(context: Context) {

    // Nos conectamos a los Servicios de Localización de Google
    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val applicationContext = context.applicationContext

    // Preparamos el "sobre" que el GPS le entregará a nuestro Receptor cuando crucemos la línea
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(applicationContext, GeofenceBroadcastReceiver::class.java)
        // Usamos FLAG_MUTABLE para que sea compatible con las últimas versiones de Android
        PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission") // Ocultamos el aviso porque ya pedimos los permisos en el Dashboard
    fun anadirAlarmaAlRadar(alarma: Alarma) {
        // 1. Configuramos si el radar debe pitar al ENTRAR o al SALIR
        val tipoTransicion = if (alarma.isAlEntrar) {
            Geofence.GEOFENCE_TRANSITION_ENTER
        } else {
            Geofence.GEOFENCE_TRANSITION_EXIT
        }

        // 2. Creamos la "Geovalla" matemática
        val geofence = Geofence.Builder()
            .setRequestId("${alarma.id}|${alarma.nombre}") // Usamos el UUID de nuestra base de datos para no perderla
            .setCircularRegion(alarma.latitud, alarma.longitud, alarma.radio)
            .setExpirationDuration(Geofence.NEVER_EXPIRE) // Estará ahí hasta que la borremos o apaguemos
            .setTransitionTypes(tipoTransicion)
            .build()

        // 3. Empaquetamos la petición
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofence(geofence)
            .build()

        // 4. Se la enviamos al satélite/sistema de Google
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d("RADAR_MANAGER", "📍 Geovalla [${alarma.nombre}] activada en el GPS.")
            }
            addOnFailureListener {
                Log.e("RADAR_MANAGER", "❌ Error al activar en GPS: ${it.message}")
            }
        }
    }

    fun quitarAlarmaDelRadar(alarmaId: String) {
        // Le pasamos la ID (UUID) de la alarma que queremos que Google deje de vigilar
        geofencingClient.removeGeofences(listOf(alarmaId)).run {
            addOnSuccessListener {
                Log.d("RADAR_MANAGER", "🛑 Geovalla [$alarmaId] borrada del GPS.")
            }
            addOnFailureListener {
                Log.e("RADAR_MANAGER", "❌ Error al borrar en GPS: ${it.message}")
            }
        }
    }
}