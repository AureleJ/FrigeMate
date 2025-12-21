package com.example.fridgemate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen() {
    val viewModel: RecipeViewModel = viewModel()

    // État pour savoir si on affiche la liste ou le détail d'une recette
    var selectedRecipe by remember { mutableStateOf<RecipeData?>(null) }

    // Gestion du bouton "Retour" physique du téléphone
    BackHandler(enabled = selectedRecipe != null) {
        selectedRecipe = null
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        if (selectedRecipe != null) {
            // --- MODE DÉTAIL ---
            RecipeDetailView(
                recipe = selectedRecipe!!,
                onBack = { selectedRecipe = null }
            )
        } else {
            // --- MODE LISTE (GRILLE) ---
            Column(modifier = Modifier.padding(16.dp)) {
                // Barre de recherche
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search a recipe...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filtres
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipItem("All", RecipeFilter.ALL, viewModel)
                    FilterChipItem("Fast (<30m)", RecipeFilter.FAST, viewModel)
                    FilterChipItem("Veggie", RecipeFilter.VEGGIE, viewModel)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contenu
                if (viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else if (viewModel.visibleRecipes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No recipes found.", color = TextGray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(viewModel.visibleRecipes) { recipe ->
                            RecipeCardItem(
                                recipe = recipe,
                                onClick = { selectedRecipe = recipe } // Clic déclenche le détail
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- VUE DÉTAILLÉE DE LA RECETTE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailView(recipe: RecipeData, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            // Image Placeholder (Grande)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // Remplacer par AsyncImage plus tard
                Text("Recipe Image", color = Color.Gray)
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Titre et Description
                Text(recipe.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
                if (!recipe.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(recipe.description, fontSize = 14.sp, color = TextGray, fontStyle = FontStyle.Italic)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Infos Temps
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = GreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Prep: ${recipe.prep_time_min ?: 0}m  •  Cook: ${recipe.cook_time_min ?: 0}m",
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ingrédients
                Text("Ingredients", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                recipe.ingredients?.forEach { ing ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("• ", fontWeight = FontWeight.Bold, color = GreenPrimary)
                        // Affiche : "500 g ground beef" ou juste "onion"
                        val qty = if (ing.quantity != null) "${ing.quantity} " else ""
                        val unit = if (ing.unit != null) "${ing.unit} " else ""
                        Text("$qty$unit${ing.name}", fontSize = 15.sp, color = TextDark)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Instructions
                Text("Instructions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                recipe.instructions?.forEachIndexed { index, step ->
                    Row(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text("${index + 1}. ", fontWeight = FontWeight.Bold, color = TextGray)
                        Text(step, fontSize = 15.sp, color = TextDark, lineHeight = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- CARTE DE LA LISTE (MODIFIÉE) ---
@Composable
fun RecipeCardItem(recipe: RecipeData, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Rend la carte cliquable
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // Suppression du bouton favoris ici
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = recipe.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDark,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("${recipe.duration} min", fontSize = 12.sp, color = TextGray)

                    if (recipe.isVegetarian) {
                        Text("Veggie", fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                    } else {
                        Text("${recipe.ingredients?.size ?: 0} ingr.", fontSize = 12.sp, color = TextGray)
                    }
                }
            }
        }
    }
}

// Helper pour les filtres (inchangé)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipItem(label: String, filter: RecipeFilter, viewModel: RecipeViewModel) {
    val isSelected = viewModel.selectedFilter == filter
    FilterChip(
        selected = isSelected,
        onClick = { viewModel.onFilterSelected(filter) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GreenLight,
            selectedLabelColor = GreenPrimary
        )
    )
}