package com.undef.prowallet

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.undef.prowallet.ui.navigation.AppNavGraph
import com.undef.prowallet.ui.navigation.Screen
import com.undef.prowallet.ui.theme.PrimaryDarker
import com.undef.prowallet.ui.theme.ProWalletTheme
import com.undef.prowallet.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

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

    var fabOffsetX by remember { mutableFloatStateOf(0f) }
    var fabOffsetY by remember { mutableFloatStateOf(0f) }
    var parentWidth by remember { mutableFloatStateOf(0f) }
    var parentHeight by remember { mutableFloatStateOf(0f) }
    var initialized by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                parentWidth = coords.size.width.toFloat()
                parentHeight = coords.size.height.toFloat()
                if (initialized == 0f && parentWidth > 0f) {
                    // Start at bottom-end with safe insets margin
                    fabOffsetX = parentWidth - 200f
                    fabOffsetY = parentHeight - 300f
                    initialized = 1f
                }
            }
    ) {
        AppNavGraph(navController = navController)

        if (currentRoute != null && currentRoute !in excludedScreens) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.ChatAi.route) },
                modifier = Modifier
                    .offset { IntOffset(fabOffsetX.roundToInt(), fabOffsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newX = (fabOffsetX + dragAmount.x)
                                .coerceIn(0f, parentWidth - 160f)
                            val newY = (fabOffsetY + dragAmount.y)
                                .coerceIn(0f, parentHeight - 160f)
                            fabOffsetX = newX
                            fabOffsetY = newY
                        }
                    },
                containerColor = PrimaryDarker,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Chat con IA",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
