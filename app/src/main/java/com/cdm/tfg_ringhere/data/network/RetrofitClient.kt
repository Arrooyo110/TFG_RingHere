package com.cdm.tfg_ringhere.data.network

import android.content.Context
import com.cdm.tfg_ringhere.utils.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Necesitamos pasarle el contexto para que pueda leer la memoria del móvil
    fun getApiService(context: Context): GeoAlarmasApi {

        // Creamos el interceptor que "secuestra" la petición antes de salir y le pega el Token
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val sessionManager = SessionManager(context)
            val token = sessionManager.fetchAuthToken()

            val requestBuilder = chain.request().newBuilder()

            // Si tenemos un token guardado, se lo pegamos en la cabecera
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }.build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Le conectamos nuestro motor modificado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeoAlarmasApi::class.java)
    }
}