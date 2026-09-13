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
import com.example.invoicely.viewmodel.CreateInvoiceViewModel
import com.example.invoicely.viewmodel.CreateInvoiceViewModelFactory
import com.example.invoicely.viewmodel.DashboardViewModel
import com.example.invoicely.viewmodel.DashboardViewModelFactory

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
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            // 1. Setup the Token Manager and Factory
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            val factory = remember { DashboardViewModelFactory(tokenManager)}

            // 2. Instantiate the DashboardViewModel
            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)

            // 3. Pass the UI state to your MainScaffold
            MainScaffold(
                onNewInvoiceClick = {
                    navController.navigate("create_invoice")
                },
                onLogoutClick = {
                    tokenManager.clearToken()
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                dashboardContent = {
                    // This reads the live state (Loading, Success, etc.) and updates the UI
                    DashboardScreen(
                        uiState = dashboardViewModel.uiState.value,
                        onNewInvoiceClick = {
                            navController.navigate("create_invoice")
                        },
                        onRefresh = {
                            dashboardViewModel.fetchDashboardData()
                        }
                    )
                }
            )
        }

        composable("create_invoice") {
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            val factory = remember { CreateInvoiceViewModelFactory(tokenManager) }
            val createInvoiceViewModel: CreateInvoiceViewModel = viewModel(factory = factory)

            CreateInvoiceScreen(
                viewModel = createInvoiceViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}