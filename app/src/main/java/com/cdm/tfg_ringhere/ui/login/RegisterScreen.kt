package com.cdm.tfg_ringhere.ui.login

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cdm.tfg_ringhere.data.network.RetrofitClient
import com.cdm.tfg_ringhere.data.network.UsuarioCreate
import kotlinx.coroutines.launch

// Colores del diseño
private val BrandBlue = Color(0xFF2B3A8B)
private val SoftGrayBg = Color(0xFFF1F4F9)
private val TextMain = Color(0xFF1E293B)
private val TextSecondary = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, isSpanish: Boolean) { // Recibe el idioma
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 28.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)
        ) {
            // Logo / Nombre App
            Text(
                text = "Ring here",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = BrandBlue,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Título y Subtítulo
            Text(
                text = if (isSpanish) "Crear Cuenta" else "Create Account",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextMain
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSpanish) "Únete para empezar a gestionar tus geoalarmas." else "Join us to start managing your geo-alarms.",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo: Full Name
            CustomRegisterField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = if (isSpanish) "Nombre completo" else "Full Name",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Email
            CustomRegisterField(
                value = email,
                onValueChange = { email = it },
                placeholder = if (isSpanish) "Correo electrónico" else "Email Address",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Password
            CustomRegisterField(
                value = password,
                onValueChange = { password = it },
                placeholder = if (isSpanish) "Contraseña" else "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                isPasswordVisible = passwordVisible,
                onVisibilityToggle = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Confirm Password
            CustomRegisterField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = if (isSpanish) "Confirmar contraseña" else "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                isPasswordVisible = confirmVisible,
                onVisibilityToggle = { confirmVisible = !confirmVisible }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón SIGN UP
            Button(
                onClick = {
                    // --- LÓGICA DE VALIDACIÓN ---
                    if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                        val msg = if (isSpanish) "Rellena todos los campos" else "Please fill all fields"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        val msg = if (isSpanish) "¡Las contraseñas no coinciden!" else "Passwords do not match!"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        scope.launch {
                            try {
                                val api = RetrofitClient.getApiService(context)
                                val user = UsuarioCreate(email, password, fullName)
                                val res = api.register(user)
                                if (res.isSuccessful) {
                                    val msg = if (isSpanish) "¡Cuenta creada!" else "Account created!"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    val msg = if (isSpanish) "El email ya está registrado" else "Email already registered"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                val msg = if (isSpanish) "Error de conexión" else "Connection error"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = if (isSpanish) "REGISTRARSE" else "SIGN UP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Or register with
            Text(text = if (isSpanish) "O regístrate con" else "Or register with", fontSize = 14.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                SocialCircleButton(Icons.Default.AccountCircle)
                SocialCircleButton(Icons.Default.Home)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer
            Row {
                Text(text = if (isSpanish) "¿Ya tienes una cuenta? " else "Already have an account? ", color = TextSecondary)
                Text(
                    text = if (isSpanish) "Inicia sesión" else "Log in",
                    color = BrandBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityToggle: () -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.6f)) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = BrandBlue.copy(alpha = 0.4f)) },
        trailingIcon = {
            if (isPassword) {
                val icon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = onVisibilityToggle) {
                    Icon(icon, contentDescription = null, tint = BrandBlue.copy(alpha = 0.3f))
                }
            }
        },
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SoftGrayBg,
            unfocusedContainerColor = SoftGrayBg,
            disabledContainerColor = SoftGrayBg,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun SocialCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(SoftGrayBg)
            .clickable { /* Social Login */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(24.dp))
    }
}