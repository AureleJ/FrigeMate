package com.example.fridgemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// --- Routes ---
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "My Fridge", Icons.Default.Kitchen)
    object Ingredients : Screen("ingredients", "Ingredients", Icons.Filled.Inventory2)
    object Recipes : Screen("recipes", "Recipes", Icons.Default.RestaurantMenu)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(primary = GreenPrimary, background = BgColor)
            ) {
                MainScreenApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    // Titre dynamique selon l'écran
    val currentScreenTitle = when (currentRoute) {
        Screen.Ingredients.route -> Screen.Ingredients.title
        Screen.Recipes.route -> Screen.Recipes.title
        else -> Screen.Dashboard.title
    }

    var userId by remember { mutableStateOf<String?>(null) }

    // AJOUTEZ CE LOG
    println("DEBUG: MainScreenApp lancé. UserId est : $userId")

    if (userId == null) {
        println("DEBUG: Affichage du LoginScreen") // <--- Mouchard
        LoginScreen(onLoginSuccess = { id -> userId = id })
    } else {
        println("DEBUG: Affichage du Scaffold (Connecté)") // <--- Mouchard
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentRoute == Screen.Dashboard.route || currentRoute == null) {
                                Icon(
                                    Icons.Default.Kitchen,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                "FridgeMate $currentScreenTitle",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
                )
            },
            bottomBar = { BottomNavigationBar(navController = navController) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavigationGraph(navController = navController, userId = userId!!)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val screens = listOf(Screen.Dashboard, Screen.Ingredients, Screen.Recipes)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title, fontSize = 10.sp) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenPrimary, selectedTextColor = GreenPrimary, indicatorColor = GreenLight
                )
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, userId: String) {
    NavHost(navController, startDestination = Screen.Dashboard.route) {

        // 1. DASHBOARD (Directement ici car pas de fichier DashboardScreen.kt dans ton image)
        composable(Screen.Dashboard.route) {
            DashboardScreen(userId = userId)
        }

        composable(Screen.Ingredients.route) {
            IngredientScreen(
                userId = userId
            )
        }

        // 2. RECETTES (Pointe vers ton fichier RecipeSection.kt)
        composable(Screen.Recipes.route) {
            // J'assume que dans RecipeSection.kt tu as une fonction @Composable nommée RecipeScreen()
            RecipeScreen()
        }
    }
}
