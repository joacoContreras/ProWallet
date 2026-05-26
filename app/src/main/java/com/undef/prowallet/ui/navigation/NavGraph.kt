package com.undef.prowallet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.undef.prowallet.ui.screens.*
import com.undef.prowallet.viewmodel.AccountViewModel
import com.undef.prowallet.viewmodel.AnalyticsViewModel
import com.undef.prowallet.viewmodel.AutoSavingsViewModel
import com.undef.prowallet.viewmodel.FixedExpensesViewModel
import com.undef.prowallet.viewmodel.MonthlySetupViewModel
import com.undef.prowallet.viewmodel.AuthViewModel
import com.undef.prowallet.viewmodel.HistoryViewModel
import com.undef.prowallet.viewmodel.HomeViewModel
import com.undef.prowallet.viewmodel.PurchaseDetailViewModel
import com.undef.prowallet.viewmodel.PurchaseViewModel
import com.undef.prowallet.viewmodel.StoreDetailViewModel
import com.undef.prowallet.viewmodel.TopStoresViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object RegisterSuccess : Screen("register_success")
    object Home : Screen("home")
    object NewPurchase : Screen("new_purchase")
    object PurchaseDetail : Screen("purchase_detail/{purchaseId}") {
        fun createRoute(purchaseId: String) = "purchase_detail/$purchaseId"
    }
    object History : Screen("history")
    object Analytics : Screen("analytics")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object TopStores : Screen("top_stores")
    object StoreDetail : Screen("store_detail/{storeName}") {
        fun createRoute(storeName: String) = "store_detail/$storeName"
    }
    object PurchaseSuccess : Screen("purchase_success")
    object EditPurchase : Screen("edit_purchase/{purchaseId}") {
        fun createRoute(purchaseId: String) = "edit_purchase/$purchaseId"
    }
    object ChatAi : Screen("chat_ai")
    object ManageAccounts : Screen("manage_accounts")
    object MonthlySetup : Screen("monthly_setup")
    object PersonalInflation : Screen("personal_inflation")
    object AutoSavings : Screen("auto_savings")
    object FixedExpenses : Screen("fixed_expenses")
    object ForgotPassword : Screen("forgot_password")
    object VerifyCode : Screen("verify_code")
    object UpdatePassword : Screen("update_password")
    object UpdatePasswordSuccess : Screen("update_password_success")
    object ContactSupport : Screen("contact_support")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val purchaseViewModel: PurchaseViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)
            SplashScreen(onNavigateToLogin = {
                val destination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
                navController.navigate(destination) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.RegisterSuccess.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.RegisterSuccess.route) {
            RegisterSuccessScreen(
                onBegin = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                homeViewModel = homeViewModel,
                onNavigateToNewPurchase = { navController.navigate(Screen.NewPurchase.route) },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToPurchaseDetail = { id ->
                    navController.navigate(Screen.PurchaseDetail.createRoute(id))
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NewPurchase.route) {
            NewPurchaseScreen(
                viewModel = purchaseViewModel,
                onSaveSuccess = {
                    navController.navigate(Screen.PurchaseSuccess.route)
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) }
            )
        }

        composable(Screen.PurchaseSuccess.route) {
            PurchaseSuccessScreen(
                viewModel = purchaseViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onViewReceipt = {
                    purchaseViewModel.uiState.value.savedPurchaseId?.let { id ->
                        navController.navigate(Screen.PurchaseDetail.createRoute(id))
                    }
                }
            )
        }

        composable(
            route = Screen.PurchaseDetail.route,
            arguments = listOf(navArgument("purchaseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val purchaseId = backStackEntry.arguments?.getString("purchaseId") ?: ""
            val purchaseDetailViewModel: PurchaseDetailViewModel = viewModel()
            PurchaseDetailScreen(
                purchaseId = purchaseId,
                viewModel = purchaseDetailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditPurchase.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EditPurchase.route,
            arguments = listOf(navArgument("purchaseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val purchaseId = backStackEntry.arguments?.getString("purchaseId") ?: ""
            NewPurchaseScreen(
                viewModel = purchaseViewModel,
                purchaseId = purchaseId,
                onSaveSuccess = {
                    purchaseViewModel.uiState.value.savedPurchaseId?.let { id ->
                        navController.navigate(Screen.PurchaseDetail.createRoute(id)) {
                            popUpTo(Screen.EditPurchase.route) { inclusive = true }
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) }
            )
        }

        composable(Screen.History.route) {
            val historyViewModel: HistoryViewModel = viewModel()
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateToPurchaseDetail = { id ->
                    navController.navigate(Screen.PurchaseDetail.createRoute(id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Analytics.route) {
            val analyticsViewModel: AnalyticsViewModel = viewModel()
            AnalyticsScreen(
                viewModel = analyticsViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToNewPurchase = { navController.navigate(Screen.NewPurchase.route) },
                onNavigateToTopStores = { navController.navigate(Screen.TopStores.route) },
                onNavigateToPersonalInflation = { navController.navigate(Screen.PersonalInflation.route) }
            )
        }

        composable(Screen.PersonalInflation.route) {
            PersonalInflationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }

        composable(Screen.TopStores.route) {
            val topStoresViewModel: TopStoresViewModel = viewModel()
            TopStoresScreen(
                viewModel = topStoresViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStoreDetail = { name ->
                    navController.navigate(Screen.StoreDetail.createRoute(name))
                }
            )
        }

        composable(
            route = Screen.StoreDetail.route,
            arguments = listOf(navArgument("storeName") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeName = backStackEntry.arguments?.getString("storeName") ?: ""
            val storeDetailViewModel: StoreDetailViewModel = viewModel()
            StoreDetailScreen(
                storeName = storeName,
                viewModel = storeDetailViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToManageAccounts = { navController.navigate(Screen.ManageAccounts.route) },
                onNavigateToMonthlySetup = { navController.navigate(Screen.MonthlySetup.route) },
                onNavigateToFixedExpenses = { navController.navigate(Screen.FixedExpenses.route) },
                onNavigateToAutoSavings = { navController.navigate(Screen.AutoSavings.route) },
                onNavigateToContactSupport = { navController.navigate(Screen.ContactSupport.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.FixedExpenses.route) {
            val fixedExpensesViewModel: FixedExpensesViewModel = viewModel()
            FixedExpensesScreen(
                viewModel = fixedExpensesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageAccounts.route) {
            val accountViewModel: AccountViewModel = viewModel()
            ManageAccountsScreen(
                viewModel = accountViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MonthlySetup.route) {
            val monthlySetupViewModel: MonthlySetupViewModel = viewModel()
            MonthlySetupScreen(
                viewModel = monthlySetupViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChatAi.route) {
            ChatAiScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AutoSavings.route) {
            val autoSavingsViewModel: AutoSavingsViewModel = viewModel()
            AutoSavingsScreen(
                viewModel = autoSavingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onCodeSent = {
                    navController.navigate(Screen.VerifyCode.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.VerifyCode.route) {
            VerifyCodeScreen(
                viewModel = authViewModel,
                onVerified = { navController.navigate(Screen.UpdatePassword.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.UpdatePassword.route) {
            UpdatePasswordScreen(
                viewModel = authViewModel,
                onSuccess = { navController.navigate(Screen.UpdatePasswordSuccess.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.UpdatePasswordSuccess.route) {
            UpdatePasswordSuccessScreen(
                viewModel = authViewModel,
                onRedirect = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ContactSupport.route) {
            ContactSupportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
