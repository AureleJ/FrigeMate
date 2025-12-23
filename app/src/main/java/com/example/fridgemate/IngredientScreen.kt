package com.example.fridgemate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(userId: String) {
    val viewModel: DashboardViewModel = viewModel()
    val state = viewModel.uiState

    // États locaux
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var editingIngredient by remember { mutableStateOf<IngredientData?>(null) }

    LaunchedEffect(userId) {
        viewModel.loadData(userId)
    }

    Scaffold(
        containerColor = WebBg,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->

        // --- CHANGEMENT MAJEUR ICI : LazyColumn est la racine ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // On applique le padding système ici
            contentPadding = PaddingValues(20.dp), // Marge interne globale pour le contenu
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espace vertical entre CHAQUE élément (titre, search, cards...)
        ) {

            // 1. TITRE (C'est maintenant un item qui scrolle)
            item {
                Text(
                    text = "My Pantry",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WebTextDark
                )
            }

            // 2. BARRE DE RECHERCHE
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search ingredients...") },
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
            }

            // 3. FILTRES (LazyRow imbriquée dans un item)
            item {
                val categories = listOf("All", "Vegetable", "Fruit", "Meat", "Dairy", "Grain", "Other")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WebGreen,
                                selectedLabelColor = Color.White,
                                containerColor = WebCardBg,
                                labelColor = WebTextGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent,
                                enabled = true,
                                selected = true,
                            ),
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            // 4. CONTENU (Chargement / Erreur / Liste)
            when (state) {
                is DashboardUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = WebGreen)
                        }
                    }
                }
                is DashboardUiState.Error -> {
                    item { Text("Error loading data", color = WebRed) }
                }
                is DashboardUiState.Success -> {
                    // Calculs de filtre
                    val filteredList = state.ingredients.filter { ing ->
                        (ing.name.contains(searchQuery, ignoreCase = true)) &&
                                (selectedCategory == "All" || (ing.category?.equals(selectedCategory, ignoreCase = true) == true))
                    }.sortedBy { ing ->
                        ing.expiryDate ?: "9999-99-99"
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Kitchen, contentDescription = null, tint = WebTextGray, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No ingredients found", color = WebTextGray)
                                }
                            }
                        }
                    } else {
                        // --- LES ITEMS DE LA LISTE ---
                        // Note : on utilise items() directement ici, pas besoin de LazyColumn imbriquée
                        items(filteredList) { ingredient ->
                            IngredientCardFull(
                                ingredient = ingredient,
                                onClick = { editingIngredient = ingredient },
                                onDelete = { viewModel.deleteIngredient(ingredient.id) }
                            )
                        }

                        // Petit espace vide en bas pour que le dernier item ne soit pas collé au bord ou caché par une navbar
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // --- DIALOGUE (Reste en dehors du scroll pour s'afficher par dessus) ---
        if (editingIngredient != null) {
            IngredientDialog(
                ingredientToEdit = editingIngredient,
                onDismiss = { editingIngredient = null },
                onConfirm = { name, qty, unit, date, category ->
                    viewModel.updateIngredient(editingIngredient!!.id, name, qty, unit, date, category)
                    editingIngredient = null
                }
            )
        }
    }
}

// ... (Garde tes fonctions IngredientCardFull, getDaysRemainingInt, getCategoryIcon inchangées en dessous) ...
// Si tu veux je peux te les remettre ici, mais c'est exactement le même code qu'avant pour ces parties là.
@Composable
fun IngredientCardFull(
    ingredient: IngredientData,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val daysRemaining = getDaysRemainingInt(ingredient.expiryDate)
    val (statusColor, statusText, statusBg) = when {
        daysRemaining == null -> Triple(WebTextGray, "No Date", WebBg)
        daysRemaining < 0 -> Triple(WebRed, "Expired", WebRed.copy(alpha = 0.1f))
        daysRemaining <= 3 -> Triple(WebOrange, "$daysRemaining days left", WebOrange.copy(alpha = 0.1f))
        else -> Triple(WebGreen, "$daysRemaining days left", WebGreen.copy(alpha = 0.1f))
    }
    val icon = getCategoryIcon(ingredient.category)

    Card(
        colors = CardDefaults.cardColors(containerColor = WebCardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (daysRemaining != null && daysRemaining < 0) BorderStroke(1.dp, WebRed.copy(alpha = 0.5f)) else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(WebBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = WebTextDark, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (daysRemaining != null && daysRemaining < 0) WebTextGray else WebTextDark,
                    textDecoration = if (daysRemaining != null && daysRemaining < 0) TextDecoration.LineThrough else null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${ingredient.quantity ?: "-"} ${ingredient.unit ?: ""}", fontSize = 13.sp, color = WebTextGray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.background(statusBg, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(text = statusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = WebTextGray.copy(alpha = 0.5f))
            }
        }
    }
}

fun getDaysRemainingInt(dateString: String?): Long? {
    if (dateString.isNullOrEmpty()) return null
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cleanDate = if (dateString.contains("T")) dateString.split("T")[0] else dateString
        val date = format.parse(cleanDate) ?: return null
        val diff = date.time - System.currentTimeMillis()
        TimeUnit.MILLISECONDS.toDays(diff)
    } catch (e: Exception) { null }
}

fun getCategoryIcon(category: String?): ImageVector {
    return when (category?.lowercase()) {
        "vegetable", "legumes" -> Icons.Default.Eco
        "fruit", "fruits" -> Icons.Default.EmojiNature
        "meat", "viande" -> Icons.Default.Restaurant
        "dairy", "laitier" -> Icons.Default.Egg
        "drink", "boisson" -> Icons.Default.LocalDrink
        "grain" -> Icons.Default.Grass
        "freezer" -> Icons.Default.AcUnit
        else -> Icons.Default.Kitchen
    }
}