package com.cdm.tfg_ringhere.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cdm.tfg_ringhere.ui.components.RingHereBottomBar

// --- COLORES EXTRAÍDOS DE TU DISEÑO ---
private val PrimaryBlue = Color(0xFF2B3A8B)
private val LightBackground = Color(0xFFF7F8FC)
private val CardGray = Color(0xFFEAEBEE)
private val TextGray = Color(0xFF6B7280)
private val SectionTextBlue = Color(0xFF4A55A2) // Azul intermedio para los títulos de sección

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(navController: NavController) {
    // Estados para los interruptores
    var altoContraste by remember { mutableStateOf(false) }
    var vibracion by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontSize = 18.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBackground)
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

            // 1. Tarjeta Cabecera (Azul)
            item { HeaderAjustes() }

            // 2. Sección: APARIENCIA Y ACCESIBILIDAD
            item {
                SettingsSection(title = "APARIENCIA Y ACCESIBILIDAD") {
                    SettingsActionItem(
                        title = "Tema de la aplicación",
                        subtitle = "Predeterminado del sistema",
                        onClick = { /* TODO */ }
                    )
                    HorizontalDivider(color = LightBackground, thickness = 2.dp)
                    SettingsToggleItem(
                        title = "Alto Contraste",
                        subtitle = "Mejora la legibilidad para problemas de visión",
                        isChecked = altoContraste,
                        onToggle = { altoContraste = it }
                    )
                }
            }

            // 3. Sección: UBICACIÓN Y BATERÍA
            item {
                SettingsSection(title = "UBICACIÓN Y BATERÍA") {
                    SettingsActionItem(
                        title = "Precisión del GPS",
                        subtitle = "Equilibrio entre rapidez de alerta y consumo de batería",
                        onClick = { /* TODO */ }
                    )
                    HorizontalDivider(color = LightBackground, thickness = 2.dp)
                    SettingsActionItem(
                        title = "Permisos de ubicación",
                        subtitle = "Gestionar el acceso en segundo plano",
                        icon = Icons.Default.OpenInNew,
                        onClick = { /* TODO: Abrir ajustes del sistema */ }
                    )
                }
            }

            // 4. Sección: NOTIFICACIONES Y SONIDO
            item {
                SettingsSection(title = "NOTIFICACIONES Y SONIDO") {
                    SettingsActionItem(
                        title = "Tono de alarma predeterminado",
                        subtitle = "Radar (predeterminado)",
                        onClick = { /* TODO */ }
                    )
                    HorizontalDivider(color = LightBackground, thickness = 2.dp)
                    SettingsToggleItem(
                        title = "Vibración",
                        subtitle = "Vibrar al activar la alarma",
                        isChecked = vibracion,
                        onToggle = { vibracion = it }
                    )
                }
            }

            // 5. Sección: INFORMACIÓN
            item {
                SettingsSection(title = "INFORMACIÓN") {
                    SettingsActionItem(
                        title = "Política de Privacidad",
                        onClick = { /* TODO */ }
                    )
                    HorizontalDivider(color = LightBackground, thickness = 2.dp)
                    SettingsBadgeItem(
                        title = "Versión de la aplicación",
                        subtitle = "v1.0.0 (TFG Build)",
                        badgeText = "STABLE"
                    )
                }
            }

            // 6. Tarjeta Footer de Privacidad (Gris)
            item { PrivacyFooter() }

            item { Spacer(modifier = Modifier.height(40.dp)) } // Espacio al final
        }
    }
}

@Composable
fun HeaderAjustes() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Configuración",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Personaliza tu experiencia de rastreo inteligente.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            color = SectionTextBlue,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(content = content)
        }
    }
}

// --- TIPOS DE ITEMS DE AJUSTES ---

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
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 13.sp, color = TextGray)
            }
        }
        Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
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
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = TextGray)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue, // Ajustado a tu diseño (azul)
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
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
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = TextGray)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CardGray)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
        }
    }
}

@Composable
fun PrivacyFooter() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Security, contentDescription = "Privacidad", tint = PrimaryBlue, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Tus datos de ubicación nunca salen de este dispositivo. Todo el procesamiento de geofencing ocurre localmente para tu privacidad.",
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}