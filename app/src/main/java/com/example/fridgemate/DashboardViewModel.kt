package com.example.fridgemate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(val ingredients: List<IngredientData>, val alerts: List<IngredientAlert>) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

data class IngredientAlert(val name: String, val daysRemaining: Long)

class DashboardViewModel : ViewModel() {
    var uiState by mutableStateOf<DashboardUiState>(DashboardUiState.Loading)
        private set

    // Pour recharger les données après un ajout/suppression
    private var currentUserId: String? = null

    fun loadData(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            uiState = DashboardUiState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getIngredients(userId)
                }

                // Extraction et Tri (les plus proches de la péremption en premier)
                val ingredientsList = response.ingredients.sortedBy { it.expiryDate }

                val alerts = ingredientsList.mapNotNull { ing ->
                    calculateDaysRemaining(ing.expiryDate)?.let { days ->
                        if (days <= 5) IngredientAlert(ing.name, days) else null
                    }
                }.sortedBy { it.daysRemaining }

                uiState = DashboardUiState.Success(ingredientsList, alerts)

            } catch (e: Exception) {
                uiState = DashboardUiState.Error("Error: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }

    fun addIngredient(name: String, quantity: String, expiryDate: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val request = AddIngredientRequest(
                        name = name,
                        quantity = quantity,
                        unit = "unit", // Valeur par défaut
                        expiryDate = expiryDate.ifBlank { null },
                        category = "General"
                    )
                    RetrofitClient.apiService.addIngredient(userId, request)
                }
                // Recharger la liste après succès
                loadData(userId)
            } catch (e: Exception) {
                // Gérer l'erreur silencieusement ou via un Toast (non implémenté ici pour simplicité)
                e.printStackTrace()
            }
        }
    }

    fun deleteIngredient(ingredientId: String) {
        val userId = currentUserId ?: return

        // Sécurité : Si l'ID est vide, on annule pour éviter le crash
        if (ingredientId.isBlank()) {
            println("DEBUG: Tentative de suppression avec un ID vide !")
            return
        }

        viewModelScope.launch {
            try {
                println("DEBUG: Suppression de l'ingrédient $ingredientId pour le user $userId")

                withContext(Dispatchers.IO) {
                    val response = RetrofitClient.apiService.deleteIngredient(userId, ingredientId)
                    if (response.isSuccessful) {
                        println("DEBUG: Suppression réussie sur le serveur")
                    } else {
                        println("DEBUG: Erreur serveur code=${response.code()}")
                    }
                }
                // Rechargement des données
                loadData(userId)
            } catch (e: Exception) {
                println("DEBUG: Crash lors de la suppression : ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }

    private fun calculateDaysRemaining(dateString: String?): Long? {
        if (dateString.isNullOrEmpty()) return null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cleanDate = if (dateString.contains("T")) dateString.split("T")[0] else dateString
            val date = format.parse(cleanDate) ?: return null
            val diff = date.time - System.currentTimeMillis()
            TimeUnit.MILLISECONDS.toDays(diff)
        } catch (e: Exception) {
            null
        }
    }

    fun fetchProductInfo(barcode: String, onResult: (String, String) -> Unit) {
        println("DEBUG: Début recherche pour barcode: $barcode") // LOG 1

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    OffRetrofitClient.apiService.getProduct(barcode)
                }

                println("DEBUG: Réponse API Status: ${response.status}") // LOG 2

                if (response.status == 1 && response.product != null) {
                    // On utilise la nouvelle fonction getBestName()
                    val name = response.product.getBestName()
                    val qty = response.product.quantity ?: ""

                    println("DEBUG: Produit trouvé: $name, Qty: $qty") // LOG 3

                    // Retour sur le thread principal pour l'UI
                    withContext(Dispatchers.Main) {
                        onResult(name, qty)
                    }
                } else {
                    println("DEBUG: Produit non trouvé dans la base (Status 0)") // LOG 4
                    withContext(Dispatchers.Main) {
                        onResult("Introuvable ($barcode)", "")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Erreur Réseau/API: ${e.message}") // LOG 5
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult("Erreur réseau", "")
                }
            }
        }
    }
}