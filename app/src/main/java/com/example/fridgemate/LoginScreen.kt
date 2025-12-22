package com.example.fridgemate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(onLoginSuccess: (UserData) -> Unit) {
    val viewModel: LoginViewModel = viewModel()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WebBg), // Gris clair
        contentAlignment = Alignment.Center
    ) {
        // Cercle décoratif en arrière-plan
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-120).dp)
                .size(350.dp)
                .background(WebGreen.copy(alpha = 0.15f), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 400.dp)
        ) {

            // --- LOGO ---
            Surface(
                shape = CircleShape,
                color = WebGreen,
                shadowElevation = 8.dp,
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Kitchen,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Titre dynamique
            Text(
                text = if (viewModel.isRegisterMode) "New Chef?" else "Welcome Back!",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WebTextDark
            )

            Text(
                text = if (viewModel.isRegisterMode) "Create a username to start" else "Enter your username to login",
                fontSize = 15.sp,
                color = WebTextGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- CARTE FORMULAIRE ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Zone d'erreur
                    if (viewModel.errorMessage != null) {
                        Surface(
                            color = WebRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = viewModel.errorMessage!!,
                                color = WebRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Champ Username
                    OutlinedTextField(
                        value = viewModel.username,
                        onValueChange = { viewModel.username = it },
                        label = { Text("Username") },
                        placeholder = { Text("Ex: ChefGordon") },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = WebGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WebGreen,
                            focusedLabelColor = WebGreen,
                            cursorColor = WebGreen
                        )
                    )

                    // Bouton Principal
                    Button(
                        onClick = { viewModel.submit(onLoginSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WebGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (viewModel.isRegisterMode) "Create Account" else "Log In",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TOGGLE LOGIN / REGISTER ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        viewModel.isRegisterMode = !viewModel.isRegisterMode
                        viewModel.errorMessage = null
                    }
                    .padding(8.dp)
            ) {
                Text(
                    text = if (viewModel.isRegisterMode) "Already have an account? " else "No account yet? ",
                    color = WebTextGray,
                    fontSize = 15.sp
                )
                Text(
                    text = if (viewModel.isRegisterMode) "Log In" else "Sign Up",
                    color = WebGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}