package com.cdm.tfg_ringhere.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cdm.tfg_ringhere.data.local.AppDatabase
import com.cdm.tfg_ringhere.model.EventoAlarma
import com.cdm.tfg_ringhere.ui.alarm.AlarmaActivaActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "geoalarmas_silencioso_v1"
        const val WAKELOCK_TAG = "RingHere::GeofenceWakeLock"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Adquirir WakeLock inmediatamente para evitar que Doze Mode suspenda el proceso
        // antes de lanzar la notificación, especialmente en dispositivos con ahorro agresivo.
        val wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wakeLock.acquire(10_000L)

        try {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent == null || geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent?.errorCode ?: -1)
                Log.e("RADAR", "Error en el Geofence: $errorMessage")
                return
            }

            val geofenceTransition = geofencingEvent.geofenceTransition

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
            ) {
                geofencingEvent.triggeringGeofences?.forEach { geofence ->
                    val partes = geofence.requestId.split("|")
                    val nombreAlarma = if (partes.size > 1) partes[1] else "Alarma"

                    // Si es EXIT es salir, cualquier otra (ENTER o DWELL) es entrar
                    val accion = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
                        "Saliendo de" else "Entrando a"

                    Log.d("RADAR", "Transición detectada: $accion $nombreAlarma")

                    val ubicacion = geofencingEvent.triggeringLocation
                    guardarEventoEnHistorial(
                        context = context,
                        alarmaId = partes[0],
                        nombreAlarma = nombreAlarma,
                        tipoTransicion = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) "SALIR" else "ENTRAR",
                        lat = ubicacion?.latitude ?: 0.0,
                        lng = ubicacion?.longitude ?: 0.0
                    )

                    lanzarAlarmaPantallaCompleta(context, nombreAlarma, accion)
                }
            } else {
                Log.e("RADAR", "Transición inválida: $geofenceTransition")
            }

        } finally {
            // Liberar siempre en finally para no dejar el WakeLock activo
            if (wakeLock.isHeld) wakeLock.release()
        }
    }

    private fun guardarEventoEnHistorial(
        context: Context,
        alarmaId: String,
        nombreAlarma: String,
        tipoTransicion: String,
        lat: Double,
        lng: Double
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val evento = EventoAlarma(
                    alarmaId = alarmaId,
                    nombreAlarma = nombreAlarma,
                    tipoTransicion = tipoTransicion,
                    latitudDetectada = lat,
                    longitudDetectada = lng
                )
                AppDatabase.getDatabase(context).eventoAlarmaDao().insertEvento(evento)
                Log.d("RADAR", "Evento guardado: $nombreAlarma ($tipoTransicion) @ $lat,$lng")
            } catch (e: Exception) {
                Log.e("RADAR", "Error guardando historial: ${e.message}")
            }
        }
    }

    private fun lanzarAlarmaPantallaCompleta(context: Context, nombreAlarma: String, accion: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarmas a Pantalla Completa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lanza la pantalla de alarma activa"
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = nombreAlarma.hashCode()

        val fullScreenIntent = Intent(context, AlarmaActivaActivity::class.java).apply {
            putExtra("NOMBRE_ALARMA", nombreAlarma)
            putExtra("NOTIFICACION_ID", notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("¡Geoalarma Alcanzada!")
            .setContentText("$accion la zona: $nombreAlarma")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(null)
            .setVibrate(null)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}