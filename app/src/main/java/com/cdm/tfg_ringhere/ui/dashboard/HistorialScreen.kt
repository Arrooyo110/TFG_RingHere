package com.cdm.tfg_ringhere.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cdm.tfg_ringhere.data.local.AppDatabase
import com.cdm.tfg_ringhere.model.EventoAlarma
import com.cdm.tfg_ringhere.ui.components.RingHereBottomBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).eventoAlarmaDao() }
    val eventos by dao.getTodosLosEventos().collectAsState(initial = emptyList())
    var mostrarConfirmBorrar by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Historial de Alarmas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (eventos.isNotEmpty()) {
                    IconButton(onClick = { mostrarConfirmBorrar = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Limpiar historial",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        bottomBar = { RingHereBottomBar(navController = navController, rutaActual = "historial") }
    ) { paddingValues ->

        if (eventos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin eventos registrados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cuando una alarma salte, aparecerá aquí\ncon la hora y ubicación exacta.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                item {
                    Text(
                        text = "${eventos.size} evento${if (eventos.size != 1) "s" else ""} registrado${if (eventos.size != 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                items(eventos) { evento ->
                    EventoCard(evento)
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (mostrarConfirmBorrar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmBorrar = false },
            title = { Text("¿Limpiar historial?") },
            text = { Text("Se borrarán todos los eventos registrados. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) { dao.limpiarTodo() }
                    mostrarConfirmBorrar = false
                }) {
                    Text("Borrar todo", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmBorrar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EventoCard(evento: EventoAlarma) {
    val esEntrar = evento.tipoTransicion == "ENTRAR"
    val colorTipo = if (esEntrar) Color(0xFF2563EB) else Color(0xFFDC2626)
    val labelTipo = if (esEntrar) "AL ENTRAR" else "AL SALIR"

    val fechaFormateada = remember(evento.timestamp) {
        SimpleDateFormat("dd MMM yyyy · HH:mm:ss", Locale("es", "ES"))
            .format(Date(evento.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de tipo
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colorTipo.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = colorTipo,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = evento.nombreAlarma,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorTipo.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = labelTipo,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorTipo,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = fechaFormateada,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Coordenadas exactas donde saltó
                if (evento.latitudDetectada != 0.0 || evento.longitudDetectada != 0.0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍 %.5f, %.5f".format(evento.latitudDetectada, evento.longitudDetectada),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}