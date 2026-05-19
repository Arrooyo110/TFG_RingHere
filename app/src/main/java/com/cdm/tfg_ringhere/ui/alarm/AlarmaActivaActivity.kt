package com.cdm.tfg_ringhere.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cdm.tfg_ringhere.ui.theme.TFG_RingHereTheme

class AlarmaActivaActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. MAGIA DE ANDROID: Encender pantalla y saltar bloqueo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 2. Recoger el nombre de la alarma que ha saltado
        val nombreAlarma = intent.getStringExtra("NOMBRE_ALARMA") ?: "Alarma Desconocida"

        // 3. Iniciar Sonido y Vibración
        iniciarSonidoYVibracion()

        setContent {
            TFG_RingHereTheme {
                AlarmaScreenUI(nombreAlarma = nombreAlarma) {
                    apagarAlarmaYSalir()
                }
            }
        }
    }

    private fun iniciarSonidoYVibracion() {
        // Reproduce el sonido de alarma por defecto del móvil en bucle
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer.create(this, uri).apply {
            isLooping = true
            start()
        }

        // Configurar el vibrador
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Patrón: Espera 0ms, vibra 500ms, pausa 500ms (0 significa que se repite en bucle)
        val pattern = longArrayOf(0, 500, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun apagarAlarmaYSalir() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        vibrator?.cancel()
        finish() // Cierra esta actividad y devuelve al usuario a lo que estuviera haciendo
    }

    override fun onDestroy() {
        super.onDestroy()
        // Por seguridad, si el sistema destruye la actividad, paramos el ruido
        mediaPlayer?.release()
        vibrator?.cancel()
    }
}

// --- INTERFAZ A PANTALLA COMPLETA ---
@Composable
fun AlarmaScreenUI(nombreAlarma: String, onDesactivar: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFC62828) // Rojo oscuro de alerta
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("¡GEOVALLA ALCANZADA!", fontSize = 20.sp, color = Color.White.copy(alpha = 0.8f), letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(nombreAlarma, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Spacer(modifier = Modifier.height(80.dp))

            Button(
                onClick = onDesactivar,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFC62828)),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth(0.8f).height(64.dp)
            ) {
                Text("APAGAR ALARMA", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}