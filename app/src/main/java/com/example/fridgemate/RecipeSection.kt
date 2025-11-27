// RecipeScreen.kt
package com.example.fridgemate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen() {
    Column(modifier = Modifier.fillMaxSize().background(BgColor).padding(16.dp)) {
        // Barre de recherche
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher une recette...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filtres rapides (Chips)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = { }, label = { Text("Tout") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenLight, selectedLabelColor = GreenPrimary))
            FilterChip(selected = false, onClick = { }, label = { Text("Rapide") })
            FilterChip(selected = false, onClick = { }, label = { Text("Végé") })
            FilterChip(selected = false, onClick = { }, label = { Text("Frigo") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grille de recettes
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Espace pour le FAB
        ) {
            items(6) { index -> // Génère 6 fausses recettes
                RecipeCardItem(index)
            }
        }
    }
}

@Composable
fun RecipeCardItem(index: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Placeholder Image
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.LightGray),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Favori", tint = Color.White)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Recette Exemple ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("25 min", fontSize = 12.sp, color = TextGray)
                    Text("${index} manquants", fontSize = 12.sp, color = if(index == 0) GreenPrimary else Color(0xFFE65100))
                }
            }
        }
    }
}