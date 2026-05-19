package com.cdm.tfg_ringhere.ui.dashboard

// =====================================================================
// 1. IMPORTACIONES
// =====================================================================
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cdm.tfg_ringhere.R
import com.cdm.tfg_ringhere.model.Alarma
import com.cdm.tfg_ringhere.ui.components.RingHereBottomBar
import com.cdm.tfg_ringhere.utils.SessionManager
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel

// =====================================================================
// 2. COLORES SEMÁNTICOS (BATERÍA, GPS Y RADAR)
// =====================================================================
val EmeraldGreen = Color(0xFF057A55)
val CyanGps = Color(0xFF31E2C2)

// =====================================================================
// 3. PANTALLA PRINCIPAL (DASHBOARD)
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: AlarmaViewModel) {
    val alarmas by viewModel.alarmas.collectAsState(initial = emptyList())
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val radarActivo by viewModel.radarActivo.collectAsState()
    val alarmaCercana by viewModel.alarmaCercana.collectAsState()
    val distanciaMetros by viewModel.distanciaCercanaMetros.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.cargarAlarmasDelUsuario(context)
        viewModel.sincronizarAlarmas(context)
    }

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) android.util.Log.e("PERMISOS", "Ubicación en segundo plano denegada.")
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val ubicacionAceptada = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (ubicacionAceptada) {
            viewModel.iniciarRastreoUbicacion(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    LaunchedEffect(Unit) {
        val permisosAPedir = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        locationPermissionLauncher.launch(permisosAPedir.toTypedArray())
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBarDesign(navController = navController, viewModel = viewModel) },
        bottomBar = { RingHereBottomBar(navController = navController, rutaActual = "dashboard") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("mapa") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir", modifier = Modifier.size(28.dp), tint = Color.White)
            }
        }
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.sincronizarAlarmas(context) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    HeroCardGPS(
                        alarmaCercana = alarmaCercana,
                        distanciaActual = distanciaMetros,
                        isRadarActivo = radarActivo,
                        onToggleRadar = { viewModel.alternarEstadoRadar(context) }
                    )
                }

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
                        onToggle = { nuevoEstado ->
                            viewModel.actualizarEstadoAlarma(alarma, nuevoEstado, context)
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// =====================================================================
// 4. COMPONENTES VISUALES ADAPTATIVOS
// =====================================================================

@Composable
fun HeroCardGPS(
    alarmaCercana: Alarma?,
    distanciaActual: Float?,
    isRadarActivo: Boolean,
    onToggleRadar: () -> Unit
) {
    val (valorNumerico, unidad) = if (!isRadarActivo || distanciaActual == null) {
        Pair("--", "km")
    } else if (distanciaActual >= 1000f) {
        Pair(String.format(java.util.Locale.US, "%.1f", distanciaActual / 1000f), "km")
    } else {
        Pair(distanciaActual.toInt().toString(), "m")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "ESTADO ACTUAL",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = valorNumerico,
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " $unidad",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Text(
                text = "Hasta la alarma: ${alarmaCercana?.nombre ?: "Ninguna activa o buscando..."}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRadarActivo && distanciaActual != null) CyanGps
                                else if (isRadarActivo) Color.Yellow
                                else Color.LightGray
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRadarActivo && distanciaActual == null) "Buscando GPS..."
                        else if (isRadarActivo) "Radar Activo"
                        else "Radar Pausado",
                        color = if (isRadarActivo && distanciaActual != null) CyanGps
                        else if (isRadarActivo) Color.Yellow
                        else Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onToggleRadar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = if (isRadarActivo) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isRadarActivo) "Pausar" else "Reanudar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TopBarDesign(navController: NavController, viewModel: AlarmaViewModel) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val sessionManager = remember { SessionManager(context) }
    val userEmail = sessionManager.getUserEmail() ?: "usuario@email.com"
    val userName = sessionManager.getUserName() ?: userEmail.substringBefore("@")
    val iniciales = userName.take(2).uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo Ring Here",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Ring here",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { showMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Text(text = iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface).width(220.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(text = "Sesión iniciada como", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(text = userEmail, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.background, thickness = 1.dp)

                DropdownMenuItem(
                    text = { Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Medium) },
                    leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red) },
                    onClick = {
                        showMenu = false
                        sessionManager.clearSession()
                        viewModel.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                )
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
            Text("Mis Alarmas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("$alarmCount geoalarmas configuradas", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Text(
            text = if (isEditing) "Hecho" else "Editar Lista",
            color = if (isEditing) Color.Red else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onEditClick() }.padding(8.dp)
        )
    }
}

@Composable
fun AlarmaItemCard(alarma: Alarma, isEditing: Boolean, onDelete: () -> Unit, onToggle: (Boolean) -> Unit) {
    val isChecked = alarma.isActive
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(alarma.nombre, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                val tipo = if (alarma.isAlEntrar) "Al entrar" else "Al salir"
                Text("Radio: ${alarma.radio.toInt()}m • $tipo", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            if (isEditing) {
                IconButton(onClick = { onDelete() }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            } else {
                Switch(
                    checked = isChecked,
                    onCheckedChange = { onToggle(it) },
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