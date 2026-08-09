
package com.fitblock.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitblock.game.ui.GameViewModel
import com.fitblock.game.ui.screens.FitBlockGameRoot
import com.fitblock.game.ui.theme.FitBlockColors
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: GameViewModel = viewModel()
            LaunchedEffect(Unit) { vm.init(this@MainActivity) }
            // Force dark theme
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().background(FitBlockColors.Background), color = FitBlockColors.Background) {
                    FitBlockGameRoot(vm)
                }
            }
        }
    }
}
