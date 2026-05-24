package com.cdm.tfg_ringhere.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "eventos_alarma")
data class EventoAlarma(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val alarmaId: String,
    val nombreAlarma: String,
    val tipoTransicion: String,   // "ENTRAR" o "SALIR"
    val latitudDetectada: Double, // ubicación exacta donde el sistema disparó la alarma
    val longitudDetectada: Double,
    val timestamp: Long = System.currentTimeMillis()
)