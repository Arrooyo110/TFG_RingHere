package com.cdm.tfg_ringhere.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE // <-- NUEVO
import retrofit2.http.PUT    // <-- NUEVO
import retrofit2.http.Path   // <-- NUEVO
import com.cdm.tfg_ringhere.model.Alarma

interface GeoAlarmasApi {

    //=========== LOGIN ===========
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") contrasena: String
    ): TokenResponse

    @POST("register")
    suspend fun register(@Body usuario: UsuarioCreate): Response<Void>
    //=========== ALARMAS ===========

    @GET("alarmas/")
    suspend fun getAlarmas(): List<AlarmaNetwork>

    @POST("alarmas/")
    suspend fun crearAlarma(@Body alarma: Alarma): Response<Void>

    @DELETE("alarmas/{id}")
    suspend fun borrarAlarma(@Path("id") id: String): Response<Void>

    @PUT("alarmas/{id}")
    suspend fun actualizarAlarma(@Path("id") id: String, @Body alarma: Alarma): Response<Void>
}