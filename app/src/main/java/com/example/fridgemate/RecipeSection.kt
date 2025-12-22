//RecipeSection.kt
package com.example.fridgemate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(userId: String) {
    val recipeViewModel: RecipeViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()

    // États
    var selectedRecipe by remember { mutableStateOf<RecipeData?>(null) }
    val dashboardState = dashboardViewModel.uiState

    // On garde matchingRecipes pour l'affichage
    val matchingRecipes = recipeViewModel.matchingRecipes

    // Variable pour éviter de recharger en boucle à chaque petit changement
    var dataLoaded by remember { mutableStateOf(false) }

    // 1. Charger les ingrédients du frigo
    LaunchedEffect(userId) {
        dashboardViewModel.loadData(userId)
    }

    // 2. MODIFICATION ICI : Dès que le frigo est chargé, on cherche les recettes
    LaunchedEffect(dashboardState) {
        if (!dataLoaded && dashboardState is DashboardUiState.Success) {
            // On envoie les ingrédients du frigo au ViewModel des recettes
            recipeViewModel.loadRecipesFromFridge(dashboardState.ingredients)
            dataLoaded = true
        }
    }

    Scaffold(
        containerColor = WebBg,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (selectedRecipe != null) {
                // --- MODE DÉTAIL ---
                RecipeDetailView(
                    recipe = selectedRecipe!!,
                    onBack = { selectedRecipe = null }
                )
            } else {
                // --- MODE LISTE (GRILLE INTELLIGENTE) ---
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {

                    // A. Barre de Recherche + Titre (Prend toute la largeur)
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            Text("Recipes", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = WebTextDark)
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = recipeViewModel.searchQuery,
                                onValueChange = { recipeViewModel.onSearchQueryChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search (e.g. Pasta, Beef...)") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WebTextGray) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = WebCardBg,
                                    unfocusedContainerColor = WebCardBg,
                                    focusedBorderColor = WebGreen,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // B. Section "Cook with what you have" (Si dispo)
                    if (matchingRecipes.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WebGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cook with what you have", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebTextDark)
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Carrousel Horizontal
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(matchingRecipes) { recipe ->
                                        MatchingRecipeCard(recipe) { selectedRecipe = recipe }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }

                    // C. Filtres (Prend toute la largeur)
                    item(span = { GridItemSpan(2) }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChipItem("All", RecipeFilter.ALL, recipeViewModel)
                            FilterChipItem("Fast (<30m)", RecipeFilter.FAST, recipeViewModel)
                            FilterChipItem("Veggie", RecipeFilter.VEGGIE, recipeViewModel)
                        }
                    }

                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All Recipes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebTextDark)
                    }

                    // D. Grille des recettes (Standard)
                    if (recipeViewModel.isLoading) {
                        item(span = { GridItemSpan(2) }) {
                            Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = WebGreen)
                            }
                        }
                    } else if (recipeViewModel.visibleRecipes.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Text("No recipes found matching criteria.", color = WebTextGray, modifier = Modifier.padding(top = 20.dp))
                        }
                    } else {
                        items(recipeViewModel.visibleRecipes) { recipe ->
                            RecipeCardItem(
                                recipe = recipe,
                                onClick = { selectedRecipe = recipe }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- CARTE HORIZONTALE (Cook with what you have) ---
@Composable
fun MatchingRecipeCard(recipe: RecipeData, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WebGreen),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(280.dp) // Un peu plus large pour afficher les infos
            .height(150.dp) // Un peu plus haut
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // 1. IMAGE (CORRIGÉE : Utilise AsyncImage maintenant)
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                if (!recipe.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(recipe.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback si pas d'image
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Timer, contentDescription = null, tint = Color.White)
                    }
                }
            }

            // 2. INFOS + COMPTEURS
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Perfect Match!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WebGreen, // Texte vert sur fond blanc
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = recipe.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                // Ligne des statistiques (Temps + Ingrédients)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Temps
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                    Text("${recipe.duration}m", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)

                    Spacer(modifier = Modifier.width(12.dp))

                    // Ingrédients (Check Vert / Croix Rouge)
                    // On a
                    Text("✅ ${recipe.matchingCount}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    // Manque
                    Text("❌ ${recipe.missingCount}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Normal)
                }
            }
        }
    }
}
// --- CARTE VERTICALE CLASSIQUE (Améliorée) ---
@Composable
fun RecipeCardItem(recipe: RecipeData, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WebCardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (!recipe.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(recipe.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = WebTextGray)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = recipe.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, // Légèrement réduit pour éviter que ça coupe
                    color = WebTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gauche : Temps
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = WebTextGray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${recipe.duration} min", fontSize = 12.sp, color = WebTextGray)
                    }

                    // Droite : Ingrédients (Style Emoji)
                    // On affiche dès qu'on a fait le calcul (donc même si 0 match, on peut vouloir afficher les manquants)
                    // Mais pour garder le design propre, affichons-le si on a au moins 1 match OU si on est sur la page

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // ✅ Ce qu'on a
                        Text(
                            text = "✅ ${recipe.matchingCount}",
                            fontSize = 12.sp,
                            color = WebTextDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // ❌ Ce qu'il manque
                        Text(
                            text = "❌ ${recipe.missingCount}",
                            fontSize = 12.sp,
                            color = WebTextGray, // Un peu plus discret en gris
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
// --- VUE DÉTAILLÉE (Nettoyée pour le style Web) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailView(recipe: RecipeData, onBack: () -> Unit) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp), // Ignore les marges système globales
        containerColor = WebBg
    ) { paddingValues ->
        // On utilise une Box pour pouvoir empiler le bouton SUR l'image
        Box(
            modifier = Modifier
                .fillMaxSize()
                // On applique le padding seulement en bas (pour la barre de nav)
                // et pas en haut, pour que l'image monte tout en haut.
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {

            // COUCHE 1 : Le contenu défilant (Image + Texte)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Grande Image (qui touche maintenant le haut de l'écran)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Color.LightGray) // Couleur de fond pendant le chargement
                ) {
                    if (!recipe.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(recipe.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = recipe.title,
                            contentScale = ContentScale.Crop, // Important pour remplir la zone
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback si pas d'image
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No Image", color = Color.Gray)
                        }
                    }
                }

                // Le reste du contenu (Titre, ingrédients...)
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header
                    Text(recipe.title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = WebTextDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = WebGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${recipe.prep_time_min}m prep  •  ${recipe.cook_time_min}m cook", fontWeight = FontWeight.Medium, color = WebTextGray)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Carte Ingrédients
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WebCardBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Ingredients", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebTextDark)
                            Spacer(modifier = Modifier.height(12.dp))

                            recipe.ingredients?.forEach { ing ->
                                Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(WebGreen, CircleShape))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    val qty = if (!ing.quantity.isNullOrEmpty()) "${ing.quantity} ${ing.unit ?: ""} " else ""
                                    Text(
                                        text = "$qty${ing.name}",
                                        fontSize = 15.sp,
                                        color = WebTextDark
                                    )
                                }
                                Divider(color = WebBg, thickness = 1.dp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Instructions
                    Text("Instructions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebTextDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    recipe.instructions?.forEachIndexed { index, step ->
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text("${index + 1}.", fontWeight = FontWeight.Bold, color = WebGreen, modifier = Modifier.width(24.dp))
                            Text(step, fontSize = 15.sp, color = WebTextGray, lineHeight = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // COUCHE 2 : Le bouton retour (Flottant par-dessus)
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 45.dp, start = 20.dp) // Marge manuelle pour éviter la barre de statut
                    .background(Color.White, CircleShape)
                    .align(Alignment.TopStart) // Ancré en haut à gauche
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WebTextDark)
            }
        }
    }
}

// Helper filtres (inchangé mais stylisé)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipItem(label: String, filter: RecipeFilter, viewModel: RecipeViewModel) {
    val isSelected = viewModel.selectedFilter == filter
    FilterChip(
        selected = isSelected,
        onClick = { viewModel.onFilterSelected(filter) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = WebGreen,
            selectedLabelColor = Color.White,
            containerColor = WebCardBg,
            labelColor = WebTextGray
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if(isSelected) WebGreen else Color.Transparent
        )
    )
}