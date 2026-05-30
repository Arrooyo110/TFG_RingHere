package com.cdm.tfg_ringhere

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cdm.tfg_ringhere.data.local.AppDatabase
import com.cdm.tfg_ringhere.data.repository.AlarmaRepository
import com.cdm.tfg_ringhere.ui.create.CreateAlarmScreen
import com.cdm.tfg_ringhere.ui.dashboard.DashboardScreen
import com.cdm.tfg_ringhere.ui.dashboard.HistorialScreen
import com.cdm.tfg_ringhere.ui.login.PantallaLogin
import com.cdm.tfg_ringhere.ui.login.RegisterScreen
import com.cdm.tfg_ringhere.ui.settings.AjustesScreen
import com.cdm.tfg_ringhere.ui.theme.TFG_RingHereTheme
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModelFactory
import com.cdm.tfg_ringhere.utils.SessionManager

// Importaciones para el agente en segundo plano
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = AlarmaRepository(database.alarmaDao())
        val factory = AlarmaViewModelFactory(repository)

        // Configuración de WorkManager (Agente en segundo plano)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<com.cdm.tfg_ringhere.utils.AlarmaSyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "AlarmaCloudSyncPeriodicWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        setContent {
            val context = LocalContext.current

            // CONEXIÓN REACTIVA A LOS AJUSTES
            val prefs = remember { context.getSharedPreferences("RingHereSettings", Context.MODE_PRIVATE) }

            // Estados que Compose vigilará de cerca
            var temaConfigurado by remember { mutableStateOf(prefs.getString("tema_app", "Predeterminado del sistema")) }
            var altoContrasteActivo by remember { mutableStateOf(prefs.getBoolean("alto_contraste", false)) }

            // Escuchador en tiempo real: Si cambia SharedPreferences, actualiza los estados de Compose
            DisposableEffect(prefs) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "tema_app") {
                        temaConfigurado = prefs.getString("tema_app", "Predeterminado del sistema")
                    }
                    if (key == "alto_contraste") {
                        altoContrasteActivo = prefs.getBoolean("alto_contraste", false)
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            // Calculamos matemáticamente si corresponde usar modo oscuro o claro
            val usarModoOscuro = when (temaConfigurado) {
                "Oscuro" -> true
                "Claro" -> false
                else -> isSystemInDarkTheme() // "Predeterminado del sistema" lee el SO del móvil
            }

            // 🎨 PASAMOS LOS PARAMETROS DINÁMICOS AL TEMA GLOBAL
            TFG_RingHereTheme(
                darkTheme = usarModoOscuro,
                highContrast = altoContrasteActivo // Pasamos el estado de alto contraste
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: AlarmaViewModel = viewModel(factory = factory)
                    val navController = rememberNavController()

                    val sessionManager = remember { SessionManager(context) }
                    val tokenGuardado = sessionManager.fetchAuthToken()
                    val rutaInicial = if (!tokenGuardado.isNullOrEmpty()) "dashboard" else "login"

                    NavHost(navController = navController, startDestination = rutaInicial) {
                        composable("login") {
                            PantallaLogin(viewModel = viewModel, navController = navController)
                        }
                        composable("dashboard") {
                            DashboardScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("mapa") {
                            com.cdm.tfg_ringhere.ui.map.MapScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(
                            route = "crear_alarma/{lat}/{lng}?alarmaId={alarmaId}",
                            arguments = listOf(
                                navArgument("lat") { type = NavType.StringType },
                                navArgument("lng") { type = NavType.StringType },
                                navArgument("alarmaId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val lat = backStackEntry.arguments?.getString("lat")?.toDouble() ?: 0.0
                            val lng = backStackEntry.arguments?.getString("lng")?.toDouble() ?: 0.0
                            val alarmaId = backStackEntry.arguments?.getString("alarmaId")

                            CreateAlarmScreen(navController, viewModel, lat, lng, alarmaId)
                        }
                        composable("ajustes") {
                            AjustesScreen(navController = navController)
                        }
                        composable("historial") {
                            HistorialScreen(navController = navController)
                        }
                        composable(
                            route = "register/{isSpanish}",
                            arguments = listOf(navArgument("isSpanish") { type = NavType.BoolType })
                        ) { backStackEntry ->
                            val isSpanish = backStackEntry.arguments?.getBoolean("isSpanish") ?: true
                            RegisterScreen(navController = navController, isSpanish = isSpanish)
                        }
                    }
                }
            }
        }
    }
}