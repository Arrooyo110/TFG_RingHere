package com.cdm.tfg_ringhere.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val PrimaryBlue = Color(0xFF2B3A8B)
private val TextGray = Color(0xFF6B7280)

@Composable
fun RingHereBottomBar(navController: NavController, rutaActual: String) {
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
            // 1. Botón Dashboard
            BottomBarItem(
                icon = Icons.Default.List,
                label = "MIS ALARMAS",
                isSelected = rutaActual == "dashboard",
                onClick = {
                    if (rutaActual != "dashboard") {
                        navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } }
                    }
                }
            )

            // 2. Botón Mapa
            BottomBarItem(
                icon = Icons.Default.Place,
                label = "MAPA",
                isSelected = rutaActual == "mapa",
                onClick = {
                    if (rutaActual != "mapa") navController.navigate("mapa")
                }
            )

            // 3. Botón Ajustes
            BottomBarItem(
                icon = Icons.Default.Settings,
                label = "AJUSTES",
                isSelected = rutaActual == "ajustes",
                onClick = {
                    if (rutaActual != "ajustes") navController.navigate("ajustes")
                }
            )
        }
    }
}

@Composable
fun BottomBarItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    if (isSelected) {
        // Diseño ACTIVO (Azul, tipo botón)
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        // Diseño INACTIVO (Gris, solo icono y texto)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = TextGray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
        }
    }
}