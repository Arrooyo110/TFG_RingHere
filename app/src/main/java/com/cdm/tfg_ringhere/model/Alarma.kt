package com.cdm.tfg_ringhere.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarmas")
data class Alarma(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val radio: Float, // Radio de la geovalla en metros
    val isAlEntrar: Boolean, // true = Al entrar, false = Al salir
    val isActive: Boolean = true
)