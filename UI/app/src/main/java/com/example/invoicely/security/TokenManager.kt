package com.example.invoicely.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    // 1. MASTER KEY: Yeh ek special digital (key) hai jo Android Keystore me securely save hoti hai.
    // Isko koi doosra app ya hacker read nahi kar sakta.
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // 2. ENCRYPTED PREFERENCES: Yeh Java ke normal SharedPreferences jaisa hi hai,
    // par isme save hone wala har data (keys aur values dono) encrypt ho jata hai.
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "invoicely_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Key encrypt karne ka algorithm
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Value encrypt karne ka algorithm
    )

    // 3. SAVE TOKEN FUNCTION
    fun saveToken(token: String) {
        sharedPreferences.edit().apply {
            putString("jwt_token", token)
            apply() // apply() asynchronous (background) me save karta hai taaki UI freeze na ho
        }
    }

    // 4. GET TOKEN FUNCTION
    // Return type 'String?' ka matlab hai ki yeh null bhi return kar sakta hai (agar user logged out hai)
    fun getToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }

    // 5. CLEAR TOKEN FUNCTION (For Logout)
    fun clearToken() {
        sharedPreferences.edit().apply {
            remove("jwt_token")
            apply()
        }
    }
}