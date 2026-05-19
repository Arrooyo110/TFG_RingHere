package com.cdm.tfg_ringhere.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cdm.tfg_ringhere.R
import com.cdm.tfg_ringhere.viewmodel.AlarmaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PrimaryBlue = Color(0xFF2B3A8B)
private val LightBackground = Color(0xFFF7F8FC)
private val InputGray = Color(0xFFEAEBEE)
private val TextGray = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin(viewModel: AlarmaViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isSpanish by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val loginCompletado by viewModel.loginExitoso.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(loginCompletado) {
        if (loginCompletado) {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    LaunchedEffect(mensajeError) {
        if (mensajeError != null) {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .systemBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSpanish) "ES" else "EN",
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            IconButton(onClick = { isSpanish = !isSpanish }) {
                Icon(Icons.Default.Language, contentDescription = "Language toggle", tint = PrimaryBlue)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Ring Here Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSpanish) "Bienvenido" else "Welcome",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSpanish) "Inicia sesión para continuar" else "Log in to continue",
                fontSize = 16.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isSpanish) "CORREO ELECTRÓNICO" else "EMAIL ADDRESS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text(if (isSpanish) "Ingresa tu correo" else "Enter your email", color = TextGray.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextGray) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
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

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isSpanish) "CONTRASEÑA" else "PASSWORD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text(if (isSpanish) "Ingresa tu contraseña" else "Enter your password", color = TextGray.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = TextGray)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
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

            Button(
                onClick = {
                    val emailClean = email.trim()
                    val passwordClean = password.trim()

                    if (!isLoading && emailClean.isNotBlank() && passwordClean.isNotBlank()) {
                        isLoading = true
                        keyboardController?.hide()

                        scope.launch {
                            delay(150)
                            viewModel.loginUsuario(emailClean, passwordClean, context)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isSpanish) "Verificando..." else "Verifying...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = if (isSpanish) "Iniciar Sesión" else "Log In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            if (mensajeError != null && !isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = mensajeError!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            val signUpText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextGray)) {
                    append(if (isSpanish) "¿No tienes una cuenta? " else "Don't have an account? ")
                }
                withStyle(style = SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) {
                    append(if (isSpanish) "Regístrate" else "Sign up")
                }
            }
            Text(
                text = signUpText,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clickable { navController.navigate("register/$isSpanish") }
            )
        }
    }
}