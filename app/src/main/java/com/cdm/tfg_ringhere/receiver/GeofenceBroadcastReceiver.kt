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
import com.cdm.tfg_ringhere.ui.alarm.AlarmaActivaActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

// =====================================================================
// 2. RECEPTOR DE EVENTOS DE GEOFENCING
// =====================================================================
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        // Renombramos el canal para que Android olvide la configuración ruidosa anterior
        const val CHANNEL_ID = "geoalarmas_silencioso_v1"
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
                // EXTRAEMOS EL NOMBRE REAL SEPARANDO EL STRING
                val partes = geofence.requestId.split("|")
                val idAlarma = partes[0]
                val nombreAlarma = if (partes.size > 1) partes[1] else "Alarma"

                val accion = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) "Entrando a" else "Saliendo de"

                Log.d("RADAR", "¡BEEP BEEP! Alarma Activada: $nombreAlarma - Acción: $accion")

                // Pasamos el nombre real a la pantalla completa
                lanzarAlarmaPantallaCompleta(context, nombreAlarma, accion)
            }
        } else {
            Log.e("RADAR", "Transición inválida: $geofenceTransition")
        }
    }

    // =====================================================================
// 3. GENERADOR DEL FULL-SCREEN INTENT (SILENCIOSO)
// =====================================================================
    private fun lanzarAlarmaPantallaCompleta(context: Context, nombreAlarma: String, accion: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Crear el canal de alta prioridad (Obligatorio en Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarmas a Pantalla Completa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lanza la pantalla roja silenciosamente"
                enableVibration(false) // La vibración ya la hace la pantalla roja
                setSound(null, null)   // 🤫 SILENCIAMOS LA NOTIFICACIÓN NATIVA
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 🚀 NUEVO: Creamos un ID predecible (basado en el nombre de la alarma)
        val notificationId = nombreAlarma.hashCode()

        // 2. Crear el Intent hacia nuestra Actividad especial de pantalla roja
        val fullScreenIntent = Intent(context, AlarmaActivaActivity::class.java).apply {
            putExtra("NOMBRE_ALARMA", nombreAlarma)
            putExtra("NOTIFICACION_ID", notificationId) // 🚀 LE PASAMOS EL ID A LA PANTALLA ROJA
            // Estas flags aseguran que la actividad se lance fresca y pase por encima del resto
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 3. Envolverlo en un PendingIntent para dárselo al gestor de notificaciones
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            notificationId, // 🚀 Usamos el ID específico en lugar de un número aleatorio
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Construir la notificación armando el Full-Screen Intent
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_map) // TODO: Cambiar por R.drawable.tu_icono
            .setContentTitle("¡Geoalarma Alcanzada!")
            .setContentText("$accion la zona: $nombreAlarma")
            .setPriority(NotificationCompat.PRIORITY_MAX) // MÁXIMA PRIORIDAD
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Evita las restricciones de "No Molestar"
            .setSound(null) // 🤫 SILENCIAMOS LA NOTIFICACIÓN NATIVA
            .setVibrate(null) // Quitamos vibración de la notificación
            .setContentIntent(fullScreenPendingIntent) // 🚀 Para que al tocarla abra la app
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        // 5. Disparar usando nuestro ID específico
        notificationManager.notify(notificationId, notification) // 🚀 Usamos el ID en lugar de un número aleatorio
    }
}