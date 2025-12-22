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

    // Cache de toutes les recettes chargées
    private var allRecipesCache = listOf<RecipeData>()

    // Liste affichée à l'écran (après filtres)
    var visibleRecipes by mutableStateOf<List<RecipeData>>(emptyList())
        private set

    // Liste spécifique pour le filtre "Fridge" (Classée par pertinence)
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

    // Fonction principale appelée par l'UI quand le frigo est chargé
    fun loadRecipesFromFridge(fridgeIngredients: List<IngredientData>) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // 1. Si frigo vide, on propose un fallback (ex: Pasta)
            if (fridgeIngredients.isEmpty()) {
                searchRecipes("Pasta")
                return@launch
            }

            try {
                // 2. RECHERCHE PARALLÈLE SUR TOUS LES INGRÉDIENTS
                // On lance une requête API pour chaque ingrédient du frigo.
                // async permet de les lancer toutes en même temps sans attendre les unes après les autres.
                val uniqueIngredients = fridgeIngredients.distinctBy { it.name } // Évite de chercher 2x "Tomate"

                val deferredResults = uniqueIngredients.map { ingredient ->
                    async(Dispatchers.IO) {
                        try {
                            // On cherche les plats qui contiennent le nom de l'ingrédient (ex: "Chicken")
                            val response = TheMealDbClient.apiService.searchMeals(ingredient.name)
                            response.meals?.map { it.toDomainModel() } ?: emptyList()
                        } catch (e: Exception) {
                            // Si une requête échoue (ex: ingrédient bizarre), on l'ignore silencieusement
                            emptyList<RecipeData>()
                        }
                    }
                }

                // 3. AGGRÉGATION DES RÉSULTATS
                // On attend que tout soit fini
                val resultsList = deferredResults.awaitAll()

                // On met tout à plat dans une seule liste et on enlève les doublons (par ID)
                val combinedRecipes = resultsList.flatten().distinctBy { it.id }

                // Mise à jour du cache global avec ces recettes
                allRecipesCache = combinedRecipes

                // 4. CALCUL DE PERTINENCE & TRI
                // C'est ici qu'on va trier pour mettre en premier celles qui utilisent le plus d'ingrédients
                updateMatchingRecipes(fridgeIngredients)

                // 5. ACTIVATION DU FILTRE AUTOMATIQUE
                // On active le filtre FRIDGE pour n'afficher que les recettes pertinentes
                onFilterSelected(RecipeFilter.FRIDGE)

            } catch (e: Exception) {
                errorMessage = "Error finding matching recipes: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Recherche manuelle via la barre de recherche
    fun searchRecipes(query: String = "") {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = withContext(Dispatchers.IO) {
                    TheMealDbClient.apiService.searchMeals(query)
                }
                val meals = response.meals ?: emptyList()
                allRecipesCache = meals.map { it.toDomainModel() }

                // Si recherche manuelle, on repasse en mode ALL pour voir les résultats de la recherche
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

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        // Recherche API si plus de 2 lettres, sinon filtrage local
        if (query.length > 2) {
            searchRecipes(query)
        } else {
            applyFilters()
        }
    }

    fun onFilterSelected(filter: RecipeFilter) {
        selectedFilter = filter
        applyFilters()
    }

    private fun applyFilters() {
        var result = allRecipesCache

        // 1. Filtre par Texte
        if (searchQuery.isNotBlank() && result.size > 1) {
            result = result.filter { recipe ->
                recipe.title.contains(searchQuery, ignoreCase = true)
            }
        }

        // 2. Filtres Catégories
        result = when (selectedFilter) {
            RecipeFilter.ALL -> result
            RecipeFilter.FAST -> result.filter { it.duration <= 30 }
            RecipeFilter.VEGGIE -> result.filter {
                it.description?.contains("Vegetarian", true) == true ||
                        it.ingredients?.any { ing -> ing.name?.contains("Vegetable", true) == true } == true
            }
            RecipeFilter.FRIDGE -> {
                // Ici on affiche la liste déjà triée par pertinence
                if (matchingRecipes.isNotEmpty()) matchingRecipes else emptyList()
            }
        }
        visibleRecipes = result
    }

    // --- CŒUR DU SYSTÈME DE RECOMMANDATION ---
    fun updateMatchingRecipes(userIngredients: List<IngredientData>) {
        val all = allRecipesCache

        if (all.isEmpty() || userIngredients.isEmpty()) {
            matchingRecipes.clear()
            return
        }

        // On calcule un score pour chaque recette
        val sorted = all.map { recipe ->
            // Score = Nombre d'ingrédients de la recette présents dans ton frigo
            val matchCount = recipe.ingredients?.count { recipeIng ->
                userIngredients.any { userIng ->
                    // Comparaison souple ("Tomato" matche "Tomatoes")
                    val rName = recipeIng.name ?: ""
                    val uName = userIng.name

                    rName.contains(uName, ignoreCase = true) || uName.contains(rName, ignoreCase = true)
                }
            } ?: 0

            // On retourne la recette avec son score
            Pair(recipe, matchCount)
        }
            // On ne garde que celles qui ont au moins 1 ingrédient en commun
            .filter { it.second > 0 }
            // TRI CRUCIAL : Les plus gros scores d'abord !
            .sortedByDescending { it.second }
            .map { it.first }

        matchingRecipes.clear()
        matchingRecipes.addAll(sorted)

        // Si on est déjà sur l'onglet FRIDGE, on met à jour l'affichage immédiatement
        if (selectedFilter == RecipeFilter.FRIDGE) {
            visibleRecipes = matchingRecipes
        }
    }
}