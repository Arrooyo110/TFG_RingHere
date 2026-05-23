package com.cdm.tfg_ringhere.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cdm.tfg_ringhere.data.local.AppDatabase
import com.cdm.tfg_ringhere.utils.GeofenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// FIX: Android borra TODOS los geofences de Google Play Services cuando el dispositivo
// se reinicia. Sin este receiver, las alarmas dejan de funcionar tras cualquier reinicio
// hasta que el usuario abra la app manualmente.
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d("RADAR_BOOT", "📱 Dispositivo reiniciado — reregistrando geovallas...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context.applicationContext)
                // Recuperamos solo las alarmas activas para reregistrar
                val alarmasActivas = database.alarmaDao().getAlarmasActivas()

                if (alarmasActivas.isNotEmpty()) {
                    GeofenceManager(context.applicationContext).reregistrarTodas(alarmasActivas)
                    Log.d("RADAR_BOOT", "✅ Reregistradas ${alarmasActivas.size} geovallas tras reinicio")
                } else {
                    Log.d("RADAR_BOOT", "ℹ️ Sin alarmas activas para reregistrar")
                }
            } catch (e: Exception) {
                Log.e("RADAR_BOOT", "❌ Error al reregistrar tras reinicio: ${e.message}")
            }
        }
    }
}