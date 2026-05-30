package com.cdm.tfg_ringhere.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cdm.tfg_ringhere.data.local.AppDatabase
import com.cdm.tfg_ringhere.data.repository.AlarmaRepository
import kotlinx.coroutines.flow.first

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

                    val alarmasLocales = repository.getAlarmasByUser(email).first()

                    val idsNube = alarmasDeEsteUsuario.map { it.id }
                    val alarmasPendientes = alarmasLocales.filter { it.id !in idsNube }

                    alarmasPendientes.forEach { alarmaPendiente ->
                        try {
                            apiService.crearAlarma(alarmaPendiente)
                            Log.d("AlarmaSyncWorker", "Alarma offline subida: ${alarmaPendiente.nombre}")
                        } catch (e: Exception) {
                            Log.e("AlarmaSyncWorker", "Error subiendo alarma offline: ${e.message}")
                        }
                    }

                    val responseFinal = apiService.obtenerAlarmas()
                    val listaDefinitiva = if (responseFinal.isSuccessful && responseFinal.body() != null) {
                        responseFinal.body()!!.filter { it.userEmail == email }
                    } else {
                        alarmasDeEsteUsuario
                    }

                    repository.clearAlarmasByUser(email)
                    repository.insertAlarmas(listaDefinitiva)

                    val alarmasActivas = listaDefinitiva.filter { it.isActive }
                    if (alarmasActivas.isNotEmpty()) {
                        GeofenceManager(applicationContext).reregistrarTodas(alarmasActivas)
                        Log.d("AlarmaSyncWorker", "Geofences reregistrados: ${alarmasActivas.size}")
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