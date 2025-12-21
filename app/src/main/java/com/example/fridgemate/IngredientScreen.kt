package com.example.fridgemate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun IngredientScreen(userId: String) {
    // Réutilisation du ViewModel existant pour charger et gérer les données
    val viewModel: DashboardViewModel = viewModel()
    val state = viewModel.uiState

    var showAddDialog by remember { mutableStateOf(false) }

    // Chargement des données à l'ouverture
    LaunchedEffect(userId) {
        viewModel.loadData(userId)
    }

    Scaffold(
        containerColor = WebBg, // Le même fond gris que le Dashboard
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = WebGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // Titre "My Ingredients" (comme sur le screen web)
            Text(
                "My Ingredients",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WebTextDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (state) {
                is DashboardUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WebGreen)
                    }
                }
                is DashboardUiState.Error -> {
                    Text("Error: ${state.message}", color = WebRed)
                }
                is DashboardUiState.Success -> {
                    val ingredients = state.ingredients

                    if (ingredients.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Fridge is empty!", color = WebTextGray)
                        }
                    } else {
                        // GRILLE À 2 COLONNES (Style Mobile fidèle au Web)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp) // Espace pour le FAB
                        ) {
                            items(ingredients) { ingredient ->
                                IngredientGridCard(
                                    ingredient = ingredient,
                                    onDelete = { viewModel.deleteIngredient(ingredient.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Réutilisation EXACTE de ton dialogue d'ajout
    if (showAddDialog) {
        AddIngredientDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, qty, date ->
                viewModel.addIngredient(name, qty, date)
                showAddDialog = false
            }
        )
    }
}

// --- LE COMPOSANT CARTE (Fidèle au screenshot) ---
@Composable
fun IngredientGridCard(ingredient: IngredientData, onDelete: () -> Unit) {
    // 1. Calcul de l'état (Périmé ou pas) grâce à la fonction existante
    val daysRemaining = getDaysRemaining(ingredient.expiryDate)
    val isExpired = daysRemaining != null && daysRemaining < 0

    // 2. Définition des couleurs selon l'état (Rouge si périmé, Blanc sinon)
    val cardBgColor = if (isExpired) WebRed else Color.White
    val textColor = if (isExpired) Color.White else WebTextDark
    val subTextColor = if (isExpired) Color.White.copy(alpha = 0.9f) else WebTextGray

    // Couleur des petits tags (unité/catégorie)
    val tagBgColor = if (isExpired) Color.White.copy(alpha = 0.2f) else WebBg
    val tagTextColor = if (isExpired) Color.White else WebTextGray

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // Ligne du haut : Image + Titre + Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Cercle icône (comme sur le screen)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isExpired) Color.White.copy(alpha = 0.3f) else WebBg),
                        contentAlignment = Alignment.Center
                    ) {
                        // Si tu as des URLs d'images, utilise AsyncImage ici. Sinon, icône par défaut.
                        Icon(
                            Icons.Outlined.Kitchen,
                            contentDescription = null,
                            tint = if (isExpired) Color.White else WebTextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = ingredient.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor,
                            maxLines = 1
                        )
                        if (isExpired) {
                            Text(
                                "Expired",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Bouton suppression discret
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = subTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ligne des Tags (Quantité, Catégorie)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Tag Quantité
                val qtyDisplay = "${ingredient.quantity ?: ""} ${ingredient.unit ?: ""}".trim()
                if (qtyDisplay.isNotBlank()) {
                    TagChip(qtyDisplay, tagBgColor, tagTextColor)
                }
                // Tag Catégorie
                ingredient.category?.let { cat ->
                    TagChip(cat, tagBgColor, tagTextColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date d'expiration en bas
            val dateDisplay = ingredient.expiryDate?.split("T")?.get(0) ?: "?"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Expires:",
                    fontSize = 11.sp,
                    color = subTextColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = dateDisplay,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

// Petit composant helper pour les étiquettes (Tags)
@Composable
fun TagChip(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}