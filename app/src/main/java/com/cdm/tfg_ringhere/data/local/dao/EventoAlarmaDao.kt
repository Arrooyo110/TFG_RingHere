package com.cdm.tfg_ringhere.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cdm.tfg_ringhere.model.EventoAlarma
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoAlarmaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvento(evento: EventoAlarma)

    // Todos los eventos ordenados del más reciente al más antiguo
    @Query("SELECT * FROM eventos_alarma ORDER BY timestamp DESC")
    fun getTodosLosEventos(): Flow<List<EventoAlarma>>

    // Limpia eventos con más de 30 días para no acumular indefinidamente
    @Query("DELETE FROM eventos_alarma WHERE timestamp < :hace30Dias")
    suspend fun limpiarEventosAntiguos(hace30Dias: Long)

    @Query("DELETE FROM eventos_alarma")
    suspend fun limpiarTodo()
}