package com.cdm.tfg_ringhere.ui.map

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cdm.tfg_ringhere.ui.components.RingHereBottomBar
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

// --- PALETA DE COLORES ---
private val PrimaryBlue = Color(0xFF2B3A8B)
private val AccentCyan = Color(0xFF31E2C2)
private val AlarmRed = Color(0xFFC62828)
private val InactiveGray = Color(0xFF9E9E9E)
private val TextGray = Color(0xFF6B7280)

enum class MapOptionData(val title: String, val type: MapType, val icon: ImageVector) {
    Standard("Normal", MapType.NORMAL, Icons.Default.Map),
    Satellite("Satélite", MapType.SATELLITE, Icons.Default.SatelliteAlt),
    Hybrid("Híbrido", MapType.HYBRID, Icons.Default.Layers)
}

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(navController: NavController, viewModel: AlarmaViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cliente para obtener la ubicación real del GPS
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var selectedOption by remember { mutableStateOf(MapOptionData.Standard) }
    var marcadorPosicion by remember { mutableStateOf<LatLng?>(null) }
    var layersMenuExpanded by remember { mutableStateOf(false) }

    val radioAlarma = 450.0
    val alarmas by viewModel.alarmas.collectAsState(initial = emptyList())

    // Posición inicial (Móstoles/Madrid)
    val defaultPos = LatLng(40.4167, -3.7037)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPos, 12f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = selectedOption.type,
                isMyLocationEnabled = true
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false // Usamos nuestro botón personalizado
            ),
            // Ajustamos el padding para que los elementos nativos de Google no se tapen
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
            onMapLongClick = { latLng ->
                marcadorPosicion = latLng
                layersMenuExpanded = false
            },
            onMapClick = {
                layersMenuExpanded = false
            }
        ) {
            // Dibujar alarmas guardadas
            alarmas.forEach { alarma ->
                val position = LatLng(alarma.latitud, alarma.longitud)
                val baseColor = if (!alarma.isActive) InactiveGray else if (alarma.isAlEntrar) PrimaryBlue else AlarmRed

                Circle(
                    center = position,
                    radius = alarma.radio.toDouble(),
                    fillColor = baseColor.copy(alpha = 0.2f),
                    strokeColor = baseColor,
                    strokeWidth = 2f
                )

                Marker(
                    state = MarkerState(position = position),
                    title = alarma.nombre,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (!alarma.isActive || alarma.isAlEntrar) BitmapDescriptorFactory.HUE_AZURE
                        else BitmapDescriptorFactory.HUE_RED
                    ),
                    alpha = if (alarma.isActive) 1.0f else 0.5f
                )
            }

            // Marcador temporal para nueva alarma
            marcadorPosicion?.let { posicion ->
                Marker(
                    state = MarkerState(position = posicion),
                    title = "Nueva Ubicación",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                )
                Circle(
                    center = posicion,
                    radius = radioAlarma,
                    fillColor = AccentCyan.copy(alpha = 0.25f),
                    strokeColor = AccentCyan,
                    strokeWidth = 3f
                )
            }
        }

        // --- CONTROLES FLOTANTES (Derecha) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón Mi Ubicación Real
            FloatingActionButton(
                onClick = {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            scope.launch {
                                val userPos = LatLng(it.latitude, it.longitude)
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.fromLatLngZoom(userPos, 15f)
                                    )
                                )
                            }
                        } ?: run {
                            android.widget.Toast.makeText(context, "Buscando señal GPS...", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                containerColor = Color.White,
                contentColor = PrimaryBlue,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Centrar")
            }

            // Selector de Capas
            MapLayerFabSelector(
                isExpanded = layersMenuExpanded,
                currentOption = selectedOption,
                onToggleExpand = { layersMenuExpanded = !layersMenuExpanded },
                onOptionSelected = {
                    selectedOption = it
                    layersMenuExpanded = false
                }
            )
        }

        // --- PARTE INFERIOR (Confirmar + Navegación) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (marcadorPosicion != null) {
                Button(
                    onClick = {
                        navController.navigate("crear_alarma/${marcadorPosicion?.latitude}/${marcadorPosicion?.longitude}")
                    },
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .fillMaxWidth(0.85f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Confirmar ubicación", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            RingHereBottomBar(navController = navController, rutaActual = "mapa")
        }
    }
}

@Composable
fun MapLayerFabSelector(
    isExpanded: Boolean,
    currentOption: MapOptionData,
    onToggleExpand: () -> Unit,
    onOptionSelected: (MapOptionData) -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .width(160.dp)
                    .heightIn(max = 200.dp), // <-- 1. Límite máximo de altura para que no se salga de la pantalla
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()) // <-- 2. Le damos la magia del scroll
                ) {
                    Text(
                        "TIPO DE MAPA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        modifier = Modifier.padding(8.dp)
                    )
                    MapOptionData.values().forEach { option ->
                        val isSelected = option == currentOption
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { onOptionSelected(option) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(option.icon, null, tint = if (isSelected) PrimaryBlue else InactiveGray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(option.title, color = if (isSelected) PrimaryBlue else Color.Black, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onToggleExpand,
            containerColor = Color.White,
            contentColor = PrimaryBlue,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.Default.Layers, contentDescription = "Capas")
        }
    }
}