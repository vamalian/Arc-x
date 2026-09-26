package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcxCyan
import com.example.ui.theme.ArcxDeepSurface
import com.example.ui.theme.ArcxHudBorder
import com.example.ui.theme.ArcxTextMuted
import com.example.ui.theme.ArcxTextPrimary
import com.example.ui.theme.ArcxVoid

@Composable
fun MainScreen(
    viewModel: ArcxViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val onboardingCompleted by viewModel.repository.onboardingCompleted.collectAsState()

    // Handle back button: return to HUD if on another tab
    BackHandler(enabled = currentScreen != ArcxScreen.HUD) {
        viewModel.navigateTo(ArcxScreen.HUD)
    }

    if (!onboardingCompleted) {
        OnboardingDialog(
            onDismiss = { viewModel.completeOnboarding() }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArcxVoid,
        bottomBar = {
            SciFiBottomNav(
                currentScreen = currentScreen,
                onSelectScreen = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ArcxScreen.HUD -> HudScreen(viewModel = viewModel)
                ArcxScreen.CHAT -> ChatScreen(viewModel = viewModel)
                ArcxScreen.TOOLS -> ToolsScreen(viewModel = viewModel)
                ArcxScreen.MEMORY -> MemoryScreen(viewModel = viewModel)
                ArcxScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun SciFiBottomNav(
    currentScreen: ArcxScreen,
    onSelectScreen: (ArcxScreen) -> Unit
) {
    NavigationBar(
        containerColor = ArcxDeepSurface,
        modifier = Modifier
            .border(width = 1.dp, color = ArcxHudBorder)
    ) {
        val navItems = listOf(
            NavTabItem(ArcxScreen.HUD, "HUD", Icons.Default.RadioButtonChecked, "nav_hud"),
            NavTabItem(ArcxScreen.CHAT, "Chat", Icons.Default.ChatBubble, "nav_chat"),
            NavTabItem(ArcxScreen.TOOLS, "Tools", Icons.Default.Build, "nav_tools"),
            NavTabItem(ArcxScreen.MEMORY, "Memory", Icons.Default.Memory, "nav_memory"),
            NavTabItem(ArcxScreen.SETTINGS, "Config", Icons.Default.Settings, "nav_settings")
        )

        navItems.forEach { item ->
            val selected = currentScreen == item.screen
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectScreen(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp),
                        tint = if (selected) ArcxCyan else ArcxTextMuted
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (selected) ArcxCyan else ArcxTextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = ArcxCyan.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag(item.tag)
            )
        }
    }
}

private data class NavTabItem(
    val screen: ArcxScreen,
    val label: String,
    val icon: ImageVector,
    val tag: String
)
