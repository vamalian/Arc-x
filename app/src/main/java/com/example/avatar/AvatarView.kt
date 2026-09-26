package com.example.avatar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ArcxAlertRed
import com.example.ui.theme.ArcxCyan
import com.example.ui.theme.ArcxElectricBlue
import com.example.ui.theme.ArcxVoid
import com.example.ui.theme.ArcxWarningAmber
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AvatarView(
    state: AvatarState,
    audioAmplitude: Float,
    intensity: Float = 0.85f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarAnim")

    // Breathing scale animation
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (state == AvatarState.SLEEP) 1.01f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AvatarState.SLEEP) 4500 else 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    // Breathing vertical offset
    val breathOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (state == AvatarState.SLEEP) 4f else 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == AvatarState.SLEEP) 4500 else 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathOffset"
    )

    // Subtle head tilt / turn for listening or thinking
    val attentivenessAngle = when (state) {
        AvatarState.LISTENING -> -1.5f
        AvatarState.THINKING -> 2.0f
        AvatarState.ALERT -> 0.0f
        else -> 0.0f
    }

    // Holographic scan line position
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLineY"
    )

    // Neural pulse phase
    val neuralPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neuralPulse"
    )

    val baseAlpha = when (state) {
        AvatarState.SLEEP -> 0.25f
        AvatarState.IDLE -> 0.70f * intensity
        AvatarState.LISTENING -> 0.85f * intensity
        AvatarState.THINKING -> 0.75f * intensity
        AvatarState.SPEAKING -> (0.80f + audioAmplitude * 0.20f) * intensity
        AvatarState.ALERT -> 0.90f * intensity
    }

    val stateTint = when (state) {
        AvatarState.ALERT -> ArcxAlertRed.copy(alpha = 0.25f)
        AvatarState.THINKING -> ArcxWarningAmber.copy(alpha = 0.12f)
        AvatarState.LISTENING -> ArcxCyan.copy(alpha = 0.15f)
        AvatarState.SPEAKING -> ArcxElectricBlue.copy(alpha = 0.15f + audioAmplitude * 0.15f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(baseAlpha)
            .graphicsLayer {
                val voiceExpansion = if (state == AvatarState.SPEAKING) audioAmplitude * 0.025f else 0f
                scaleX = breathScale + voiceExpansion
                scaleY = breathScale + voiceExpansion
                translationY = breathOffset
                rotationZ = attentivenessAngle
            },
        contentAlignment = Alignment.Center
    ) {
        // 3D Male Digital Companion Character Render
        Image(
            painter = painterResource(id = R.drawable.img_arcx_avatar),
            contentDescription = "ARC-X 3D Male AI Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // State Tint & Vignette Shader
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ArcxVoid.copy(alpha = 0.85f),
                            stateTint,
                            ArcxVoid.copy(alpha = 0.6f),
                            ArcxVoid.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Radial shadow to keep center Core focused
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            ArcxVoid.copy(alpha = 0.5f),
                            ArcxVoid.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Procedural Hologram & Neural Energy Overlays
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Futuristic Scanline
            val scanY = height * scanLineY
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        ArcxCyan.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.5f),
                        ArcxCyan.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, scanY),
                end = Offset(width, scanY),
                strokeWidth = 2.5f
            )

            // 2. State-specific holographic FX
            if (state == AvatarState.SPEAKING && audioAmplitude > 0.05f) {
                // Speech resonance ripples around avatar chest/voice
                val chestY = height * 0.42f
                val rippleRadius = (width * 0.25f) + (audioAmplitude * 90f)
                drawCircle(
                    color = ArcxCyan.copy(alpha = audioAmplitude * 0.4f),
                    radius = rippleRadius,
                    center = Offset(width * 0.5f, chestY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                )
                drawCircle(
                    color = ArcxElectricBlue.copy(alpha = audioAmplitude * 0.3f),
                    radius = rippleRadius * 1.35f,
                    center = Offset(width * 0.5f, chestY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
            }

            if (state == AvatarState.LISTENING) {
                // Audio receptive reticle at eye level
                val eyeLevelY = height * 0.28f
                drawCircle(
                    color = ArcxCyan.copy(alpha = neuralPulse * 0.5f),
                    radius = 28f * neuralPulse,
                    center = Offset(width * 0.5f, eyeLevelY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
            }

            if (state == AvatarState.THINKING) {
                // Data synthesis orbits
                val headY = height * 0.28f
                for (i in 0..4) {
                    val angle = (neuralPulse * 360f + i * 72f) * (Math.PI / 180.0)
                    val px = width * 0.5f + cos(angle).toFloat() * 110f
                    val py = headY + sin(angle).toFloat() * 28f
                    drawCircle(
                        color = ArcxWarningAmber.copy(alpha = 0.7f),
                        radius = 3.5f,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
