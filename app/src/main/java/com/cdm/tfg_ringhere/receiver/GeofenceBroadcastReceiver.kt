package com.cdm.tfg_ringhere.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cdm.tfg_ringhere.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlin.random.Random

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "geoalarmas_channel_v2"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // 1. Comprobamos si hay algún error
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent?.errorCode ?: -1)
            Log.e("RADAR", "Error en el Geofence: $errorMessage")
            return
        }

        // 2. Comprobamos la transición
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            val triggeringGeofences = geofencingEvent.triggeringGeofences

            triggeringGeofences?.forEach { geofence ->
                val idAlarma = geofence.requestId
                val tipo = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) "Entrando a" else "Saliendo de"

                Log.d("RADAR", "¡BEEP BEEP! Alarma Activada: $idAlarma - Tipo: $tipo")

                // 3. LOGRAMOS EL IMPACTO FÍSICO: Lanzamos la notificación
                lanzarNotificacion(context, "¡Geoalarma Activada!", "$tipo la zona designada.")
            }
        } else {
            Log.e("RADAR", "Transición inválida: $geofenceTransition")
        }
    }

    private fun lanzarNotificacion(context: Context, titulo: String, mensaje: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Referencia a tu archivo de sonido en la carpeta raw
        val sonidoUri = android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.alarma_sonido}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alertas Críticas de Ring Here",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para las alertas sonoras de ubicación"
                enableVibration(true)
                // IMPORTANTE: El sonido se asigna al CANAL en versiones modernas
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM) // Lo marca como ALARMA
                    .build()
                setSound(sonidoUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(sonidoUri) // Para versiones antiguas
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Ayuda a saltarse modos "No molestar"
            .setAutoCancel(true)
            .build()

        notificationManager.notify(kotlin.random.Random.nextInt(), notification)
    }
}