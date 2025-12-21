package com.example.fridgemate

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers // Import required
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Import required

class LoginViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun login(username: String, onLoginSuccess: (UserData) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.login(LoginRequest(username))
                }

                if (response.success) {
                    // Vérification de sécurité supplémentaire
                    if (response.user.id.isNotEmpty()) {
                        onLoginSuccess(response.user)
                    } else {
                        errorMessage = "Erreur : ID utilisateur introuvable dans la réponse serveur."
                    }
                } else {
                    // Si success = false, on affiche un message générique ou celui du serveur si dispo
                    errorMessage = "Échec de la connexion"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur : ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}