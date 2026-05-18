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
            // Inicializamos la base de datos y el repositorio localmente desde el contexto de la aplicación
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = AlarmaRepository(database.alarmaDao())

            val sessionManager = SessionManager(applicationContext)
            val email = sessionManager.getUserEmail() ?: ""

            if (email.isNotEmpty()) {
                val apiService = com.cdm.tfg_ringhere.data.network.RetrofitClient.getApiService(applicationContext)

                // Realiza la llamada GET a Render
                val response = apiService.obtenerAlarmas()

                if (response.isSuccessful && response.body() != null) {
                    val alarmasNube = response.body()!!

                    // Limpiamos lo viejo de este usuario e insertamos el listado fresco
                    repository.clearAlarmasByUser(email)
                    repository.insertAlarmas(alarmasNube.filter { it.userEmail == email })
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("AlarmaSyncWorker", "Error en segundo plano: ${e.message}")
            Result.retry()
        }
    }
}