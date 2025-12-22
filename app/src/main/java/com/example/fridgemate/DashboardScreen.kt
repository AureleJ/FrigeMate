package com.example.fridgemate

import android.Manifest
import android.util.Size // Attention à cet import pour CameraX si utilisé, sinon android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.DateRange // Pour l'icône calendrier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(userId: String) {
    val viewModel: DashboardViewModel = viewModel()
    val state = viewModel.uiState

    // État pour ouvrir/fermer le dialogue d'ajout
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadData(userId)
    }

    Scaffold(
        containerColor = WebBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- EN-TÊTE AVEC BOUTON D'AJOUT ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WebTextDark
                )

                // Petit bouton "+" discret en haut (remplace le FAB)
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(WebGreen, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }

            when (state) {
                is DashboardUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WebGreen)
                    }
                }
                is DashboardUiState.Error -> {
                    ErrorCard(state.message) { viewModel.loadData(userId) }
                }
                is DashboardUiState.Success -> {
                    // Calculs
                    val ingredients = state.ingredients
                    val totalCount = ingredients.size
                    var expiredCount = 0
                    var expiringSoonCount = 0

                    ingredients.forEach { ing ->
                        val days = getDaysRemaining(ing.expiryDate)
                        if (days != null) {
                            if (days < 0) expiredCount++
                            else if (days <= 3) expiringSoonCount++
                        }
                    }

                    val categoryDistribution = ingredients
                        .groupingBy { it.category?.replaceFirstChar { c -> c.uppercase() } ?: "General" }
                        .eachCount()

                    // Affichage des Sections
                    if (state.alerts.isNotEmpty()) {
                        AlertSection(state.alerts)
                    }

                    FridgeSummarySection(
                        totalCount = totalCount,
                        expiringSoonCount = expiringSoonCount,
                        expiredCount = expiredCount,
                        categoryDistribution = categoryDistribution
                    )

                    // Liste rapide
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Recent Items", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = WebTextDark)

                        if (ingredients.isEmpty()) {
                            Text("Your fridge is empty.", color = WebTextGray)
                        } else {
                            ingredients.take(5).forEach { ingredient ->
                                IngredientItem(
                                    ingredient = ingredient,
                                    onDelete = { viewModel.deleteIngredient(ingredient.id) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // --- DIALOGUE D'AJOUT (Restauré) ---
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

@OptIn(ExperimentalMaterial3Api::class) // Nécessaire pour le DatePicker
@Composable
fun AddIngredientDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    val viewModel: DashboardViewModel = viewModel()

    // États du formulaire
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") } // La date stockée en String (YYYY-MM-DD)

    // États pour le Scanner et le Calendrier
    var showScanner by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // État du DatePicker Material 3
    val datePickerState = rememberDatePickerState()

    // Permission Caméra (inchangé)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
    }

    // --- 1. GESTION DU CALENDRIER (DatePicker) ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // On récupère la date sélectionnée (en millisecondes)
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = convertMillisToDate(millis)
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = WebGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = WebTextGray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- 2. GESTION DU SCANNER (inchangé) ---
    if (showScanner) {
        Dialog(onDismissRequest = { showScanner = false }) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                BarcodeScanner(onBarcodeScanned = { barcode ->
                    showScanner = false
                    viewModel.fetchProductInfo(barcode) { foundName, foundQty ->
                        name = foundName
                        if (foundQty.isNotBlank()) quantity = foundQty
                    }
                })
                IconButton(
                    onClick = { showScanner = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    } else {
        // --- 3. LE FORMULAIRE ---
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = WebCardBg,
            title = { Text("Add Item", color = WebTextDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Champ Nom + Scan
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = WebGreen)
                        }
                    }

                    // Champ Quantité
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity (e.g. 2 L)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Champ Date (Modifié pour ouvrir le calendrier)
                    OutlinedTextField(
                        value = date,
                        onValueChange = { }, // On empêche l'écriture manuelle directe ici
                        label = { Text("Expiry Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true, // Le clavier ne s'ouvrira pas
                        singleLine = true,
                        trailingIcon = {
                            // Clic sur l'icône calendrier
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = WebGreen)
                            }
                        },
                        // On détecte aussi le clic sur le champ lui-même (InteractionSource serait plus propre mais ceci fonctionne)
                        interactionSource = remember { MutableInteractionSource() }
                            .also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect {
                                        if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                            showDatePicker = true
                                        }
                                    }
                                }
                            }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { if (name.isNotBlank()) onConfirm(name, quantity, date) },
                    colors = ButtonDefaults.buttonColors(containerColor = WebGreen)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel", color = WebTextGray) }
            }
        )
    }
}

