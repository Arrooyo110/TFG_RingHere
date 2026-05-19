package com.cdm.tfg_ringhere.receiver

// =====================================================================
// 1. IMPORTACIONES
// =====================================================================
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cdm.tfg_ringhere.R
import com.cdm.tfg_ringhere.ui.alarm.AlarmaActivaActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlin.random.Random

// =====================================================================
// 2. RECEPTOR DE EVENTOS DE GEOFENCING
// =====================================================================
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        // Renombrado para evitar conflictos con la caché de canales antiguos de Android
        const val CHANNEL_ID = "geoalarmas_fullscreen_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // --- VALIDACIÓN DE ERRORES ---
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent?.errorCode ?: -1)
            Log.e("RADAR", "Error en el Geofence: $errorMessage")
            return
        }

        // --- EXTRACCIÓN DE DATOS DE TRANSICIÓN ---
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            val triggeringGeofences = geofencingEvent.triggeringGeofences

            triggeringGeofences?.forEach { geofence ->
                // El requestId contiene el ID o nombre que asignaste al crear la geovalla
                val idAlarma = geofence.requestId
                val accion = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) "Entrando a" else "Saliendo de"

                Log.d("RADAR", "¡BEEP BEEP! Alarma Activada: $idAlarma - Acción: $accion")

                // --- DISPARAR LA MAGIA EN PANTALLA COMPLETA ---
                lanzarAlarmaPantallaCompleta(context, idAlarma, accion)
            }
        } else {
            Log.e("RADAR", "Transición inválida: $geofenceTransition")
        }
    }

    // =====================================================================
// 3. GENERADOR DEL FULL-SCREEN INTENT
// =====================================================================
    private fun lanzarAlarmaPantallaCompleta(context: Context, nombreAlarma: String, accion: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Crear el canal de alta prioridad (Obligatorio en Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarmas Críticas a Pantalla Completa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal diseñado para despertar el dispositivo al cruzar geovallas"
                enableVibration(true)
                // Nota: No inyectamos sonido aquí porque la 'AlarmaActivaActivity'
                // ya utiliza MediaPlayer nativo para sonar en bucle por encima del bloqueo.
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Crear el Intent hacia nuestra Actividad especial de pantalla roja
        val fullScreenIntent = Intent(context, AlarmaActivaActivity::class.java).apply {
            putExtra("NOMBRE_ALARMA", nombreAlarma)
            // Estas flags aseguran que la actividad se lance fresca y pase por encima del resto
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 3. Envolverlo en un PendingIntent para dárselo al gestor de notificaciones
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            Random.nextInt(), // ID aleatorio para evitar que varias alarmas colisionen
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Construir la notificación armando el Full-Screen Intent
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("¡Geoalarma Alcanzada!")
            .setContentText("$accion la zona: $nombreAlarma")
            .setPriority(NotificationCompat.PRIORITY_MAX) // MÁXIMA PRIORIDAD
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Evita las restricciones de "No Molestar"
            .setFullScreenIntent(fullScreenPendingIntent, true) // 🚀 AQUÍ SE DESPIERTA EL MÓVIL
            .setAutoCancel(true)
            .build()

        // 5. Disparar
        notificationManager.notify(Random.nextInt(), notification)
    }
}