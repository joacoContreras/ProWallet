package com.undef.prowallet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.undef.prowallet.ui.screens.*
import com.undef.prowallet.viewmodel.AuthViewModel
import com.undef.prowallet.viewmodel.HomeViewModel
import com.undef.prowallet.viewmodel.PurchaseViewModel

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
    object ChatAi : Screen("chat_ai")
    object ManageAccounts : Screen("manage_accounts")
    object MonthlySetup : Screen("monthly_setup")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val purchaseViewModel: PurchaseViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToLogin = {
                navController.navigate(Screen.Login.route) {
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
                onViewReceipt = { /* Logic for receipt */ }
            )
        }

        composable(
            route = Screen.PurchaseDetail.route,
            arguments = listOf(navArgument("purchaseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val purchaseId = backStackEntry.arguments?.getString("purchaseId") ?: ""
            PurchaseDetailScreen(
                purchaseId = purchaseId,
                viewModel = purchaseViewModel,
                homeViewModel = homeViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { _ ->
                    navController.navigate(Screen.NewPurchase.route)
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                homeViewModel = homeViewModel,
                onNavigateToPurchaseDetail = { id ->
                    navController.navigate(Screen.PurchaseDetail.createRoute(id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                homeViewModel = homeViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToNewPurchase = { navController.navigate(Screen.NewPurchase.route) },
                onNavigateToTopStores = { navController.navigate(Screen.TopStores.route) }
            )
        }

        composable(Screen.TopStores.route) {
            TopStoresScreen(
                homeViewModel = homeViewModel,
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
            StoreDetailScreen(
                storeName = storeName,
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
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ManageAccounts.route) {
            ManageAccountsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MonthlySetup.route) {
            MonthlySetupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChatAi.route) {
            ChatAiScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
