//Network.kt
package com.example.fridgemate

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/* --- 1. LOGIN & USER --- */
data class RegisterRequest(val username: String)
// 1. Modèle de requête (Le body est le même pour login et création : { "username": "..." })
data class UserRequest(val username: String)

// 2. Modèle de réponse pour le LOGIN (qui contient "success")
data class LoginResponse(
    val success: Boolean,
    val user: UserData? // Peut être null si erreur
)

// 3. Modèle de l'Utilisateur (Reste le même, c'est ce que renvoie la création)
data class UserData(
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("id") val simpleId: String? = null,
    val username: String
) {
    val id: String get() = mongoId ?: simpleId ?: ""
}
/* --- 2. FRIDGE & INGREDIENTS --- */
data class IngredientsResponse(
    val ingredients: List<IngredientData>
)

data class IngredientData(
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("id") val simpleId: String? = null,
    val name: String,
    val quantity: String?,
    val unit: String?,
    val category: String?,
    val expiryDate: String?
) {
    val id: String get() = mongoId ?: simpleId ?: ""
}

data class AddIngredientRequest(
    val name: String,
    val quantity: String,
    val unit: String,
    val expiryDate: String?,
    val category: String
)

/* --- 3. RECIPES (ADAPTÉ À TON JSON) --- */

// Classe enveloppe (Wrapper)
data class RecipesResponse(
    val recipes: List<RecipeData>
)

// L'ingrédient DANS la recette (API utilise "item", pas "name")
data class RecipeIngredient(
    @SerializedName("item") val name: String?, // <-- Traduction ici
    val quantity: String?, // Gson convertira le nombre 500 en string "500"
    val unit: String?
)

// La Recette complète
data class RecipeData(
    @SerializedName("_id") val mongoId: String? = null,
    @SerializedName("id") val simpleId: String? = null,
    @SerializedName("name") val title: String,
    val description: String?,
    val prep_time_min: Int? = 0,
    val cook_time_min: Int? = 0,
    val ingredients: List<RecipeIngredient>? = emptyList(),
    val instructions: List<String>? = emptyList(),
    val imageUrl: String? = null
) {
    val id: String get() = mongoId ?: simpleId ?: ""
    val duration: Int get() = (prep_time_min ?: 0) + (cook_time_min ?: 0)
    val isVegetarian: Boolean get() = description?.contains("Veg", true) == true || title.contains("Veg", true) == true

    // --- AJOUTS POUR LES COMPTEURS ---
    // Ces variables seront remplies par le ViewModel
    var matchingCount: Int = 0
    var missingCount: Int = 0
}

data class RecipeSearchRequest(val ingredients: List<String>)

/* --- 4. API SERVICE --- */
interface FridgeApiService {
    // Users
    @POST("users/login")
    suspend fun login(@Body request: UserRequest): LoginResponse

    // Route CRÉATION (POST /) : Renvoie DIRECTEMENT un UserData
    // Attention : J'assume que ton routeur est monté sur "/users" dans ton server.js
    @POST("users")
    suspend fun createUser(@Body request: UserRequest): UserData

    // Fridge
    @retrofit2.http.GET("users/{userId}/fridge/ingredients")
    suspend fun getIngredients(@retrofit2.http.Path("userId") userId: String): IngredientsResponse

    @retrofit2.http.POST("users/{userId}/fridge/ingredients")
    suspend fun addIngredient(@retrofit2.http.Path("userId") userId: String, @retrofit2.http.Body ingredient: AddIngredientRequest): IngredientData


    @retrofit2.http.DELETE("users/{userId}/fridge/ingredients/{ingredientId}")
    suspend fun deleteIngredient(@retrofit2.http.Path("userId") userId: String, @retrofit2.http.Path("ingredientId") ingredientId: String): retrofit2.Response<ResponseBody>

    @retrofit2.http.PUT("users/{userId}/fridge/ingredients/{ingredientId}")
    suspend fun updateIngredient(
        @retrofit2.http.Path("userId") userId: String,
        @retrofit2.http.Path("ingredientId") ingredientId: String,
        @retrofit2.http.Body ingredient: AddIngredientRequest // On réutilise le même objet que pour l'ajout
    ): IngredientData

    // Recipes
    @retrofit2.http.GET("recipes")
    suspend fun getAllRecipes(): RecipesResponse

    @retrofit2.http.POST("recipes/search")
    suspend fun searchRecipes(@retrofit2.http.Body request: RecipeSearchRequest): RecipesResponse

}
/* --- 5. THE MEAL DB API (NOUVEAU) --- */

// Structure de la réponse JSON de TheMealDB
data class TheMealDbResponse(
    val meals: List<TheMealDbMeal>?
)

// Un plat tel que renvoyé par TheMealDB
data class TheMealDbMeal(
    val idMeal: String,
    val strMeal: String,        // Nom
    val strCategory: String?,   // Catégorie (Beef, Chicken...)
    val strArea: String?,       // Origine (Italian, French...)
    val strInstructions: String?, // Instructions (Un gros bloc de texte)
    val strMealThumb: String?,  // URL de l'image
    val strTags: String?,       // Tags (ex: "Pasta,Curry")

    // Les ingrédients sont moches dans cette API (1 à 20), on les mappe plus tard
    val strIngredient1: String?, val strMeasure1: String?,
    val strIngredient2: String?, val strMeasure2: String?,
    val strIngredient3: String?, val strMeasure3: String?,
    val strIngredient4: String?, val strMeasure4: String?,
    val strIngredient5: String?, val strMeasure5: String?,
    val strIngredient6: String?, val strMeasure6: String?,
    val strIngredient7: String?, val strMeasure7: String?,
    val strIngredient8: String?, val strMeasure8: String?,
    val strIngredient9: String?, val strMeasure9: String?,
    val strIngredient10: String?, val strMeasure10: String?,
    val strIngredient11: String?, val strMeasure11: String?,
    val strIngredient12: String?, val strMeasure12: String?,
    val strIngredient13: String?, val strMeasure13: String?,
    val strIngredient14: String?, val strMeasure14: String?,
    val strIngredient15: String?, val strMeasure15: String?,
    // ... on s'arrête à 15 pour l'exemple, l'API va jusqu'à 20
)

