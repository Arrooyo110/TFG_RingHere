package com.cdm.tfg_ringhere.data.network

// Lo que recibimos al hacer Login
data class TokenResponse(
    val access_token: String,
    val token_type: String
)

// Lo que recibimos al pedir las Alarmas (Fíjate que es igual al schema de Python)
data class AlarmaNetwork(
    val id: Int,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val radio: Double,
    val is_active: Boolean
)