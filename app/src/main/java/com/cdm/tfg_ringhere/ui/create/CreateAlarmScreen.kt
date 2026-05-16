package com.cdm.tfg_ringhere.ui.create

import androidx.compose.foundation.clickable
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
import com.google.maps.android.compose.*

// --- PALETA DE COLORES ---
private val PrimaryBlue = Color(0xFF2B3A8B)
private val AlarmRed = Color(0xFFC62828) // Un rojo claro pero serio para alarmas de salida
private val LightBackground = Color(0xFFF7F8FC)
private val CardGray = Color(0xFFEAEBEE)
private val TextGray = Color(0xFF6B7280)
private val AccentCyan = Color(0xFF31E2C2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen(
    navController: NavController,
    viewModel: AlarmaViewModel,
    lat: Double,
    lng: Double
) {
    var nombreAlarma by remember { mutableStateOf("") }
    var radioValue by remember { mutableStateOf(450f) }

    // Este estado controla la lógica y ahora también el color
    var triggerAlEntrar by remember { mutableStateOf(true) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val ubicacionSeleccionada = LatLng(lat, lng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacionSeleccionada, 15f)
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nueva Alarma", fontWeight = FontWeight.Bold, color = PrimaryBlue) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryBlue)
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
            // Campo Nombre
            Text("NOMBRE DE LA ALARMA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nombreAlarma,
                onValueChange = { nombreAlarma = it },
                placeholder = { Text("Ej: Trabajo", color = TextGray.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,   // <-- LETRA SIEMPRE OSCURA
                    unfocusedTextColor = Color.Black, // <-- LETRA SIEMPRE OSCURA
                    unfocusedBorderColor = CardGray,
                    focusedBorderColor = PrimaryBlue
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sección Ubicación
            Text("Ubicación", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Text("Define el radio de activación", fontSize = 13.sp, color = TextGray)

            Spacer(modifier = Modifier.height(16.dp))

            // --- MAPA PREVIEW INTERACTIVO CON LÓGICA DE COLOR ---
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
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                    properties = MapProperties(mapType = MapType.NORMAL)
                ) {
                    Marker(
                        state = MarkerState(position = ubicacionSeleccionada),
                        title = nombreAlarma.ifEmpty { "Nueva Alarma" }
                    )

                    // --- AQUÍ ESTÁ LA MAGIA DEL COLOR DINÁMICO ---
                    val dynamicStrokeColor = if (triggerAlEntrar) PrimaryBlue else AlarmRed
                    val dynamicFillColor = dynamicStrokeColor.copy(alpha = 0.15f)

                    Circle(
                        center = ubicacionSeleccionada,
                        radius = radioValue.toDouble(),
                        fillColor = dynamicFillColor,
                        strokeColor = dynamicStrokeColor,
                        strokeWidth = 2f
                    )
                }

                // Botones flotantes (MyLocation, Layers)
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

            // Slider de Radio
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SettingsInputAntenna, contentDescription = null, tint = AccentCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Radio", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                            append("${radioValue.toInt()}")
                        }
                        append(" METROS")
                    },
                    color = PrimaryBlue
                )
            }

            Slider(
                value = radioValue,
                onValueChange = { radioValue = it },
                valueRange = 100f..1000f,
                colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tipo de activación
            Text("TIPO DE ACTIVACIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
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

            // Botón Guardar
            Button(
                onClick = {
                    if (nombreAlarma.isNotBlank()) {
                        viewModel.guardarNuevaAlarma(
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
                        android.widget.Toast.makeText(
                            context,
                            "Por favor, ponle un nombre a la alarma",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                // <-- SE FUERZA EL ICONO Y EL TEXTO A BLANCO -->
                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Guardar Alarma", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun MapMiniButton(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
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
        color = if (isSelected) PrimaryBlue else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, CardGray)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else PrimaryBlue, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = if (isSelected) Color.White else PrimaryBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}