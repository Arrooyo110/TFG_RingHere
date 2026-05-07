package com.cdm.tfg_ringhere.ui.dashboard

import androidx.compose.foundation.background
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
import com.cdm.tfg_ringhere.model.Alarma
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel

// --- COLORES EXTRAÍDOS DE TU DISEÑO ---
val PrimaryBlue = Color(0xFF2B3A8B)
val LightBackground = Color(0xFFF7F8FC)
val CardGray = Color(0xFFEAEBEE)
val TextGray = Color(0xFF6B7280)
val EmeraldGreen = Color(0xFF057A55)
val CyanGps = Color(0xFF31E2C2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AlarmaViewModel) {
    // Observamos las alarmas reales de la base de datos
    val alarmas by viewModel.alarmas.collectAsState()

    Scaffold(
        containerColor = LightBackground,
        topBar = { TopBarDesign() },
        bottomBar = { BottomNavigationDesign() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Navegar a CreateAlarmScreen */ },
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

            // 1. Tarjeta Principal (Estado Actual)
            item { HeroCardGPS() }

            // 2. Cabecera de la lista
            item { SectionHeader(alarmCount = alarmas.size) }

            // 3. Lista dinámica de alarmas
            items(alarmas) { alarma ->
                AlarmaItemCard(alarma)
            }

            // Espacio extra al final para que el FAB no tape la última tarjeta
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TopBarDesign() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
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
fun SectionHeader(alarmCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Mis Alarmas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("$alarmCount geoalarmas configuradas", fontSize = 14.sp, color = TextGray)
        }
        Text("Editar Lista", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun AlarmaItemCard(alarma: Alarma) {
    // Usamos el estado del interruptor. Por defecto lo marcamos activo para la UI
    var isChecked by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardGray),
        elevation = CardDefaults.cardElevation(0.dp)
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
            Switch(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
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

@Composable
fun BottomNavigationDesign() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(32.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Activo
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.List, contentDescription = "Mis Alarmas", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("MIS ALARMAS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Items Inactivos
            BottomNavItem(icon = Icons.Default.Place, label = "MAPA")
            BottomNavItem(icon = Icons.Default.Settings, label = "AJUSTES")
        }
    }
}

@Composable
fun BottomNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        Icon(icon, contentDescription = label, tint = TextGray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
    }
}