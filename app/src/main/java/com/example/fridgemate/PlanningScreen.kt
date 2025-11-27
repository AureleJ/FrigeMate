// PlanningScreen.kt
package com.example.fridgemate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlanningScreen() {
    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // Sélecteur de semaine (Simplifié)
        Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Semaine du 25 au 31 Mars", fontWeight = FontWeight.Bold, color = TextDark)
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            val days = listOf("Lundi 25", "Mardi 26", "Mercredi 27", "Jeudi 28")
            items(days.size) { index ->
                DayPlanningCard(dayTitle = days[index], isToday = index == 0)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun DayPlanningCard(dayTitle: String, isToday: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        // Bordure verte si c'est aujourd'hui
        border = if (isToday) androidx.compose.foundation.BorderStroke(2.dp, GreenPrimary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(dayTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if(isToday) GreenPrimary else TextDark)
            Spacer(modifier = Modifier.height(12.dp))

            // Créneau Midi
            MealSlot(type = "Déjeuner", mealName = "Salade César Poulet", isPlanned = true)
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = BgColor)
            // Créneau Soir
            MealSlot(type = "Dîner", mealName = null, isPlanned = false)
        }
    }
}

@Composable
fun MealSlot(type: String, mealName: String?, isPlanned: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(type, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextGray, modifier = Modifier.width(80.dp))
        if (isPlanned) {
            Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(mealName ?: "", fontWeight = FontWeight.Medium, color = TextDark)
        } else {
            // Bouton pour ajouter
            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("Ajouter +", fontSize = 12.sp)
            }
        }
    }
}