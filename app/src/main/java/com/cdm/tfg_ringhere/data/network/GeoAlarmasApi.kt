package com.cdm.tfg_ringhere.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path
import com.cdm.tfg_ringhere.model.Alarma

interface GeoAlarmasApi {

    // --- Login y registro ---
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") contrasena: String
    ): TokenResponse

    @POST("register")
    suspend fun register(@Body usuario: UsuarioCreate): Response<Void>

    // --- Alarmas ---
    @GET("alarmas/")
    suspend fun getAlarmas(): List<AlarmaNetwork>

    @POST("alarmas/")
    suspend fun crearAlarma(@Body alarma: Alarma): Response<Void>

    @DELETE("alarmas/{id}")
    suspend fun borrarAlarma(@Path("id") id: String): Response<Void>

    @PUT("alarmas/{id}")
    suspend fun actualizarAlarma(@Path("id") id: String, @Body alarma: Alarma): Response<Void>

    @GET("alarmas/")
    suspend fun obtenerAlarmas(): retrofit2.Response<List<Alarma>>
}