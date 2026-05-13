package com.cdm.tfg_ringhere.data.local.dao

import androidx.room.*
import com.cdm.tfg_ringhere.model.Alarma
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarma(alarma: Alarma)

    @Delete
    suspend fun deleteAlarma(alarma: Alarma)

    @Query("SELECT * FROM alarmas ORDER BY fechaCreacion DESC")
    fun getAllAlarmas(): Flow<List<Alarma>>
}