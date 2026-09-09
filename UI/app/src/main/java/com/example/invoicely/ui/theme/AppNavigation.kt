package com.example.invoicely.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.invoicely.security.TokenManager
import com.example.invoicely.viewmodel.AuthViewModel
import com.example.invoicely.viewmodel.AuthViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(onAnimationFinished = {
                navController.navigate("auth") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("auth") {
            // 1. Android Context nikaalo (SharedPreferences ke liye zaroori hai)
            val context = LocalContext.current

            // 2. TokenManager aur Factory ko instantiate karo (remember use karke taaki screen rotate hone par destroy na ho)
            val tokenManager = remember { TokenManager(context) }
            val factory = remember { AuthViewModelFactory(tokenManager) }

            // 3. Apni factory pass karke ViewModel generate karo
            val authViewModel: AuthViewModel = viewModel(factory = factory)

            // 4. AuthScreen ko ViewModel pass karo
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate("main")
                }
            )
        }

        composable("main") {
            MainScaffold()
        }
    }
}