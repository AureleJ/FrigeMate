package com.example.fridgemate

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody

/* --- 1. LOGIN & USER --- */
data class LoginRequest(val username: String)
data class LoginResponse(val success: Boolean, val user: UserData)

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

    @SerializedName("name") val title: String, // <-- Traduction: API envoie "name", on veut "title"

    val description: String?,

    // Champs spécifiques de ton API
    val prep_time_min: Int? = 0,
    val cook_time_min: Int? = 0,

    val ingredients: List<RecipeIngredient>? = emptyList(),
    val instructions: List<String>? = emptyList()
) {
    val id: String
        get() = mongoId ?: simpleId ?: ""

    // On calcule la durée totale pour l'affichage (Prep + Cuisson)
    val duration: Int
        get() = (prep_time_min ?: 0) + (cook_time_min ?: 0)

    // Logique végétarienne (basée sur le titre ou la description car pas de champ "category")
    val isVegetarian: Boolean
        get() = description?.contains("Veg", true) == true || title.contains("Veg", true)
}

data class RecipeSearchRequest(val ingredients: List<String>)

/* --- 4. API SERVICE --- */
interface FridgeApiService {
    // Users
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // Fridge
    @retrofit2.http.GET("users/{userId}/fridge/ingredients")
    suspend fun getIngredients(@retrofit2.http.Path("userId") userId: String): IngredientsResponse

    @retrofit2.http.POST("users/{userId}/fridge/ingredients")
    suspend fun addIngredient(@retrofit2.http.Path("userId") userId: String, @retrofit2.http.Body ingredient: AddIngredientRequest): IngredientData

    @retrofit2.http.DELETE("users/{userId}/fridge/ingredients/{ingredientId}")
    suspend fun deleteIngredient(@retrofit2.http.Path("userId") userId: String, @retrofit2.http.Path("ingredientId") ingredientId: String): retrofit2.Response<ResponseBody>

    // Recipes
    @retrofit2.http.GET("recipes")
    suspend fun getAllRecipes(): RecipesResponse

    @retrofit2.http.POST("recipes/search")
    suspend fun searchRecipes(@retrofit2.http.Body request: RecipeSearchRequest): RecipesResponse
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
    val apiService: FridgeApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FridgeApiService::class.java)
    }
}