package com.example.fridgemate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RecipeFilter { ALL, FAST, VEGGIE, FRIDGE }

class RecipeViewModel : ViewModel() {

    private var allRecipesCache = listOf<RecipeData>()

    // MÉMOIRE DU FRIGO : On garde les ingrédients ici pour les calculs futurs
    private var storedIngredients: List<IngredientData> = emptyList()

    var visibleRecipes by mutableStateOf<List<RecipeData>>(emptyList())
        private set

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

    // 1. CHARGEMENT INITIAL VIA LE FRIGO
    fun loadRecipesFromFridge(fridgeIngredients: List<IngredientData>) {
        // On sauvegarde le frigo pour plus tard
        storedIngredients = fridgeIngredients

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            if (fridgeIngredients.isEmpty()) {
                searchRecipes("Pasta")
                return@launch
            }

            try {
                // Recherche parallèle sur tous les ingrédients
                val uniqueIngredients = fridgeIngredients.distinctBy { it.name }
                val deferredResults = uniqueIngredients.map { ingredient ->
                    async(Dispatchers.IO) {
                        try {
                            val response = TheMealDbClient.apiService.searchMeals(ingredient.name)
                            response.meals?.map { it.toDomainModel() } ?: emptyList()
                        } catch (e: Exception) {
                            emptyList<RecipeData>()
                        }
                    }
                }

                val resultsList = deferredResults.awaitAll()
                val combinedRecipes = resultsList.flatten().distinctBy { it.id }

                // IMPORTANT : On calcule les scores AVANT de mettre en cache
                calculateScores(combinedRecipes)

                allRecipesCache = combinedRecipes

                // On met à jour la liste "Cook with what you have" (triée par score)
                updateMatchingRecipesList()

                // On active le filtre
                onFilterSelected(RecipeFilter.FRIDGE)

            } catch (e: Exception) {
                errorMessage = "Error finding matching recipes: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // 2. RECHERCHE MANUELLE
    fun searchRecipes(query: String = "") {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = withContext(Dispatchers.IO) {
                    TheMealDbClient.apiService.searchMeals(query)
                }
                val meals = response.meals ?: emptyList()
                val domainMeals = meals.map { it.toDomainModel() }

                // ICI LE FIX : On recalcule les scores immédiatement avec le frigo en mémoire
                calculateScores(domainMeals)

                allRecipesCache = domainMeals

                if (selectedFilter == RecipeFilter.FRIDGE) {
                    selectedFilter = RecipeFilter.ALL
                }
                applyFilters()

            } catch (e: Exception) {
                errorMessage = "Error loading recipes: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // --- NOUVELLE FONCTION DÉDIÉE AU CALCUL ---
    private fun calculateScores(recipes: List<RecipeData>) {
        if (storedIngredients.isEmpty()) return

        recipes.forEach { recipe ->
            // Combien on en a ?
            val currentMatchCount = recipe.ingredients?.count { recipeIng ->
                storedIngredients.any { userIng ->
                    val rName = recipeIng.name ?: ""
                    val uName = userIng.name
                    rName.contains(uName, ignoreCase = true) || uName.contains(rName, ignoreCase = true)
                }
            } ?: 0

            // Combien il en manque ?
            val totalIngredients = recipe.ingredients?.size ?: 0
            val currentMissingCount = if (totalIngredients > currentMatchCount) totalIngredients - currentMatchCount else 0

            recipe.matchingCount = currentMatchCount
            recipe.missingCount = currentMissingCount
        }
    }

    // Mise à jour de la liste spécifique "Cook with what you have"
    private fun updateMatchingRecipesList() {
        val sorted = allRecipesCache
            .filter { it.matchingCount > 0 }
            .sortedByDescending { it.matchingCount }

        matchingRecipes.clear()
        matchingRecipes.addAll(sorted)
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        if (query.length > 2) searchRecipes(query) else applyFilters()
    }

    fun onFilterSelected(filter: RecipeFilter) {
        selectedFilter = filter
        applyFilters()
    }

    private fun applyFilters() {
        var result = allRecipesCache

        if (searchQuery.isNotBlank() && result.size > 1) {
            result = result.filter { recipe ->
                recipe.title.contains(searchQuery, ignoreCase = true)
            }
        }

        result = when (selectedFilter) {
            RecipeFilter.ALL -> result
            RecipeFilter.FAST -> result.filter { it.duration <= 30 }
            RecipeFilter.VEGGIE -> result.filter { it.isVegetarian }
            RecipeFilter.FRIDGE -> if (matchingRecipes.isNotEmpty()) matchingRecipes else emptyList()
        }
        visibleRecipes = result
    }
}