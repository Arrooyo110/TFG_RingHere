package com.cdm.tfg_ringhere.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName = "alarmas")
data class Alarma(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val radio: Float,
    @SerializedName("is_al_entrar")
    val isAlEntrar: Boolean,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("fecha_creacion")
    val fechaCreacion: Long = System.currentTimeMillis(),
    @SerializedName("user_email")
    val userEmail: String = ""
)