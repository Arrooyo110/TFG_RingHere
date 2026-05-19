package com.cdm.tfg_ringhere.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cdm.tfg_ringhere.ui.components.RingHereBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("RingHereSettings", Context.MODE_PRIVATE) }

    var altoContraste by remember { mutableStateOf(prefs.getBoolean("alto_contraste", false)) }
    var vibracion by remember { mutableStateOf(prefs.getBoolean("vibracion", true)) }

    var temaActual by remember { mutableStateOf(prefs.getString("tema_app", "Predeterminado del sistema") ?: "Predeterminado del sistema") }
    var gpsPrec by remember { mutableStateOf(prefs.getString("gps_precision", "Alta (Recomendado)") ?: "Alta (Recomendado)") }
    var tonoActual by remember { mutableStateOf(prefs.getString("tono_alarma", "Radar (Predeterminado)") ?: "Radar (Predeterminado)") }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }
    var showRingtoneDialog by remember { mutableStateOf(false) }

    // El fondo principal ahora lee el color de fondo del tema actual
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = { RingHereBottomBar(navController = navController, rutaActual = "ajustes") }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item { HeaderAjustes() }

            item {
                SettingsSection(title = "APARIENCIA Y ACCESIBILIDAD") {
                    SettingsActionItem(
                        title = "Tema de la aplicación",
                        subtitle = temaActual,
                        onClick = { showThemeDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
                    SettingsToggleItem(
                        title = "Alto Contraste",
                        subtitle = "Mejora la legibilidad para problemas de visión",
                        isChecked = altoContraste,
                        onToggle = { nuevoValor ->
                            altoContraste = nuevoValor
                            prefs.edit().putBoolean("alto_contraste", nuevoValor).apply()
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "UBICACIÓN Y BATERÍA") {
                    SettingsActionItem(
                        title = "Precisión del GPS",
                        subtitle = gpsPrec,
                        onClick = { showGpsDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
                    SettingsActionItem(
                        title = "Permisos de ubicación",
                        subtitle = "Gestionar el acceso en segundo plano",
                        icon = Icons.Default.OpenInNew,
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "NOTIFICACIONES Y SONIDO") {
                    SettingsActionItem(
                        title = "Tono de alarma predeterminado",
                        subtitle = tonoActual,
                        onClick = { showRingtoneDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
                    SettingsToggleItem(
                        title = "Vibración",
                        subtitle = "Vibrar al activar la alarma",
                        isChecked = vibracion,
                        onToggle = { nuevoValor ->
                            vibracion = nuevoValor
                            prefs.edit().putBoolean("vibracion", nuevoValor).apply()
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "INFORMACIÓN") {
                    SettingsActionItem(
                        title = "Política de Privacidad",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"))
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
                    SettingsBadgeItem(
                        title = "Versión de la aplicación",
                        subtitle = "v1.0.0 (TFG Build)",
                        badgeText = "STABLE"
                    )
                }
            }

            item { PrivacyFooter() }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showThemeDialog) {
        OptionsDialog(
            title = "Elige un tema",
            options = listOf("Predeterminado del sistema", "Claro", "Oscuro"),
            selectedOption = temaActual,
            onOptionSelected = { seleccion ->
                temaActual = seleccion
                prefs.edit().putString("tema_app", seleccion).apply()
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showGpsDialog) {
        OptionsDialog(
            title = "Precisión del GPS",
            options = listOf("Alta (Recomendado)", "Equilibrada", "Baja (Ahorro de batería)"),
            selectedOption = gpsPrec,
            onOptionSelected = { seleccion ->
                gpsPrec = seleccion
                prefs.edit().putString("gps_precision", seleccion).apply()
                showGpsDialog = false
            },
            onDismiss = { showGpsDialog = false }
        )
    }

    if (showRingtoneDialog) {
        OptionsDialog(
            title = "Tono de alarma",
            options = listOf("Radar (Predeterminado)", "Campana clásica", "Digital", "Silencioso"),
            selectedOption = tonoActual,
            onOptionSelected = { seleccion ->
                tonoActual = seleccion
                prefs.edit().putString("tono_alarma", seleccion).apply()
                showRingtoneDialog = false
            },
            onDismiss = { showRingtoneDialog = false }
        )
    }
}

@Composable
fun OptionsDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (option == selectedOption),
                                onClick = { onOptionSelected(option) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun HeaderAjustes() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Configuración", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Personaliza tu experiencia de rastreo inteligente.", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.ChevronRight,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsBadgeItem(title: String, subtitle: String, badgeText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun PrivacyFooter() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Security, contentDescription = "Privacidad", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Tus datos de ubicación nunca salen de este dispositivo. Todo el procesamiento de geofencing ocurre localmente para tu privacidad.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}