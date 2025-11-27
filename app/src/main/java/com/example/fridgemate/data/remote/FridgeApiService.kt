package com.example.fridgemate.data.remote

import retrofit2.http.*

// Cette interface est comme un "menu" des fonctions du serveur.
interface FridgeApiService {

    // --- SECTION INGRÉDIENTS ---

    // 1. @GET : Indique que c'est une requête HTTP GET (pour lire des données).
    // "api/ingredients" : C'est la fin de l'URL (le endpoint) que votre serveur Express écoute.
    // suspend fun : La fonction doit être "suspend" car une requête réseau prend du temps,
    // et on ne veut pas bloquer l'application pendant ce temps.
    // List<IngredientDto> : On s'attend à ce que le serveur nous réponde avec une liste d'ingrédients en JSON.
    @GET("api/ingredients")
    suspend fun getIngredients(): List<IngredientDto>

    // 2. @POST : Requête HTTP POST (pour créer des données).
    // @Body : On dit à Retrofit : "Prends l'objet 'ingredient' que je te passe en paramètre,
    // transforme-le en JSON, et mets-le dans le corps de la requête HTTP pour l'envoyer au serveur".
    @POST("api/ingredients")
    suspend fun addIngredient(@Body ingredient: IngredientDto): IngredientDto
    // Note : Le serveur renvoie souvent l'objet créé (avec son nouvel ID généré).


    // --- SECTION LISTE DE COURSES ---

    @GET("api/shopping")
    suspend fun getShoppingItems(): List<ShoppingItemDto>

    @POST("api/shopping")
    suspend fun addShoppingItem(@Body item: ShoppingItemDto): ShoppingItemDto

    // 3. @PUT et @Path : Pour mettre à jour un élément précis.
    // "api/shopping/{id}" : L'URL contient une partie variable.
    // @Path("id") : On dit à Retrofit : "Prends la valeur du paramètre 'id' de la fonction,
    // et remplace le '{id}' dans l'URL par cette valeur".
    // Ex: si id = "123", l'URL sera "api/shopping/123".
    @PUT("api/shopping/{id}")
    suspend fun updateShoppingItem(@Path("id") id: String, @Body item: ShoppingItemDto): ShoppingItemDto

    @DELETE("api/shopping/{id}")
    suspend fun deleteShoppingItem(@Path("id") id: String)
}