package com.example.fridgemate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RecipeFilter { ALL, FAST, VEGGIE, FRIDGE }

class RecipeViewModel : ViewModel() {

    // Cache de toutes les recettes chargées (pour ne pas rappeler l'API à chaque filtre)
    private var allRecipesCache = listOf<RecipeData>()

    // Liste affichée à l'écran (après filtres)
    var visibleRecipes by mutableStateOf<List<RecipeData>>(emptyList())
        private set

    // Liste spécifique pour le filtre "Fridge"
    var matchingRecipes = mutableStateListOf<RecipeData>()
        private set

    var searchQuery by mutableStateOf("")
        private set

    var selectedFilter by mutableStateOf(RecipeFilter.ALL)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // CORRECTION ICI :
                // L'API renvoie RecipesResponse, on doit accéder à la propriété .recipes
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllRecipes()
                }

                // On extrait la liste de l'enveloppe
                allRecipesCache = response.recipes
                applyFilters()

            } catch (e: Exception) {
                errorMessage = "Error loading recipes: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun searchByIngredients(ingredients: List<String>) {
        viewModelScope.launch {
            isLoading = true
            try {
                // CORRECTION ICI EGALEMENT :
                // L'API renvoie RecipesResponse, on doit accéder à la propriété .recipes
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.searchRecipes(RecipeSearchRequest(ingredients))
                }

                // On extrait la liste de l'enveloppe
                allRecipesCache = response.recipes
                applyFilters()

            } catch (e: Exception) {
                errorMessage = "Search failed: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun onFilterSelected(filter: RecipeFilter) {
        selectedFilter = filter
        applyFilters()
    }

    private fun applyFilters() {
        var result = allRecipesCache

        // 1. Filtre par Texte
        if (searchQuery.isNotBlank()) {
            result = result.filter { recipe ->
                recipe.title.contains(searchQuery, ignoreCase = true) ||
                        recipe.description?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        // 2. Filtre par Chips (Boutons filtres)
        result = when (selectedFilter) {
            RecipeFilter.ALL -> result
            RecipeFilter.FAST -> result.filter { it.duration in 1..30 } // J'ai ajouté > 0 pour éviter les bugs si durée pas définie
            RecipeFilter.VEGGIE -> result.filter { it.isVegetarian }
            RecipeFilter.FRIDGE -> {
                // Si on a trouvé des recettes qui matchent le frigo, on les montre, sinon on montre tout ou rien selon ton choix
                if (matchingRecipes.isNotEmpty()) matchingRecipes else emptyList()
            }
        }

        visibleRecipes = result
    }

    // --- Logique "Cook with what you have" ---
    // Cette fonction doit être appelée depuis l'UI (ex: LaunchedEffect) en passant les ingrédients du User
    fun updateMatchingRecipes(userIngredients: List<IngredientData>) {
        val all = allRecipesCache

        if (all.isEmpty() || userIngredients.isEmpty()) {
            matchingRecipes.clear()
            return
        }

        val sorted = all.map { recipe ->
            // Score de correspondance : Combien d'ingrédients de la recette j'ai dans mon frigo ?
            val matchCount = recipe.ingredients?.count { recipeIng ->
                userIngredients.any { userIng ->
                    // Comparaison : "Tomato" contient "Tomato" ? ou vice versa
                    val rName = recipeIng.name ?: ""
                    val uName = userIng.name

                    rName.contains(uName, ignoreCase = true) || uName.contains(rName, ignoreCase = true)
                }
            } ?: 0
            Pair(recipe, matchCount)
        }
            .filter { it.second > 0 } // On garde ceux qui ont au moins 1 ingrédient en commun
            .sortedByDescending { it.second } // Les meilleurs scores d'abord
            .map { it.first }

        matchingRecipes.clear()
        matchingRecipes.addAll(sorted.take(10)) // On garde le top 10

        // Si le filtre actuel est FRIDGE, on force la mise à jour de l'affichage
        if (selectedFilter == RecipeFilter.FRIDGE) {
            applyFilters()
        }
    }
}