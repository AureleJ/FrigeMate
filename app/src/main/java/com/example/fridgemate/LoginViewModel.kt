package com.example.fridgemate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    // Champs de saisie
    var username by mutableStateOf("")

    // Mode : true = Création de compte, false = Connexion
    var isRegisterMode by mutableStateOf(false)

    // États de l'UI
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun submit(onSuccess: (UserData) -> Unit) {
        if (username.isBlank()) {
            errorMessage = "Please enter a username"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // On crée l'objet requête (défini dans Network.kt)
                val request = UserRequest(username)

                if (isRegisterMode) {
                    // --- CAS 1 : INSCRIPTION ---
                    // Ton API "POST /" renvoie directement l'objet UserData.
                    // Si le serveur renvoie une erreur (ex: 400 ou 500), ça partira dans le catch.
                    val newUser = RetrofitClient.apiService.createUser(request)

                    println("DEBUG: Inscription réussie. ID=${newUser.id}")
                    onSuccess(newUser)

                } else {
                    // --- CAS 2 : CONNEXION ---
                    // Ton API "POST /login" renvoie un objet LoginResponse { success: true, user: ... }
                    val response = RetrofitClient.apiService.login(request)

                    // On vérifie le booléen success ET que l'objet user n'est pas null
                    if (response.success && response.user != null) {
                        println("DEBUG: Login réussi. ID=${response.user.id}")
                        onSuccess(response.user)
                    } else {
                        // Si success=false ou user=null
                        errorMessage = "User not found"
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Si c'est une erreur réseau ou une erreur HTTP (400, 404, 500...)
                errorMessage = if (isRegisterMode) {
                    "Error: User might already exist"
                } else {
                    "Connection error or User not found"
                }
            } finally {
                isLoading = false
            }
        }
    }
}