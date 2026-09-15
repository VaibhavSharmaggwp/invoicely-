package com.example.invoicely.ui.theme

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.invoicely.viewmodel.InvoiceDetailViewModel
import com.example.invoicely.viewmodel.InvoiceDetailViewModelFactory

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

            // 3. Intercept back press on Dashboard: Go back to Login screen instead of closing app
            BackHandler {
                tokenManager.clearToken()
                navController.navigate("auth") {
                    popUpTo("main") { inclusive = true }
                }
            }

            // 4. Pass the UI state to your MainScaffold
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
                        onInvoiceClick = { invoiceId ->
                            navController.navigate("invoice_detail/$invoiceId")
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

        composable("invoice_detail/{invoiceId}") { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: return@composable

            // Factory and ViewModel setup
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            val factory = remember { InvoiceDetailViewModelFactory(tokenManager) }
            val viewModel: InvoiceDetailViewModel = viewModel(factory = factory)

            // Fetch data only once when screen opens
            LaunchedEffect(invoiceId) {
                viewModel.fetchInvoiceDetails(invoiceId)
            }

            InvoiceDetailScreen(
                uiState = viewModel.uistate.value,
                onBackClick = { navController.popBackStack() },
                onDownloadPdfClick = { /* TODO later */ },
                onRecordPaymentClick = { /* TODO later */ }
            )
        }
    }
}