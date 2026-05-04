package com.undef.superahorro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.undef.superahorro.ui.navigation.AppNavGraph
import com.undef.superahorro.ui.theme.SuperAhorroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperAhorroTheme {
                AppNavGraph()
            }
        }
    }
}