// Interface Retrofit spécifique pour TheMealDB
interface TheMealDbService {
    // Chercher par nom (ou vide pour avoir une sélection)
    @retrofit2.http.GET("search.php")
    suspend fun searchMeals(@retrofit2.http.Query("s") query: String): TheMealDbResponse
}

// Client Retrofit dédié
object TheMealDbClient {
    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    val apiService: TheMealDbService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TheMealDbService::class.java)
    }
}

// --- FONCTION UTILITAIRE DE CONVERSION ---
// Cette fonction transforme le format "moche" de TheMealDB en ton beau format "RecipeData"
fun TheMealDbMeal.toDomainModel(): RecipeData {
    // 1. Récupération des ingrédients
    val ingredientsList = mutableListOf<RecipeIngredient>()

    // Fonction locale pour ajouter si non vide
    fun addIfValid(ing: String?, meas: String?) {
        if (!ing.isNullOrBlank()) {
            ingredientsList.add(RecipeIngredient(name = ing, quantity = meas, unit = ""))
        }
    }

    addIfValid(strIngredient1, strMeasure1)
    addIfValid(strIngredient2, strMeasure2)
    addIfValid(strIngredient3, strMeasure3)
    addIfValid(strIngredient4, strMeasure4)
    addIfValid(strIngredient5, strMeasure5)
    addIfValid(strIngredient6, strMeasure6)
    addIfValid(strIngredient7, strMeasure7)
    addIfValid(strIngredient8, strMeasure8)
    addIfValid(strIngredient9, strMeasure9)
    addIfValid(strIngredient10, strMeasure10)
    addIfValid(strIngredient11, strMeasure11)
    addIfValid(strIngredient12, strMeasure12)
    addIfValid(strIngredient13, strMeasure13)
    addIfValid(strIngredient14, strMeasure14)
    addIfValid(strIngredient15, strMeasure15)

    // 2. Découpage des instructions (L'API donne un gros texte, on coupe aux retours à la ligne)
    val instructionsList = strInstructions?.split("\r\n", "\n")?.filter { it.isNotBlank() } ?: emptyList()

    // 3. Gestion du temps (L'API ne donne PAS le temps, on met une valeur aléatoire ou fixe pour l'UI)
    // Astuce : on génère un temps aléatoire entre 20 et 60 min pour faire "vrai"
    val randomTime = (20..60).random()

    return RecipeData(
        simpleId = idMeal,
        title = strMeal,
        description = strArea ?: "International", // On utilise l'origine comme description courte
        prep_time_min = 15, // Valeur par défaut
        cook_time_min = randomTime - 15,
        ingredients = ingredientsList,
        instructions = instructionsList,
        imageUrl = strMealThumb
        // Astuce : On détourne un champ existant ou on utilise une extension pour stocker l'URL d'image.
        // Comme RecipeData n'a pas de champ image, on va tricher et l'ajouter dans la classe RecipeData ci-dessous.
    )
}

// Modèles pour la réponse OFF
data class OffProductResponse(val product: OffProduct?, val status: Int)
data class OffProduct(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("product_name_en") val productNameEn: String?, // Ajout
    @SerializedName("product_name_fr") val productNameFr: String?, // Ajout
    @SerializedName("generic_name") val genericName: String?,      // Ajout

    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("quantity") val quantity: String?
) {
    // Une fonction intelligente pour trouver le meilleur nom disponible
    fun getBestName(): String {
        return productName?.takeIf { it.isNotBlank() }
            ?: productNameEn?.takeIf { it.isNotBlank() }
            ?: productNameFr?.takeIf { it.isNotBlank() }
            ?: genericName?.takeIf { it.isNotBlank() }
            ?: "Produit inconnu"
    }
}

// Interface API OFF
interface OpenFoodFactsService {
    @retrofit2.http.GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@retrofit2.http.Path("barcode") barcode: String): OffProductResponse
}

// Client Retrofit DÉDIÉ à Open Food Facts
object OffRetrofitClient {
    val apiService: OpenFoodFactsService by lazy {
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/") // URL différente de ton localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsService::class.java)
    }
}

/* --- 5. CLIENT RETROFIT --- */
object RetrofitClient {

    // 1. La nouvelle URL (IMPORTANT : Garde le '/' à la fin !)
    private const val BASE_URL = "https://frigemate-server.onrender.com/"

    // 2. Ta clé API
    private const val API_KEY = "tie-un-tigre"

    // Configuration du client HTTP pour ajouter la clé automatiquement
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()

            // Ajout de la clé API dans l'en-tête (Header) de chaque requête
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    // ⚠️ Vérifie le nom du header côté serveur (souvent "x-api-key", "Authorization" ou "api_key")
                    // Si tu n'es pas sûr, essaie "x-api-key" ou demande-moi selon ton code serveur.
                    .header("x-api-key", API_KEY)
                    .method(original.method, original.body)

                chain.proceed(requestBuilder.build())
            }

            // Augmenter le temps d'attente (Render peut être lent au réveil)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        builder.build()
    }

    val apiService: FridgeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // On attache le client sécurisé ici
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FridgeApiService::class.java)
    }
}