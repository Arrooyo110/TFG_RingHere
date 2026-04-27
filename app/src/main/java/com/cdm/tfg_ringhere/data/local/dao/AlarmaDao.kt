package com.cdm.tfg_ringhere.data.local.dao

import androidx.room.*
import com.cdm.tfg_ringhere.model.Alarma
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmaDao {
// Crea las funciones para insertar una alarma, borrarla y obtener todas usando Flow
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarma(alarma: Alarma)

    @Delete
    suspend fun deleteAlarma(alarma: Alarma)

    @Query("SELECT * FROM alarmas")
    fun getAllAlarmas(): Flow<List<Alarma>>
}