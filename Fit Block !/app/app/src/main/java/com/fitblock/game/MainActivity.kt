package com.fitblock.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.fitblock.game.ui.GameViewModel
import com.fitblock.game.ui.screens.FitBlockGameRoot
import com.fitblock.game.ui.theme.FitBlockTheme

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitBlockTheme {
                FitBlockGameRoot(vm)
            }
        }
    }
}
