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
import androidx.compose.material.icons.filled.QrCodeScanner // Import icone
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
                    // 1. Alerts
                    if (state.alerts.isNotEmpty()) {
                        AlertSection(state.alerts)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 2. Fridge Summary (Traduit)
                    FridgeSummarySection(state.ingredients.size)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. My Fridge List (NOUVELLE SECTION)
                    Text("My Fridge Content", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    state.ingredients.forEach { ingredient ->
                        IngredientItem(
                            ingredient = ingredient,
                            onDelete = { viewModel.deleteIngredient(ingredient.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Espace vide pour que le FAB ne cache pas le dernier élément
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
fun FridgeSummarySection(totalCount: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Fridge Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("$totalCount", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("ingredients", color = TextDark)
                }

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 0.7f },
                        modifier = Modifier.size(70.dp),
                        color = GreenPrimary,
                        trackColor = GreenLight,
                        strokeWidth = 6.dp,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Full", fontSize = 10.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                        Text("70%", fontSize = 10.sp, color = GreenPrimary)
                    }
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

