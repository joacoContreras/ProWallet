package com.undef.prowallet

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.undef.prowallet.ui.navigation.AppNavGraph
import com.undef.prowallet.ui.navigation.Screen
import com.undef.prowallet.ui.theme.PrimaryDarker
import com.undef.prowallet.ui.theme.ProWalletTheme
import com.undef.prowallet.viewmodel.SettingsViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val darkMode by settingsViewModel.darkMode.collectAsState()
            ProWalletTheme(darkTheme = darkMode) {
                ProWalletAppWrapper()
            }
        }
    }
}

@Composable
fun ProWalletAppWrapper() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Screens where the AI FAB should NOT be visible
    val excludedScreens = listOf(
        Screen.Splash.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.RegisterSuccess.route,
        Screen.ChatAi.route
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AppNavGraph(navController = navController)

        if (currentRoute != null && currentRoute !in excludedScreens) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.ChatAi.route) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 120.dp), // Increased bottom padding to avoid Analytics overlap
                containerColor = PrimaryDarker,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Chat with AI",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
