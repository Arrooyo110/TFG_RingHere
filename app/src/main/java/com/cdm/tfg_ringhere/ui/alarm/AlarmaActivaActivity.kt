package com.cdm.tfg_ringhere.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.cdm.tfg_ringhere.R

class AlarmaActivaActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Mostrar por encima de la pantalla de bloqueo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        super.onCreate(savedInstanceState)

        val nombreAlarma = intent.getStringExtra("NOMBRE_ALARMA") ?: "Alarma Desconocida"

        val prefs = getSharedPreferences("RingHereSettings", Context.MODE_PRIVATE)
        val permitirVibracion = prefs.getBoolean("vibracion", true)
        val tonoSeleccionado = prefs.getString("tono_alarma", "Predeterminado del sistema") ?: "Predeterminado del sistema"

        aplicarSonido(tonoSeleccionado)
        if (permitirVibracion) {
            aplicarVibracion()
        }

        setContent {
            TFG_RingHereTheme {
                AlarmaScreenUI(nombreAlarma = nombreAlarma) {
                    apagarAlarmaYSalir()
                }
            }
        }
    }

    // --- Audio ---

    private fun aplicarSonido(tono: String) {
        if (tono == "Silencioso") return

        try {
            val uriTono: Uri = when (tono) {
                "Radar (Predeterminado)" -> Uri.parse("android.resource://$packageName/${R.raw.radar_alarm}")
                "Campana clásica" -> Uri.parse("android.resource://$packageName/${R.raw.classic_alarm}")
                "Digital" -> Uri.parse("android.resource://$packageName/${R.raw.digital_alarm}")
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uriTono)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(audioAttributes)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Vibración ---

    private fun aplicarVibracion() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 700, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    // --- Apagado ---

    private fun apagarAlarmaYSalir() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        vibrator?.cancel()

        val notificationId = intent.getIntExtra("NOTIFICACION_ID", -1)
        if (notificationId != -1) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(notificationId)
        }

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        vibrator?.cancel()
    }
}

// --- Interfaz a pantalla completa ---

@Composable
fun AlarmaScreenUI(nombreAlarma: String, onDesactivar: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFC62828)
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