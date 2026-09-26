package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcxCyan
import com.example.ui.theme.ArcxDeepSurface
import com.example.ui.theme.ArcxHudBorder
import com.example.ui.theme.ArcxSurface
import com.example.ui.theme.ArcxTextMuted
import com.example.ui.theme.ArcxTextPrimary
import com.example.ui.theme.ArcxTextSecondary

@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ARC-X",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp,
                    color = ArcxCyan
                )
                Text(
                    text = "ADVANCED REACTIVE COGNITIVE EXECUTIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = ArcxTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Personal Futuristic AI Companion",
                    fontSize = 12.sp,
                    color = ArcxTextMuted
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureBrief(
                    icon = Icons.Default.Bolt,
                    title = "Holographic ARC-X Core",
                    desc = "Tap the glowing central reactor to trigger voice recognition or interrupt responses."
                )
                FeatureBrief(
                    icon = Icons.Default.Person,
                    title = "3D Male Digital Companion",
                    desc = "A reactive digital character residing in the background who breathes and speaks in real-time."
                )
                FeatureBrief(
                    icon = Icons.Default.Mic,
                    title = "Voice Synthesis & Intelligence",
                    desc = "Spoken responses powered by Android Speech & Text-to-Speech engines."
                )
                FeatureBrief(
                    icon = Icons.Default.Shield,
                    title = "Privacy-First Architecture",
                    desc = "Microphone only activates when explicitly engaged. All memories stay on device."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_initialize_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ArcxCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "INITIALIZE ARC-X CORE",
                    fontWeight = FontWeight.Bold,
                    color = ArcxDeepSurface,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        containerColor = ArcxDeepSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(1.dp, ArcxHudBorder, RoundedCornerShape(16.dp))
    )
}

@Composable
private fun FeatureBrief(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = ArcxSurface,
            shape = CircleShape,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = ArcxCyan, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ArcxTextPrimary)
            Text(desc, fontSize = 11.sp, color = ArcxTextSecondary, lineHeight = 15.sp)
        }
    }
}