// --- FONCTION UTILITAIRE (À ajouter en bas du fichier ou hors de la classe) ---
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Composable
fun AlertSection(alerts: List<IngredientAlert>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WebCardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).background(WebOrange.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = WebOrange, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Expiring Soon", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WebTextDark)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alerts.take(3).forEach { alert ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(WebRed))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = alert.name, fontWeight = FontWeight.Medium, color = WebTextDark)
                        }
                        val daysText = if (alert.daysRemaining < 0) "Expired" else "${alert.daysRemaining} days"
                        Text(text = daysText, color = if(alert.daysRemaining < 0) WebRed else WebOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = WebBg, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun FridgeSummarySection(
    totalCount: Int, expiringSoonCount: Int, expiredCount: Int, categoryDistribution: Map<String, Int>
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebTextDark)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OverviewStatCard("Items", totalCount.toString(), Modifier.weight(1f))
                OverviewStatCard("Soon", expiringSoonCount.toString(), Modifier.weight(1f))
                OverviewStatCard("Expired", expiredCount.toString(), Modifier.weight(1f), isDanger = expiredCount > 0)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Distribution", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WebTextDark)
            Card(
                colors = CardDefaults.cardColors(containerColor = WebCardBg),
                elevation = CardDefaults.cardElevation(1.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (categoryDistribution.isNotEmpty()) DonutChartWithLegend(categoryDistribution)
                    else Text("No data yet", color = WebTextGray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun OverviewStatCard(label: String, count: String, modifier: Modifier = Modifier, isDanger: Boolean = false) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = WebCardBg),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = count, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (isDanger) WebRed else WebTextDark)
            Text(text = label, fontSize = 12.sp, color = WebTextGray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DonutChartWithLegend(data: Map<String, Int>) {
    val chartColors = listOf(Color(0xFFFF8A65), Color(0xFF00C853), Color(0xFFFFD600), Color(0xFF2979FF), Color(0xFFAA00FF))
    val total = data.values.sum().toFloat()
    val proportions = data.values.map { it / total }
    val labels = data.keys.toList()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 35f
                var startAngle = -90f
                proportions.forEachIndexed { index, fraction ->
                    val sweepAngle = fraction * 360f
                    drawArc(
                        color = chartColors[index % chartColors.size],
                        startAngle = startAngle + 2f,
                        sweepAngle = sweepAngle - 2f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = androidx.compose.ui.geometry.Size(size.width, size.height),
                        topLeft = Offset(0f, 0f)
                    )
                    startAngle += sweepAngle
                }
            }
            Text(text = "${total.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = WebTextDark)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEachIndexed { index, label ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(chartColors[index % chartColors.size], CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, fontSize = 12.sp, color = WebTextGray)
                    Text(text = " (${data[label]})", fontSize = 12.sp, color = WebTextDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun IngredientItem(ingredient: IngredientData, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WebCardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(ingredient.name, fontWeight = FontWeight.SemiBold, color = WebTextDark)
                val dateText = ingredient.expiryDate?.split("T")?.get(0) ?: "-"
                Text("Exp: $dateText", fontSize = 12.sp, color = WebTextGray)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WebTextGray, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = WebRed.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Error", color = WebRed, fontWeight = FontWeight.Bold)
            Text(message, fontSize = 12.sp, color = WebTextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = WebRed)) { Text("Retry") }
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
    } catch (e: Exception) { null }
}