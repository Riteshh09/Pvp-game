package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val profile by viewModel.playerProfile.collectAsState()

                var showCustomizer by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ArenaColors.DarkCanvas
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val currentProfile = profile
                        if (currentProfile != null) {
                            when (currentScreen) {
                                is ScreenState.MainDashboard -> {
                                    DashboardScreen(
                                        viewModel = viewModel,
                                        profile = currentProfile,
                                        onCustomizeProfileClick = { showCustomizer = true }
                                    )
                                }
                                is ScreenState.Matchmaking -> {
                                    MatchmakingScreen(viewModel = viewModel)
                                }
                                is ScreenState.BattleArena -> {
                                    BattleArenaScreen(viewModel = viewModel, profile = currentProfile)
                                }
                                is ScreenState.Leaderboard -> {
                                    LeaderboardScreen(viewModel = viewModel, profile = currentProfile)
                                }
                                is ScreenState.MatchHistoryView -> {
                                    MatchHistoryScreen(viewModel = viewModel)
                                }
                            }

                            if (showCustomizer) {
                                ProfileCustomizerDialog(
                                    currentProfile = currentProfile,
                                    onDismiss = { showCustomizer = false },
                                    onSave = { name, avatar, combatClass ->
                                        viewModel.customizeProfile(name, avatar, combatClass)
                                        showCustomizer = false
                                    }
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ArenaColors.NeonCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}
