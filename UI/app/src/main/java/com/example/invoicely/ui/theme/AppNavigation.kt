package com.example.invoicely.ui.theme

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    // 1. The Controller: Yeh navigation manage karega
    val navController = rememberNavController()

    // 2. The Host: App starts at "splash"
    NavHost(navController = navController, startDestination = "splash") {

        // Route 1: Splash Screen
        composable("splash") {
            SplashScreen(onAnimationFinished = {
                // Splash khatam hone par "auth" par jao
                navController.navigate("auth") {
                    // "splash" ko back history se hata do
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        // Route 2: Authentication Screen (Login / Sign Up)
        composable("auth") {
            AuthScreen(onAuthSuccess = {
                // Login success hone par "main" par jao
                navController.navigate("main") {
                    // "auth" ko back history se hata do
                    popUpTo("auth") { inclusive = true }
                }
            })
        }

        // Route 3: The Main Dashboard Shell
        composable("main") {
            MainScaffold()
        }
    }
}