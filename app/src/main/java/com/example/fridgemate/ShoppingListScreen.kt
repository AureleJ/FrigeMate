// ShoppingListScreen.kt
package com.example.fridgemate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShoppingListScreen() {
    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // Header "Ajout rapide"
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = androidx.compose.ui.graphics.RectangleShape,
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Ajouter un article...", color = TextGray, modifier = Modifier.weight(1f))
                IconButton(onClick = { }, modifier = Modifier.background(GreenLight, CircleShape).size(36.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = GreenPrimary)
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Catégorie 1
            item { CategoryHeader("Fruits & Légumes (3)") }
            items(3) { ShoppingItem(name = "Pommes", quantity = "${it + 1} kg", initialChecked = it == 0) }

            // Catégorie 2
            item { Spacer(modifier = Modifier.height(8.dp)); CategoryHeader("Produits Frais (2)") }
            item { ShoppingItem(name = "Lait Entier", quantity = "2L", initialChecked = false) }
            item { ShoppingItem(name = "Œufs Bio", quantity = "x12", initialChecked = true) }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Barre inférieure de résumé
        Surface(shadowElevation = 8.dp, color = Color.White) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("2 articles terminés", fontWeight = FontWeight.Bold, color = TextDark)
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary), shape = RoundedCornerShape(8.dp)) {
                    Text("Ranger dans le frigo")
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GreenPrimary, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun ShoppingItem(name: String, quantity: String, initialChecked: Boolean) {
    var isChecked by remember { mutableStateOf(initialChecked) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = TextGray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium, color = if(isChecked) TextGray else TextDark, textDecoration = if(isChecked) TextDecoration.LineThrough else null)
                Text(quantity, fontSize = 12.sp, color = TextGray)
            }
            IconButton(onClick = { /* Supprimer */ }) {
                Icon(Icons.Outlined.Delete, contentDescription = "Supprimer", tint = TextGray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
        }
    }
}