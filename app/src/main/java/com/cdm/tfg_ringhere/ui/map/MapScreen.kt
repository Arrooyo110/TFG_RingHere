package com.cdm.tfg_ringhere.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

private val AccentCyan = Color(0xFF31E2C2)
private val AlarmRed = Color(0xFFC62828)
private val InactiveGray = Color(0xFF9E9E9E)

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

    val prefs = remember { context.getSharedPreferences("RingHereSettings", Context.MODE_PRIVATE) }
    var temaConfigurado by remember {
        mutableStateOf(prefs.getString("tema_app", "Predeterminado del sistema") ?: "Predeterminado del sistema")
    }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            if (key == "tema_app") {
                temaConfigurado = sharedPrefs.getString("tema_app", "Predeterminado del sistema") ?: "Predeterminado del sistema"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val esModoOscuro = when (temaConfigurado) {
        "Oscuro" -> true
        "Claro" -> false
        else -> isSystemInDarkTheme()
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var selectedOption by remember { mutableStateOf(MapOptionData.Standard) }
    var marcadorPosicion by remember { mutableStateOf<LatLng?>(null) }
    var layersMenuExpanded by remember { mutableStateOf(false) }

    val radioAlarma = 450.0
    val alarmas by viewModel.alarmas.collectAsState(initial = emptyList())

    val defaultPos = LatLng(40.4167, -3.7037)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPos, 12f)
    }

    val mapProperties = remember(esModoOscuro, selectedOption) {
        MapProperties(
            mapType = selectedOption.type,
            isMyLocationEnabled = true,
            mapStyleOptions = if (esModoOscuro && selectedOption.type == MapType.NORMAL) {
                MapStyleOptions(getDarkMapJsonStyle())
            } else null
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false
            ),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
            onMapLongClick = { latLng ->
                marcadorPosicion = latLng
                layersMenuExpanded = false
            },
            onMapClick = {
                layersMenuExpanded = false
            }
        ) {
            alarmas.forEach { alarma ->
                val position = LatLng(alarma.latitud, alarma.longitud)
                val activeBlue = MaterialTheme.colorScheme.primary
                val baseColor = if (!alarma.isActive) InactiveGray else if (alarma.isAlEntrar) activeBlue else AlarmRed

                Circle(
                    center = position,
                    radius = alarma.radio.toDouble(),
                    fillColor = baseColor.copy(alpha = 0.2f),
                    strokeColor = baseColor,
                    strokeWidth = 2f
                )

                MarkerInfoWindow(
                    state = MarkerState(position = position),
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (!alarma.isActive || alarma.isAlEntrar) BitmapDescriptorFactory.HUE_AZURE
                        else BitmapDescriptorFactory.HUE_RED
                    ),
                    alpha = if (alarma.isActive) 1.0f else 0.5f
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = alarma.nombre,
                                fontWeight = FontWeight.Bold,
                                color = activeBlue,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (alarma.isAlEntrar) "Alarma al entrar" else "Alarma al salir",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

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

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            scope.launch {
                                val userPos = LatLng(it.latitude, it.longitude)
                                cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(userPos, 15f)))
                            }
                        } ?: run {
                            Toast.makeText(context, "Buscando señal GPS...", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Centrar mapa")
            }

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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                    .heightIn(max = 200.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "TIPO DE MAPA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(8.dp)
                    )
                    MapOptionData.entries.forEach { option ->
                        val isSelected = option == currentOption
                        val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(backgroundColor)
                                .clickable { onOptionSelected(option) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(option.icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option.title,
                                color = contentColor,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onToggleExpand,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.Default.Layers, contentDescription = "Capas de mapa")
        }
    }
}

private fun getDarkMapJsonStyle(): String {
    return """
        [
          { "elementType": "geometry", "stylers": [{ "color": "#242f3e" }] },
          { "elementType": "labels.text.stroke", "stylers": [{ "color": "#242f3e" }] },
          { "elementType": "labels.text.fill", "stylers": [{ "color": "#746855" }] },
          { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{ "color": "#d59563" }] },
          { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [{ "color": "#d59563" }] },
          { "featureType": "poi.park", "elementType": "geometry", "stylers": [{ "color": "#263c3f" }] },
          { "featureType": "poi.park", "elementType": "labels.text.fill", "stylers": [{ "color": "#6b9a76" }] },
          { "featureType": "road", "elementType": "geometry", "stylers": [{ "color": "#38414e" }] },
          { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#212a37" }] },
          { "featureType": "road", "elementType": "labels.text.fill", "stylers": [{ "color": "#9ca5b3" }] },
          { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#17263c" }] }
        ]
    """.trimIndent()
}