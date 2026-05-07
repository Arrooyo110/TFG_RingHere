package com.cdm.tfg_ringhere.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel

// --- COLORES DEL SISTEMA ---
private val PrimaryBlue = Color(0xFF2B3A8B)
private val LightBackground = Color(0xFFF7F8FC)
private val InputGray = Color(0xFFEAEBEE)
private val TextGray = Color(0xFF6B7280)
private val IconBlueSoft = Color(0xFFE0E5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin(viewModel: AlarmaViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // Para el botón del ojito

    val context = LocalContext.current

    // Estados del ViewModel
    val loginCompletado by viewModel.loginExitoso.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()

    // Navegación automática si el login es exitoso
    LaunchedEffect(loginCompletado) {
        if (loginCompletado) {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .systemBarsPadding()
            .imePadding() // Empuja la UI hacia arriba si sale el teclado
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Icono superior (Escudo)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(IconBlueSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = "Logo",
                tint = PrimaryBlue,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Textos de Bienvenida
        Text("Bienvenido", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Inicia sesión para continuar", fontSize = 16.sp, color = TextGray)

        Spacer(modifier = Modifier.height(40.dp))

        // 3. Campo de Correo
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("CORREO ELECTRÓNICO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Ingresa tu correo", color = TextGray.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = TextGray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = InputGray,
                    unfocusedContainerColor = InputGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Campo de Contraseña
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("CONTRASEÑA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Ingresa tu contraseña", color = TextGray.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = TextGray) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Mostrar contraseña", tint = TextGray)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = InputGray,
                    unfocusedContainerColor = InputGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Botón de Login
        Button(
            onClick = { viewModel.loginUsuario(email, password, context) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        // Mensaje de Error (si existe)
        if (mensajeError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensajeError!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Olvidaste tu contraseña
        Text(
            text = "¿Olvidaste tu contraseña?",
            color = PrimaryBlue,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.clickable { /* TODO: Navegar a recuperar password */ }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 7. Divisor "O CONTINUAR CON"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = InputGray)
            Text("  O CONTINUAR CON  ", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.weight(1f), color = InputGray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 8. Botones Sociales (Placeholders visuales)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Google Placeholder
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.size(56.dp).clickable { }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("G", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 24.sp) // Reemplazar con Drawable en el futuro
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Apple Placeholder
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.size(56.dp).clickable { }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("", color = Color.Black, fontSize = 28.sp) // Reemplazar con Drawable en el futuro
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 9. Registro
        val signUpText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = TextGray)) { append("¿No tienes una cuenta? ") }
            withStyle(style = SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) { append("Regístrate") }
        }
        Text(
            text = signUpText,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .clickable { /* TODO: Navegar a registro */ }
        )
    }
}