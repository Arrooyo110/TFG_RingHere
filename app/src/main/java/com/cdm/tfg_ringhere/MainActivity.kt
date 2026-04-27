package com.cdm.tfg_ringhere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cdm.tfg_ringhere.data.local.AppDatabase
import com.cdm.tfg_ringhere.data.repository.AlarmaRepository
import com.cdm.tfg_ringhere.model.Alarma
import com.cdm.tfg_ringhere.ui.theme.TFG_RingHereTheme
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModelFactory
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializamos la Base de Datos y el Repositorio (Inyección manual)
        val database = AppDatabase.getDatabase(this)
        val repository = AlarmaRepository(database.alarmaDao())
        val factory = AlarmaViewModelFactory(repository)

        setContent {
            TFG_RingHereTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. Obtenemos el ViewModel usando nuestro Factory
                    val viewModel: AlarmaViewModel = viewModel(factory = factory)

                    // 3. Mostramos la pantalla principal
                    PantallaPruebaAlarmas(viewModel)
                }
            }
        }
    }
}

@Composable
fun PantallaPruebaAlarmas(viewModel: AlarmaViewModel) {
    // Observamos la lista de alarmas. Si cambia en la BD, la UI se repinta sola.
    val alarmas by viewModel.alarmas.collectAsState()

    Column(modifier = Modifier
        .systemBarsPadding() // <-- ESTA ES LA MAGIA
        .padding(16.dp)) {
        // --- BOTÓN PARA AÑADIR ---
        Button(
            onClick = {
                // Creamos una alarma con datos de prueba
                val nuevaAlarma = Alarma(
                    nombre = "Alarma de prueba #${Random.nextInt(1, 100)}",
                    latitud = 40.3223, // Coordenadas ficticias
                    longitud = -3.8657,
                    radio = 250f,
                    isAlEntrar = true
                )
                viewModel.insert(nuevaAlarma)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Añadir Alarma de Prueba")
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // --- LISTA DE ALARMAS GUARDADAS ---
        Text(
            text = "Alarmas en la Base de Datos:",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(alarmas) { alarma ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    // Usamos una fila (Row) para poner el texto y el botón uno al lado del otro
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = alarma.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Radio: ${alarma.radio} metros", style = MaterialTheme.typography.bodySmall)
                        }

                        // --- BOTÓN DE BORRAR ---
                        IconButton(onClick = { viewModel.delete(alarma) }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                contentDescription = "Borrar alarma",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}