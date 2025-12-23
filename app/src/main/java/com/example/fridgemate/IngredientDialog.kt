package com.example.fridgemate

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.QrCodeScanner // L'icône du scanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel // Nécessaire pour le scan
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDialog(
    ingredientToEdit: IngredientData? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    // On récupère le ViewModel ici juste pour la fonction de Scan
    val viewModel: DashboardViewModel = viewModel()

    // --- ÉTATS DU FORMULAIRE ---
    var name by remember { mutableStateOf(ingredientToEdit?.name ?: "") }
    var quantity by remember { mutableStateOf(ingredientToEdit?.quantity ?: "") }

    // Unité (Dropdown)
    var unit by remember { mutableStateOf(ingredientToEdit?.unit ?: "") }
    var expandedUnit by remember { mutableStateOf(false) }
    val units = listOf("kg", "g", "L", "ml", "pcs", "pack", "can", "bottle")

    // Date
    var date by remember { mutableStateOf(ingredientToEdit?.expiryDate?.split("T")?.get(0) ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Catégorie
    var category by remember { mutableStateOf(ingredientToEdit?.category ?: "Other") }
    var expandedCategory by remember { mutableStateOf(false) }
    val categories = listOf("Vegetable", "Fruit", "Meat", "Dairy", "Grain", "Drink", "Other")

    // --- ÉTATS DU SCANNER ---
    var showScanner by remember { mutableStateOf(false) }

    // Permission Caméra
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
    }

    // --- 1. POPUP DU SCANNER (Plein écran) ---
    if (showScanner) {
        Dialog(onDismissRequest = { showScanner = false }) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                // Assure-toi d'avoir ta fonction BarcodeScanner disponible dans le projet
                BarcodeScanner(onBarcodeScanned = { barcode ->
                    showScanner = false
                    // Appel API pour trouver le produit
                    viewModel.fetchProductInfo(barcode) { foundName, foundQty ->
                        name = foundName
                        // On essaie de séparer la quantité et l'unité si possible (ex: "500 g")
                        if (foundQty.isNotBlank()) {
                            val parts = foundQty.split(" ")
                            if (parts.size >= 2) {
                                quantity = parts[0]
                                // On essaie de matcher l'unité, sinon on met tout dans qty
                                val potentialUnit = parts[1]
                                if (units.contains(potentialUnit)) {
                                    unit = potentialUnit
                                }
                            } else {
                                quantity = foundQty
                            }
                        }
                    }
                })

                // Bouton Fermer le scanner
                IconButton(
                    onClick = { showScanner = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
    // --- 2. POPUP DATE PICKER ---
    else if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        date = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK", color = WebGreen) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
    // --- 3. FORMULAIRE PRINCIPAL ---
    else {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = WebCardBg,
            title = {
                Text(
                    text = if (ingredientToEdit == null) "Add Item" else "Edit Item",
                    color = WebTextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // --- NOM + SCANNER ---
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = WebGreen)
                        }
                    }

                    // --- QUANTITÉ + UNITÉ ---
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Quantité
                        OutlinedTextField(
                            value = quantity, onValueChange = { quantity = it },
                            label = { Text("Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Unité (Dropdown Réparé avec ExposedDropdownMenuBox)
                        ExposedDropdownMenuBox(
                            expanded = expandedUnit,
                            onExpandedChange = { expandedUnit = !expandedUnit },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {}, // ReadOnly
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                                modifier = Modifier.menuAnchor() // CRUCIAL
                            )
                            ExposedDropdownMenu(
                                expanded = expandedUnit,
                                onDismissRequest = { expandedUnit = false }
                            ) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(if (u.isEmpty()) "None" else u) },
                                        onClick = {
                                            unit = u
                                            expandedUnit = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // --- CATÉGORIE ---
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        category = c
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }

                    // --- DATE ---
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        label = { Text("Expiry Date") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }, // Fallback click
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = WebGreen)
                            }
                        },
                        // Permet d'ouvrir le calendrier même en cliquant dans le champ texte
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
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
                    onClick = {
                        if (name.isNotBlank()) onConfirm(name, quantity, unit, date, category)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WebGreen)
                ) {
                    Text(if (ingredientToEdit == null) "Add" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel", color = WebTextGray) }
            }
        )
    }
}