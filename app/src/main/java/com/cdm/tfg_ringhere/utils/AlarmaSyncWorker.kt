package com.cdm.tfg_ringhere.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cdm.tfg_ringhere.data.local.AppDatabase
import com.cdm.tfg_ringhere.data.repository.AlarmaRepository

class AlarmaSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = AlarmaRepository(database.alarmaDao())

            val sessionManager = SessionManager(applicationContext)
            val email = sessionManager.getUserEmail() ?: ""

            if (email.isNotEmpty()) {
                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient
                    .getApiService(applicationContext)

                val response = apiService.obtenerAlarmas()

                if (response.isSuccessful && response.body() != null) {
                    val alarmasNube = response.body()!!
                    val alarmasDeEsteUsuario = alarmasNube.filter { it.userEmail == email }

                    repository.clearAlarmasByUser(email)
                    repository.insertAlarmas(alarmasDeEsteUsuario)

                    // FIX: reregistrar geofences después de sincronizar.
                    // Al hacer clearAlarmasByUser + insertAlarmas los geofences anteriores
                    // siguen registrados en Google Play Services con IDs que ya no existen
                    // en la BD local, y los nuevos no tienen geofence activo todavía.
                    // Reregistrar aquí garantiza que siempre estén en sincronía.
                    val alarmasActivas = alarmasDeEsteUsuario.filter { it.isActive }
                    if (alarmasActivas.isNotEmpty()) {
                        GeofenceManager(applicationContext).reregistrarTodas(alarmasActivas)
                        Log.d("AlarmaSyncWorker", "🔄 Geofences reregistrados: ${alarmasActivas.size}")
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("AlarmaSyncWorker", "Error en segundo plano: ${e.message}")
            Result.retry()
        }
    }
}