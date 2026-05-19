package com.cdm.tfg_ringhere.data.network

import android.content.Context
import com.cdm.tfg_ringhere.utils.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    //private const val BASE_URL = "http://192.168.1.24:8000/"  //-- Movil android
    //private const val BASE_URL = "http://10.0.2.2:8000/"  //-- Emulator Android
    private const val BASE_URL = "https://ringhere-api.onrender.com/"

    fun getApiService(context: Context): GeoAlarmasApi {

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val sessionManager = SessionManager(context)
                val token = sessionManager.fetchAuthToken()

                val requestBuilder = chain.request().newBuilder()

                if (token != null) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeoAlarmasApi::class.java)
    }
}