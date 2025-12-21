package com.example.fridgemate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Warning
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.QrCodeScanner // Import icone
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog // Import Dialog pour le scanner full screen

@Composable
fun DashboardScreen(userId: String) {
    val viewModel: DashboardViewModel = viewModel()

    // État pour la boite de dialogue d'ajout
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadData(userId)
    }

    val state = viewModel.uiState

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GreenPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (state) {
                is DashboardUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
                is DashboardUiState.Error -> {
                    ErrorCard(state.message) { viewModel.loadData(userId) }
                }
                is DashboardUiState.Success -> {
                    // --- 1. CALCUL DES STATS (Front-End) ---
                    val ingredients = state.ingredients
                    val totalCount = ingredients.size

                    // On analyse chaque ingrédient pour voir s'il est périmé
                    var expiredCount = 0
                    var expiringSoonCount = 0

                    ingredients.forEach { ing ->
                        val days = getDaysRemaining(ing.expiryDate)
                        if (days != null) {
                            if (days < 0) expiredCount++      // Périmé
                            else if (days <= 3) expiringSoonCount++ // Bientôt (<= 3 jours)
                        }
                    }

                    // On groupe les ingrédients par catégorie pour le graphique
                    // Ex: { "Meat": 2, "Dairy": 5, "General": 1 }
                    val categoryDistribution = ingredients
                        .groupingBy {
                            // On met une majuscule et une valeur par défaut
                            it.category?.replaceFirstChar { char -> char.uppercase() } ?: "General"
                        }
                        .eachCount()

                    // --- 2. AFFICHAGE DES SECTIONS ---

                    // Alertes (inchangé)
                    if (state.alerts.isNotEmpty()) {
                        AlertSection(state.alerts)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Résumé Frigo (NOUVELLE VERSION AVEC LES VARIABLES CALCULÉES)
                    FridgeSummarySection(
                        totalCount = totalCount,
                        expiringSoonCount = expiringSoonCount,
                        expiredCount = expiredCount,
                        categoryDistribution = categoryDistribution
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Liste complète (Ta nouvelle section liste)
                    Text("My Fridge Content", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = WebTextDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    ingredients.forEach { ingredient ->
                        IngredientItem(
                            ingredient = ingredient,
                            onDelete = { viewModel.deleteIngredient(ingredient.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Espace pour le bouton flottant
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Boite de dialogue pour ajouter un ingrédient
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

// --- Composants ---

@Composable
fun IngredientItem(ingredient: IngredientData, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(ingredient.name, fontWeight = FontWeight.Bold, color = TextDark)
                val dateText = ingredient.expiryDate?.split("T")?.get(0) ?: "No date"
                Text("Exp: $dateText • Qty: ${ingredient.quantity ?: "?"}", fontSize = 12.sp, color = TextGray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun AddIngredientDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    // On récupère le ViewModel juste pour appeler fetchProductInfo
    val viewModel: DashboardViewModel = viewModel()

    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    // Gestion du Scanner
    var showScanner by remember { mutableStateOf(false) }

    // Gestion Permission Caméra
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        }
    }

    if (showScanner) {
        // Affichage du scanner en plein écran
        Dialog(onDismissRequest = { showScanner = false }) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                BarcodeScanner(onBarcodeScanned = { barcode ->
                    showScanner = false // Fermer le scanner
                    // Appeler l'API OpenFoodFacts
                    viewModel.fetchProductInfo(barcode) { foundName, foundQty ->
                        name = foundName
                        if (foundQty.isNotBlank()) quantity = foundQty
                    }
                })
                // Bouton pour fermer manuellement
                IconButton(
                    onClick = { showScanner = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Text("X", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // Affichage normal du formulaire
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add to Fridge") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Champ Nom avec bouton Scan
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = GreenPrimary)
                        }
                    }

                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") })
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Expiry (YYYY-MM-DD)") }, placeholder = { Text("2024-12-31") })
                }
            },
            confirmButton = {
                Button(
                    onClick = { if (name.isNotBlank()) onConfirm(name, quantity, date) },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AlertSection(alerts: List<IngredientAlert>) {
    val WarningText = Color(0xFFE65100)
    val AlertDotColor = Color(0xFFFFA726)

    Card(
        // 👇 C'EST ICI : On force la carte à prendre toute la largeur
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Warning, contentDescription = null, tint = WarningText)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Expiring Soon!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            }
            Spacer(modifier = Modifier.height(12.dp))

            alerts.forEach { alert ->
                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AlertDotColor))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = alert.name, fontWeight = FontWeight.SemiBold, color = TextDark)

                    val daysText = if (alert.daysRemaining < 0) "Expired" else "${alert.daysRemaining} days left"
                    Text(text = " ($daysText)", color = WarningText, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FridgeSummarySection(
    totalCount: Int,
    expiringSoonCount: Int,
    expiredCount: Int,
    categoryDistribution: Map<String, Int>
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        // --- PARTIE 1 : FRIDGE OVERVIEW (Les 3 cartes) ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Fridge Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WebTextDark
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // On utilise un poids (weight) de 1f pour qu'elles aient toutes la même largeur
                OverviewStatCard("Items", totalCount.toString(), Modifier.weight(1f))
                OverviewStatCard("Expiring Soon", expiringSoonCount.toString(), Modifier.weight(1f))
                OverviewStatCard("Expired", expiredCount.toString(), Modifier.weight(1f), isDanger = expiredCount > 0)
            }
        }

        // --- PARTIE 2 : CATEGORY DISTRIBUTION (Le Graphique) ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Category Distribution",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WebTextDark
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp), // Ombre légère comme sur le web
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (categoryDistribution.isNotEmpty()) {
                        DonutChartWithLegend(categoryDistribution)
                    } else {
                        Text("No data available", color = WebTextGray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// --- Petite carte carrée pour les stats ---
@Composable
fun OverviewStatCard(label: String, count: String, modifier: Modifier = Modifier, isDanger: Boolean = false) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)), // Gris très clair pour le fond des petites cartes
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(0.dp) // Design "plat"
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDanger) WebRed else WebTextDark
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = WebTextGray,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

// --- Le Graphique Donut et sa Légende ---
@Composable
fun DonutChartWithLegend(data: Map<String, Int>) {
    // Couleurs inspirées de ton image (Orange, Vert, Jaune, Bleu)
    val chartColors = listOf(
        Color(0xFFFF8A65), // Orange (General)
        Color(0xFF00C853), // Vert (Meat)
        Color(0xFFFFD600), // Jaune (Vegetable)
        Color(0xFF2979FF), // Bleu (Dairy)
        Color(0xFFAA00FF)  // Violet (Autre)
    )

    val total = data.values.sum().toFloat()
    val proportions = data.values.map { it / total }
    val labels = data.keys.toList()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 1. DESSIN DU GRAPHIQUE
        Box(
            modifier = Modifier.size(140.dp), // Taille du donut
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 50f // Épaisseur de l'anneau
                var startAngle = -90f // Commencer en haut

                proportions.forEachIndexed { index, fraction ->
                    val sweepAngle = fraction * 360f
                    // Espace entre les segments (optionnel, pour faire joli comme sur l'image)
                    val gapAngle = 2f

                    drawArc(
                        color = chartColors[index % chartColors.size],
                        startAngle = startAngle + (gapAngle / 2),
                        sweepAngle = sweepAngle - gapAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                        size = Size(size.width, size.height),
                        topLeft = Offset(0f, 0f)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. LA LÉGENDE
        // On utilise un FlowRow si dispo, sinon une simple Row avec wrap manuel
        // Ici, une Row simple centrée qui passe à la ligne si besoin (approximatif pour mobile)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEachIndexed { index, label ->
                if (index > 0) Spacer(modifier = Modifier.width(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(chartColors[index % chartColors.size], RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = WebTextGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Loading Error", color = Color.Red, fontWeight = FontWeight.Bold)
            Text(message, fontSize = 12.sp)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Retry") }
        }
    }
}

fun getDaysRemaining(dateString: String?): Long? {
    if (dateString.isNullOrEmpty()) return null
    return try {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cleanDate = if (dateString.contains("T")) dateString.split("T")[0] else dateString
        val date = format.parse(cleanDate) ?: return null
        val diff = date.time - System.currentTimeMillis()
        java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff)
    } catch (e: Exception) {
        null
    }
}