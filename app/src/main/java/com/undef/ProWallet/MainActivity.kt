package com.undef.prowallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.undef.prowallet.ui.navigation.AppNavGraph
import com.undef.prowallet.ui.theme.ProWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProWalletTheme {
                AppNavGraph()
            }
        }
    }
}
