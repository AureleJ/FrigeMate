// MainActivity.kt
package com.example.fridgemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
// Importez vos autres écrans ici
// import com.example.fridgemate.screens.*

// --- Définition des Couleurs Globales ---
val GreenPrimary = Color(0xFF4CAF50)
val GreenLight = Color(0xFFE8F5E9)
val TextDark = Color(0xFF2C3E50)
val TextGray = Color(0xFF7F8C8D)
val BgColor = Color(0xFFF5F7F6)

// --- Routes de Navigation ---
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Tableau de Bord", Icons.Default.Kitchen)
    object Recipes : Screen("recipes", "Recettes", Icons.Default.RestaurantMenu)
    object Planning : Screen("planning", "Planification", Icons.Default.CalendarMonth)
    object Shopping : Screen("shopping", "Liste Courses", Icons.Default.ShoppingCart)
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
    // État pour savoir sur quel écran on est pour changer le titre
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreenTitle = when (currentRoute) {
        Screen.Recipes.route -> Screen.Recipes.title
        Screen.Planning.route -> Screen.Planning.title
        Screen.Shopping.route -> Screen.Shopping.title
        else -> Screen.Dashboard.title
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Affiche l'icône seulement sur le dashboard pour correspondre à l'image originale
                        if (currentRoute == Screen.Dashboard.route || currentRoute == null) {
                            Icon(Icons.Default.Kitchen, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("FridgeMate $currentScreenTitle", fontSize = 18.sp, color = Color.White)
                    }
                },
                navigationIcon = { IconButton(onClick = {}) { Icon(Icons.Default.Menu, "Menu", tint = Color.White) } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.Notifications, "Alertes", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) },
        floatingActionButton = {
            // Le FAB change d'action selon la page, ou peut être caché
            FloatingActionButton(
                onClick = { /* Action contextuelle : Ajouter ingrédient, liste, etc. */ },
                containerColor = GreenPrimary, contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(65.dp)
            ) { Icon(Icons.Default.Add, "Ajouter", modifier = Modifier.size(32.dp)) }
        },
        floatingActionButtonPosition = FabPosition.EndOverlay
    ) { paddingValues ->
        // Zone de contenu principal gérée par le NavHost
        Box(modifier = Modifier.padding(paddingValues)) {
            NavigationGraph(navController = navController)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val screens = listOf(Screen.Dashboard, Screen.Recipes, Screen.Planning, Screen.Shopping)
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
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            // Remplacer par l'appel à votre composable DashboardScreen() du code précédent
            // DashboardScreen()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Contenu du Tableau de Bord ici") }
        }
        composable(Screen.Recipes.route) { RecipeScreen() }
        composable(Screen.Planning.route) { PlanningScreen() }
        composable(Screen.Shopping.route) { ShoppingListScreen() }
    }
}