package com.example.fridgemate.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // --- POINT CRITIQUE : L'ADRESSE DU SERVEUR ---
    // Si votre serveur Express tourne sur votre PC sur le port 3000 (http://localhost:3000).
    // L'émulateur Android est comme un téléphone séparé. Pour lui, "localhost", c'est lui-même !
    // Pour accéder à votre PC depuis l'émulateur, Android fournit une adresse spéciale : 10.0.2.2
    // N'OUBLIEZ PAS LE "/" À LA FIN !
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // Si vous utilisez un VRAI téléphone connecté en USB, vous devez utiliser l'adresse IP locale
    // de votre PC sur le réseau WiFi (ex: http://192.168.1.45:3000/).


    // On crée la variable 'apiService' paresseusement ("by lazy").
    // Cela veut dire qu'elle ne sera créée que la première fois qu'on essaiera de l'utiliser.
    val apiService: FridgeApiService by lazy {
        Retrofit.Builder()
            // 1. On lui donne l'adresse de base du serveur.
            .baseUrl(BASE_URL)
            // 2. On ajoute le "traducteur" (ConverterFactory).
            // C'est lui qui utilise la librairie GSON pour transformer automatiquement :
            // Objets Kotlin -> Texte JSON (pour les envois)
            // Texte JSON -> Objets Kotlin (pour les réceptions)
            .addConverterFactory(GsonConverterFactory.create())
            // 3. On construit l'objet Retrofit.
            .build()
            // 4. L'étape magique : Retrofit lit notre interface FridgeApiService et crée
            // automatiquement le code nécessaire pour faire les requêtes HTTP.
            .create(FridgeApiService::class.java)
    }
}