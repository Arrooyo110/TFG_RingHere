package com.cdm.tfg_ringhere.data.local.dao

import androidx.room.*
import com.cdm.tfg_ringhere.model.Alarma
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarma(alarma: Alarma)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmas(alarmas: List<Alarma>)

    @Delete
    suspend fun deleteAlarma(alarma: Alarma)

    @Query("DELETE FROM alarmas WHERE userEmail = :email")
    suspend fun clearAlarmasByUser(email: String)

    // --- Filtrado por usuario ---
    @Query("SELECT * FROM alarmas WHERE userEmail = :email ORDER BY fechaCreacion DESC")
    fun getAlarmasByUser(email: String): Flow<List<Alarma>>

    // --- Usado por BootReceiver y AlarmaSyncWorker para reregistrar geofences ---
    @Query("SELECT * FROM alarmas WHERE isActive = 1")
    suspend fun getAlarmasActivas(): List<Alarma>
}