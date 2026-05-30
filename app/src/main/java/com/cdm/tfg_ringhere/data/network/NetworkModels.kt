package com.cdm.tfg_ringhere.data.network

// Lo que recibimos al hacer Login
data class TokenResponse(
    val access_token: String,
    val token_type: String
)

// Lo que recibimos al pedir las Alarmas
data class AlarmaNetwork(
    val id: String,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val radio: Double,
    val is_active: Boolean
)

data class UsuarioCreate(
    val email: String,
    val password: String,
    val full_name: String
)