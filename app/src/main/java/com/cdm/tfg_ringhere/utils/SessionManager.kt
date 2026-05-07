package com.cdm.tfg_ringhere.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    // Creamos un archivo privado en el móvil solo para nuestra app
    private var prefs: SharedPreferences = context.getSharedPreferences("MiAppTFG", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
    }

    // Función para GUARDAR el token en la memoria del móvil
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    // Función para RECUPERAR el token cuando lo necesitemos
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    // Función para CERRAR SESIÓN (borrar el token)
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}