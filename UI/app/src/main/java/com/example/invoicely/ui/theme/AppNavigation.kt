package com.example.invoicely.ui.theme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.invoicely.viewmodel.InvoicesListUiState
import com.example.invoicely.viewmodel.InvoicesListViewModel
import com.example.invoicely.viewmodel.InvoicesListViewModelFactory
import com.example.invoicely.viewmodel.HistoryViewModel
import com.example.invoicely.viewmodel.HistoryViewModelFactory
import com.example.invoicely.viewmodel.HistoryUiState
import com.example.invoicely.network.BusinessProfileDto
import com.example.invoicely.viewmodel.SettingsViewModel
import com.example.invoicely.viewmodel.SettingsViewModelFactory
import com.example.invoicely.viewmodel.ProfileUiState
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val settingsFactory = remember { SettingsViewModelFactory(tokenManager) }
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)

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

            // 2. TokenManager aur Factory ko instantiate karo
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
            // 1. Setup the Token Manager and Factories
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            val dashboardFactory = remember { DashboardViewModelFactory(tokenManager) }
            val ledgerFactory = remember { InvoicesListViewModelFactory(tokenManager) }
            val historyFactory = remember { HistoryViewModelFactory(tokenManager) }

            // 2. State to track which tab is currently selected
            var currentTab by remember { mutableStateOf("dashboard") }

            // 3. Intercept back press: navigate back to dashboard first, or logout if already on dashboard
            BackHandler {
                if (currentTab != "dashboard") {
                    currentTab = "dashboard"
                } else {
                    tokenManager.clearToken()
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            }

            // 4. Pass the tab state to MainScaffold
            MainScaffold(
                currentRoute = currentTab,
                onNavigate = { newTab -> currentTab = newTab },
                onNewInvoiceClick = {
                    navController.navigate("create_invoice")
                }
            ) {
                // Switch between screens based on the selected tab
                when (currentTab) {
                    "dashboard" -> {
                        val dashboardViewModel: DashboardViewModel = viewModel(factory = dashboardFactory)
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
                    "ledger" -> {
                        val ledgerViewModel: InvoicesListViewModel = viewModel(factory = ledgerFactory)
                        LaunchedEffect(Unit) {
                            ledgerViewModel.fetchAllInvoices()
                        }

                        when (val state = ledgerViewModel.uiState.value) {
                            is InvoicesListUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF151A11))
                                }
                            }
                            is InvoicesListUiState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = state.message,
                                            color = Color(0xFFD32F2F),
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 15.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Button(
                                            onClick = { ledgerViewModel.fetchAllInvoices() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF151A11)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Retry", color = Color.White, fontFamily = OutfitFontFamily)
                                        }
                                    }
                                }
                            }
                            is InvoicesListUiState.Success -> {
                                InvoicesListScreen(
                                    invoices = state.data,
                                    onInvoiceClick = { id ->
                                        navController.navigate("invoice_detail/$id")
                                    }
                                )
                            }
                        }
                    }
                    "history" -> {
                        val historyViewModel: HistoryViewModel = viewModel(factory = historyFactory)
                        LaunchedEffect(Unit) {
                            historyViewModel.fetchHistory()
                        }

                        when (val state = historyViewModel.uiState.value) {
                            is HistoryUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF151A11))
                                }
                            }
                            is HistoryUiState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = state.message,
                                            color = Color(0xFFD32F2F),
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 15.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Button(
                                            onClick = { historyViewModel.fetchHistory() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF151A11)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Retry", color = Color.White, fontFamily = OutfitFontFamily)
                                        }
                                    }
                                }
                            }
                            is HistoryUiState.Success -> {
                                HistoryScreen(
                                    events = state.events,
                                    onInvoiceClick = { invoiceId ->
                                        if (invoiceId.isNotBlank()) {
                                            navController.navigate("invoice_detail/$invoiceId")
                                        }
                                    }
                                )
                            }
                        }
                    }
                    "settings" -> {
                        LaunchedEffect(Unit) {
                            settingsViewModel.fetchProfile()
                        }

                        val profileState = settingsViewModel.uiState.value
                        val currentProfile = (profileState as? ProfileUiState.Success)?.profile

                        val displayName = currentProfile?.tradeName?.takeIf { it.isNotBlank() }
                            ?: currentProfile?.legalEntityName?.takeIf { it.isNotBlank() }
                            ?: "Vaibhav Sharma"

                        val displayBusiness = currentProfile?.legalEntityName?.takeIf { it.isNotBlank() }
                            ?: "Invoicely HQ"

                        SettingsScreen(
                            userName = displayName,
                            businessName = displayBusiness,
                            onNavigateToCompanyDetails = {
                                navController.navigate("company_details")
                            },
                            onNavigateToBankUpi = {
                                navController.navigate("bank_and_upi")
                            },
                            onLogoutClick = {
                                tokenManager.clearToken()
                                navController.navigate("auth") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
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
                onRecordPaymentClick = { /* TODO later */ },
                onConfirmPayment = { amount, method ->
                    viewModel.submitPayment(invoiceId, amount, method)
                },
                onPaymentSuccess = {
                    viewModel.fetchInvoiceDetails(invoiceId)
                }
            )
        }

        composable("company_details") {
            LaunchedEffect(Unit) {
                settingsViewModel.fetchProfile()
            }

            val currentState = settingsViewModel.uiState.value
            val profile = (currentState as? ProfileUiState.Success)?.profile ?: BusinessProfileDto()

            val uiState = CompanyProfileState(
                legalEntityName = profile.legalEntityName ?: "",
                tradeName = profile.tradeName ?: "",
                gstin = profile.gstin ?: "",
                contactEmail = profile.contactEmail ?: "",
                contactPhone = profile.contactPhone ?: "",
                registeredAddress = profile.registeredAddress ?: "",
                pinCode = profile.pinCode ?: ""
            )

            CompanyDetailsScreen(
                initialState = uiState,
                onBackClick = { navController.popBackStack() },
                onSaveProfile = { updatedUiState ->
                    // Merge updated UI state with existing Bank details so we don't overwrite Bank data
                    val mergedDto = profile.copy(
                        legalEntityName = updatedUiState.legalEntityName ?: "",
                        tradeName = updatedUiState.tradeName ?: "",
                        gstin = updatedUiState.gstin ?: "",
                        contactEmail = updatedUiState.contactEmail ?: "",
                        contactPhone = updatedUiState.contactPhone ?: "",
                        registeredAddress = updatedUiState.registeredAddress ?: "",
                        pinCode = updatedUiState.pinCode ?: ""
                    )

                    coroutineScope.launch {
                        val success = settingsViewModel.updateProfile(mergedDto)
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }

        composable("bank_and_upi") {
            LaunchedEffect(Unit) {
                settingsViewModel.fetchProfile()
            }

            val currentState = settingsViewModel.uiState.value
            val profile = (currentState as? ProfileUiState.Success)?.profile ?: BusinessProfileDto()

            val bankState = BankAndUpiState(
                accountHolderName = profile.accountHolderName ?: "",
                bankName = profile.bankName ?: "",
                accountNumber = profile.accountNumber ?: "",
                ifscCode = profile.ifscCode ?: "",
                upiVpa = profile.upiVpa ?: ""
            )

            BankAndUpiScreen(
                initialState = bankState,
                onBackClick = { navController.popBackStack() },
                onSavePaymentDetails = { updatedBankState ->
                    val mergedDto = profile.copy(
                        accountHolderName = updatedBankState.accountHolderName ?: "",
                        bankName = updatedBankState.bankName ?: "",
                        accountNumber = updatedBankState.accountNumber ?: "",
                        ifscCode = updatedBankState.ifscCode ?: "",
                        upiVpa = updatedBankState.upiVpa ?: ""
                    )

                    coroutineScope.launch {
                        val success = settingsViewModel.updateProfile(mergedDto)
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    }
}