package com.cdm.tfg_ringhere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cdm.tfg_ringhere.data.local.AppDatabase
import com.cdm.tfg_ringhere.data.repository.AlarmaRepository
import com.cdm.tfg_ringhere.ui.theme.TFG_RingHereTheme
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cdm.tfg_ringhere.ui.create.CreateAlarmScreen
import com.cdm.tfg_ringhere.ui.dashboard.DashboardScreen
import com.cdm.tfg_ringhere.ui.login.PantallaLogin
import com.cdm.tfg_ringhere.ui.login.RegisterScreen
import com.cdm.tfg_ringhere.ui.settings.AjustesScreen
import com.cdm.tfg_ringhere.utils.SessionManager

// --- IMPORTACIONES ADICIONALES PARA WORKMANAGER ---
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

        // --- REQUISITO 3: Configurar y encolar el Agente en Segundo Plano (WorkManager) ---
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Solo se ejecuta si hay internet
            .build()

        // El mínimo permitido por el sistema operativo Android por seguridad es cada 15 minutos
        val syncWorkRequest = PeriodicWorkRequestBuilder<com.cdm.tfg_ringhere.utils.AlarmaSyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "AlarmaCloudSyncPeriodicWork",
            ExistingPeriodicWorkPolicy.KEEP, // Si ya existía, no duplica ni pisa el ciclo actual
            syncWorkRequest
        )
        // ---------------------------------------------------------------------------------

        setContent {
            TFG_RingHereTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: AlarmaViewModel = viewModel(factory = factory)
                    val navController = rememberNavController()

                    val context = LocalContext.current
                    val sessionManager = remember { SessionManager(context) }
                    val tokenGuardado = sessionManager.fetchAuthToken()

                    val rutaInicial = if (!tokenGuardado.isNullOrEmpty()) {
                        "dashboard"
                    } else {
                        "login"
                    }

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
                        composable("crear_alarma/{lat}/{lng}") { backStackEntry ->
                            val lat = backStackEntry.arguments?.getString("lat")?.toDouble() ?: 0.0
                            val lng = backStackEntry.arguments?.getString("lng")?.toDouble() ?: 0.0
                            CreateAlarmScreen(navController, viewModel, lat, lng)
                        }
                        composable("ajustes") {
                            AjustesScreen(navController = navController)
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