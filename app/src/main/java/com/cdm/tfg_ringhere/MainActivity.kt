package com.cdm.tfg_ringhere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.cdm.tfg_ringhere.data.local.AppDatabase
import com.cdm.tfg_ringhere.data.repository.AlarmaRepository
import com.cdm.tfg_ringhere.ui.PantallaLogin
import com.cdm.tfg_ringhere.ui.theme.TFG_RingHereTheme
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cdm.tfg_ringhere.ui.create.CreateAlarmScreen
import com.cdm.tfg_ringhere.ui.dashboard.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializamos la Base de Datos y el Repositorio
        val database = AppDatabase.getDatabase(this)
        val repository = AlarmaRepository(database.alarmaDao())
        val factory = AlarmaViewModelFactory(repository)

        setContent {
            TFG_RingHereTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: AlarmaViewModel = viewModel(factory = factory)

                    // 1. Creamos el controlador del GPS de nuestra app
                    val navController = rememberNavController()

                    // 2. Definimos el mapa de carreteras (NavHost)
                    NavHost(navController = navController, startDestination = "mapa") {

                        // Ruta 1: Pantalla de Login
                        composable("login") {
                            PantallaLogin(viewModel = viewModel, navController = navController)
                        }

                        // Ruta 2: Pantalla Principal (Dashboard)
                        composable("dashboard") {
                            DashboardScreen(viewModel = viewModel)
                        }

                        // Ruta 3: Pantalla del mapa
                        composable("mapa") {
                            // Asegúrate de importar com.cdm.tfg_ringhere.ui.map.MapScreen
                            com.cdm.tfg_ringhere.ui.map.MapScreen(navController = navController)
                        }

                        // Ruta 4: Pantalla de crear alarma
                        composable("crear_alarma/{lat}/{lng}") { backStackEntry ->
                            val lat = backStackEntry.arguments?.getString("lat")?.toDouble() ?: 0.0
                            val lng = backStackEntry.arguments?.getString("lng")?.toDouble() ?: 0.0
                            CreateAlarmScreen(navController, viewModel, lat, lng)
                        }
                    }
                }
            }
        }
    }
}

