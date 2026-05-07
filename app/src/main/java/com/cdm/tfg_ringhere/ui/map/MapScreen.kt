package com.cdm.tfg_ringhere.ui.map

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// --- 1. DEFINICIÓN DE COLORES Y ENUMS (Esto es lo que te faltaba arriba) ---
private val PrimaryBlue = Color(0xFF2B3A8B)
private val TextGray = Color(0xFF6B7280)

enum class MapOption(val title: String, val type: MapType) {
    Standard("Normal", MapType.NORMAL),
    Satellite("Satélite", MapType.SATELLITE),
    Hybrid("Híbrido", MapType.HYBRID)
}

@Composable
fun MapScreen(navController: NavController) {
    var selectedOption by remember { mutableStateOf(MapOption.Standard) }
    var marcadorPosicion by remember { mutableStateOf<LatLng?>(null) }
    val radioAlarma = 500.0

    val madrid = LatLng(40.416775, -3.703790)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(madrid, 11f)
    }

    // EL BOX es el contenedor principal
    Box(modifier = Modifier.fillMaxSize()) {

        // EL MAPA
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = selectedOption.type),
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            onMapLongClick = { latLng ->
                marcadorPosicion = latLng
            }
        ) {
            // Los elementos internos del mapa (Marker y Circle) van AQUÍ DENTRO
            marcadorPosicion?.let { posicion ->
                Marker(
                    state = MarkerState(position = posicion),
                    title = "Nueva Alarma"
                )
                Circle(
                    center = posicion,
                    radius = radioAlarma,
                    fillColor = PrimaryBlue.copy(alpha = 0.2f),
                    strokeColor = PrimaryBlue,
                    strokeWidth = 2f
                )
            }
        }

        // EL SELECTOR (Fuera del mapa, pero dentro del Box)
        MapTypeSelector(
            currentOption = selectedOption,
            onOptionSelected = { selectedOption = it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 24.dp)
        )

        // EL BOTÓN (Solo si hay marcador)
        if (marcadorPosicion != null) {
            Button(
                onClick = { navController.navigate("crear_alarma/${marcadorPosicion?.latitude}/${marcadorPosicion?.longitude}") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Confirmar ubicación", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun MapTypeSelector(
    currentOption: MapOption,
    onOptionSelected: (MapOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MapOption.values().forEach { option ->
                val isSelected = option == currentOption
                val backgroundColor by animateColorAsState(if (isSelected) PrimaryBlue else Color.Transparent)
                val textColor by animateColorAsState(if (isSelected) Color.White else TextGray)

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .clickable { onOptionSelected(option) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.title,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}