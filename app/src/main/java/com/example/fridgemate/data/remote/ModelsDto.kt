package com.example.fridgemate.data.remote

import com.google.gson.annotations.SerializedName

/**
 * DTO pour un Ingrédient.
 * C'est le contrat entre l'app et le serveur.
 */
data class IngredientDto(
    // 1. L'annotation @SerializedName est CRUCIALE si le nom côté serveur est différent.
    // Souvent, les bases de données (comme MongoDB) utilisent "_id".
    // Ici, on dit à Kotlin : "Quand tu reçois du JSON avec le champ '_id', range-le dans ma variable 'id'".
    @SerializedName("_id") val id: String? = null,

    // 2. Si le nom JSON est exactement le même que le nom Kotlin, pas besoin d'annotation.
    val name: String,
    val quantity: String,

    // 3. Pour les dates, le plus simple est souvent d'utiliser un Timestamp (Long),
    // qui est un grand chiffre représentant les millisecondes depuis 1970.
    // C'est universel et facile à gérer pour le serveur et l'app.
    val expiryDate: Long? = null
)

/**
 * DTO pour un Article de course.
 * Même logique.
 */
data class ShoppingItemDto(
    // On suppose ici aussi que le serveur envoie un "_id". Adaptez selon votre serveur Express !
    @SerializedName("_id") val id: String? = null,
    val name: String,
    val category: String = "Général",
    val isChecked: Boolean = false
)