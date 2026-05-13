package com.cdm.tfg_ringhere.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cdm.tfg_ringhere.model.Alarma
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel
import com.cdm.tfg_ringhere.ui.components.RingHereBottomBar

// --- COLORES ---
val PrimaryBlue = Color(0xFF2B3A8B)
val LightBackground = Color(0xFFF7F8FC)
val CardGray = Color(0xFFEAEBEE)
val TextGray = Color(0xFF6B7280)
val EmeraldGreen = Color(0xFF057A55)
val CyanGps = Color(0xFF31E2C2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: AlarmaViewModel) {
    val alarmas by viewModel.alarmas.collectAsState(initial = emptyList())
    var isEditing by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        containerColor = LightBackground,
        topBar = { TopBarDesign() },
        bottomBar = { RingHereBottomBar(navController = navController, rutaActual = "dashboard") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("mapa") },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir alarma", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { HeroCardGPS() }
            item {
                SectionHeader(
                    alarmCount = alarmas.size,
                    isEditing = isEditing,
                    onEditClick = { isEditing = !isEditing }
                )
            }

            items(alarmas) { alarma ->
                AlarmaItemCard(
                    alarma = alarma,
                    isEditing = isEditing,
                    onDelete = { viewModel.eliminarAlarma(alarma, context) },
                    // --- AQUÍ ESTÁ LA CONEXIÓN CON EL VIEWMODEL ---
                    onToggle = { nuevoEstado ->
                        viewModel.actualizarEstadoAlarma(alarma, nuevoEstado, context)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ... TopBarDesign, HeroCardGPS y SectionHeader se mantienen igual ...

@Composable
fun TopBarDesign() {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = PrimaryBlue)
            Spacer(modifier = Modifier.width(16.dp))
            Text("Ring here", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
        }
        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = PrimaryBlue, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun HeroCardGPS() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("ESTADO ACTUAL", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("1.2", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold)
                Text(" km", color = Color.White.copy(alpha = 0.9f), fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))
            }
            Text("Hasta la alarma más cercana", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CyanGps))
                Spacer(modifier = Modifier.width(8.dp))
                Text("GPS Activo", color = CyanGps, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SectionHeader(alarmCount: Int, isEditing: Boolean, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Mis Alarmas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("$alarmCount geoalarmas configuradas", fontSize = 14.sp, color = TextGray)
        }
        Text(
            text = if (isEditing) "Hecho" else "Editar Lista",
            color = if (isEditing) Color.Red else PrimaryBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onEditClick() }.padding(8.dp)
        )
    }
}

@Composable
fun AlarmaItemCard(
    alarma: Alarma,
    isEditing: Boolean,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit // <-- NUEVO PARÁMETRO
) {
    // Usamos el valor real que viene de la base de datos
    // Si la base de datos cambia, Compose volverá a dibujar esta tarjeta automáticamente
    val isChecked = alarma.isActive

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(alarma.nombre, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                val tipo = if (alarma.isAlEntrar) "Al entrar" else "Al salir"
                Text("Radio: ${alarma.radio.toInt()}m • $tipo", fontSize = 14.sp, color = TextGray)
            }

            if (isEditing) {
                IconButton(onClick = { onDelete() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar alarma", tint = Color.Red)
                }
            } else {
                Switch(
                    checked = isChecked,
                    onCheckedChange = { onToggle(it) }, // <-- EJECUTAMOS LA ACCIÓN
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EmeraldGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }
        }
    }
}