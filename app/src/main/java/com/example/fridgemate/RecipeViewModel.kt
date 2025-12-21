package com.example.fridgemate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RecipeFilter { ALL, FAST, VEGGIE, FRIDGE }

class RecipeViewModel : ViewModel() {

    private var allRecipesCache = listOf<RecipeData>()

    var visibleRecipes by mutableStateOf<List<RecipeData>>(emptyList())
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
                // MODIFICATION ICI : On récupère l'enveloppe et on extrait la liste
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllRecipes()
                }
                allRecipesCache = response.recipes // <-- On prend .recipes
                applyFilters()
            } catch (e: Exception) {
                errorMessage = "Error loading recipes: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // --- Recherche par ingrédients (Bonus : si tu veux utiliser le Search de l'API) ---
    fun searchByIngredients(ingredients: List<String>) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.searchRecipes(RecipeSearchRequest(ingredients))
                }
                allRecipesCache = response.recipes
                applyFilters()
            } catch (e: Exception) {
                errorMessage = "Search failed: ${e.localizedMessage}"
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

        // 2. Filtre par Chips
        result = when (selectedFilter) {
            RecipeFilter.ALL -> result
            RecipeFilter.FAST -> result.filter { (it.duration ?: 999) <= 30 }
            RecipeFilter.VEGGIE -> result.filter { it.isVegetarian }
            RecipeFilter.FRIDGE -> result // À implémenter plus tard
        }

        visibleRecipes = result
    }
}