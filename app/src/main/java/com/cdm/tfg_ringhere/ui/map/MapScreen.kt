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
import com.cdm.tfg_ringhere.ui.components.RingHereBottomBar
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// --- COLORES ---
private val PrimaryBlue = Color(0xFF2B3A8B)

private val AccentCyan = Color(0xFF31E2C2) // El color verde/cyan de tu diseño
private val AlarmRed = Color(0xFFC62828)
private val InactiveGray = Color(0xFF9E9E9E)
private val TextGray = Color(0xFF6B7280)

enum class MapOption(val title: String, val type: MapType) {
    Standard("Normal", MapType.NORMAL),
    Satellite("Satélite", MapType.SATELLITE),
    Hybrid("Híbrido", MapType.HYBRID)
}

@Composable
fun MapScreen(navController: NavController, viewModel: AlarmaViewModel) { // <-- AÑADIDO viewModel
    var selectedOption by remember { mutableStateOf(MapOption.Standard) }
    var marcadorPosicion by remember { mutableStateOf<LatLng?>(null) }
    val radioAlarma = 450.0 // Radio por defecto para nuevas alarmas

    // Obtenemos las alarmas guardadas en la base de datos
    val alarmas by viewModel.alarmas.collectAsState(initial = emptyList())

    val madrid = LatLng(40.416775, -3.703790)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(madrid, 11f)
    }

    Scaffold(
        bottomBar = { RingHereBottomBar(navController = navController, rutaActual = "mapa") }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = selectedOption.type),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
                onMapLongClick = { latLng ->
                    // Al hacer pulsación larga, ponemos el marcador temporal para CREAR
                    marcadorPosicion = latLng
                }
            ) {
                // --- 1. PINTAR LAS ALARMAS EXISTENTES ---
                // --- 1. PINTAR LAS ALARMAS EXISTENTES ---
                alarmas.forEach { alarma ->
                    val position = LatLng(alarma.latitud, alarma.longitud)

                    val baseColor = if (!alarma.isActive) {
                        InactiveGray
                    } else if (alarma.isAlEntrar) {
                        PrimaryBlue
                    } else {
                        AlarmRed
                    }

                    Circle(
                        center = position,
                        radius = alarma.radio.toDouble(),
                        fillColor = baseColor.copy(alpha = 0.2f),
                        strokeColor = baseColor,
                        strokeWidth = 2f
                    )

                    // Cambiamos la lógica de la chincheta
                    val hueColor = if (!alarma.isActive) {
                        BitmapDescriptorFactory.HUE_AZURE // Si está inactiva, la dejamos azul...
                    } else if (alarma.isAlEntrar) {
                        BitmapDescriptorFactory.HUE_AZURE
                    } else {
                        BitmapDescriptorFactory.HUE_RED
                    }

                    Marker(
                        state = MarkerState(position = position),
                        title = alarma.nombre,
                        snippet = if (alarma.isActive) "Radio: ${alarma.radio.toInt()}m" else "Desactivada",
                        icon = BitmapDescriptorFactory.defaultMarker(hueColor),
                        // ... ¡Pero le bajamos muchísimo la opacidad para que se vea "apagada" o grisácea!
                        alpha = if (alarma.isActive) 1.0f else 0.35f
                    )
                }

                // --- 2. PINTAR EL MARCADOR TEMPORAL (NUEVA ALARMA) ---
                marcadorPosicion?.let { posicion ->
                    Marker(
                        state = MarkerState(position = posicion),
                        title = "Ubicación seleccionada",
                        // Usamos un tono verde/cyan de Google Maps para que pegue con tu app
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                    )
                    Circle(
                        center = posicion,
                        radius = radioAlarma,
                        fillColor = AccentCyan.copy(alpha = 0.25f), // Tu color personalizado
                        strokeColor = AccentCyan,
                        strokeWidth = 3f
                    )
                }
            }

            // Selector de tipo de mapa
            MapTypeSelector(
                currentOption = selectedOption,
                onOptionSelected = { selectedOption = it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 24.dp)
            )

            // Botón de confirmar (Solo sale si hay un marcador temporal)
            if (marcadorPosicion != null) {
                Button(
                    onClick = {
                        navController.navigate("crear_alarma/${marcadorPosicion?.latitude}/${marcadorPosicion?.longitude}")
                    },
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