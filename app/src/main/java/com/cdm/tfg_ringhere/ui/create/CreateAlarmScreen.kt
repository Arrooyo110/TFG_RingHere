package com.cdm.tfg_ringhere.ui.create

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

private val AlarmRed = Color(0xFFC62828)
private val AccentCyan = Color(0xFF31E2C2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen(
    navController: NavController,
    viewModel: AlarmaViewModel,
    lat: Double,
    lng: Double,
    alarmaId: String? = null
) {
    var nombreAlarma by remember { mutableStateOf("") }
    var radioValue by remember { mutableStateOf(450f) }
    var triggerAlEntrar by remember { mutableStateOf(true) }

    val context = LocalContext.current

    // --- Tema reactivo ---
    val prefs = remember { context.getSharedPreferences("RingHereSettings", Context.MODE_PRIVATE) }
    var temaConfigurado by remember { mutableStateOf(prefs.getString("tema_app", "Predeterminado del sistema") ?: "Predeterminado del sistema") }

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

    // --- Configuración del mapa ---
    val ubicacionSeleccionada = LatLng(lat, lng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacionSeleccionada, 15f)
    }

    val mapProperties = remember(esModoOscuro) {
        MapProperties(
            mapType = MapType.NORMAL,
            mapStyleOptions = if (esModoOscuro) MapStyleOptions(getDarkMapJsonStyle()) else null
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nueva Alarma", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            // --- Nombre ---
            Text("NOMBRE DE LA ALARMA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nombreAlarma,
                onValueChange = { nombreAlarma = it },
                placeholder = { Text("Ej: Trabajo", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Ubicación ---
            Text("Ubicación", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Define el radio de activación", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(16.dp))

            // --- Mapa preview ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(4.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, mapToolbarEnabled = false),
                    properties = mapProperties
                ) {
                    Marker(
                        state = MarkerState(position = ubicacionSeleccionada),
                        title = nombreAlarma.ifEmpty { "Nueva Alarma" }
                    )

                    val dynamicStrokeColor = if (triggerAlEntrar) MaterialTheme.colorScheme.primary else AlarmRed
                    val dynamicFillColor = dynamicStrokeColor.copy(alpha = 0.15f)

                    Circle(
                        center = ubicacionSeleccionada,
                        radius = radioValue.toDouble(),
                        fillColor = dynamicFillColor,
                        strokeColor = dynamicStrokeColor,
                        strokeWidth = 2f
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MapMiniButton(Icons.Default.MyLocation)
                    MapMiniButton(Icons.Default.Layers)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Radio ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SettingsInputAntenna, contentDescription = null, tint = AccentCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Radio", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                            append("${radioValue.toInt()}")
                        }
                        append(" METROS")
                    },
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = radioValue,
                onValueChange = { radioValue = it },
                valueRange = 200f..1000f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Tipo de activación ---
            Text("TIPO DE ACTIVACIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActivationOption(
                    text = "Al entrar",
                    icon = Icons.Default.Login,
                    isSelected = triggerAlEntrar,
                    onClick = { triggerAlEntrar = true },
                    modifier = Modifier.weight(1f)
                )
                ActivationOption(
                    text = "Al salir",
                    icon = Icons.Default.Logout,
                    isSelected = !triggerAlEntrar,
                    onClick = { triggerAlEntrar = false },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Guardar ---
            Button(
                onClick = {
                    if (nombreAlarma.isNotBlank()) {
                        viewModel.guardarNuevaAlarma(
                            alarmaId = alarmaId,
                            nombre = nombreAlarma,
                            lat = lat,
                            lng = lng,
                            radio = radioValue,
                            alEntrar = triggerAlEntrar,
                            context = context
                        )
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Por favor, ponle un nombre a la alarma", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Guardar Alarma", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// --- Subcomponentes ---

@Composable
fun MapMiniButton(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ActivationOption(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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