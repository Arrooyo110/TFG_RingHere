package com.cdm.tfg_ringhere.data.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface GeoAlarmasApi {
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") contrasena: String
    ): TokenResponse

    // ¡Mira qué limpio queda ahora! Ya no hace falta pedir el Header aquí
    @GET("alarmas/")
    suspend fun getAlarmas(): List<AlarmaNetwork>
}