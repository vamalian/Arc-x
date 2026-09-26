package com.example.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcxAlertRed
import com.example.ui.theme.ArcxCyan
import com.example.ui.theme.ArcxCyanGlow
import com.example.ui.theme.ArcxElectricBlue
import com.example.ui.theme.ArcxQuantumPurple
import com.example.ui.theme.ArcxTextMuted
import com.example.ui.theme.ArcxWarningAmber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcxCoreView(
    state: CoreState,
    audioLevel: Float, // 0f to 1f
    onCoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 230.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coreTransition")

    // Outer slow rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    CoreState.THINKING -> 3500
                    CoreState.LISTENING -> 5000
                    CoreState.SPEAKING -> 6000
                    else -> 14000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRot"
    )

    // Middle counter rotation
    val middleRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    CoreState.THINKING -> 2800
                    CoreState.LISTENING -> 4000
                    CoreState.SPEAKING -> 4500
                    else -> 10000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "middleRot"
    )

    // Inner pulsing core breathing
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    CoreState.THINKING -> 600
                    CoreState.LISTENING -> 900
                    CoreState.SPEAKING -> 750
                    CoreState.ERROR -> 400
                    else -> 2200
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    // Color transition based on state
    val primaryColor by animateColorAsState(
        targetValue = when (state) {
            CoreState.IDLE -> ArcxCyan
            CoreState.LISTENING -> ArcxCyanGlow
            CoreState.THINKING -> ArcxQuantumPurple
            CoreState.SPEAKING -> ArcxElectricBlue
            CoreState.ERROR -> ArcxAlertRed
            CoreState.OFFLINE -> ArcxTextMuted
        },
        animationSpec = tween(500),
        label = "primaryColor"
    )

    val secondaryColor by animateColorAsState(
        targetValue = when (state) {
            CoreState.IDLE -> ArcxElectricBlue
            CoreState.LISTENING -> ArcxCyan
            CoreState.THINKING -> ArcxWarningAmber
            CoreState.SPEAKING -> ArcxCyan
            CoreState.ERROR -> ArcxWarningAmber
            CoreState.OFFLINE -> Color(0xFF334155)
        },
        animationSpec = tween(500),
        label = "secondaryColor"
    )

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("arcx_core_button")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = sizeDp / 2, color = primaryColor),
                onClick = onCoreClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width / 2f - 8f

            // Dynamic reactive expansion based on audio volume
            val reactiveOffset = audioLevel * 18f

            // 1. Ambient Glow Field
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f + (audioLevel * 0.35f)),
                        secondaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.05f + reactiveOffset
                ),
                radius = baseRadius * 1.05f + reactiveOffset,
                center = center
            )

            // 2. Outermost Gyro Ring with Tech Ticks
            val outerRadius = baseRadius * 0.96f
            for (i in 0 until 48) {
                val tickAngle = (outerRotation + i * (360f / 48f)) * (PI / 180f)
                val isMajor = i % 6 == 0
                val tickLen = if (isMajor) 10f else 5f
                val rStart = outerRadius - tickLen
                val rEnd = outerRadius

                val start = Offset(
                    center.x + cos(tickAngle).toFloat() * rStart,
                    center.y + sin(tickAngle).toFloat() * rStart
                )
                val end = Offset(
                    center.x + cos(tickAngle).toFloat() * rEnd,
                    center.y + sin(tickAngle).toFloat() * rEnd
                )

                drawLine(
                    color = if (isMajor) primaryColor else primaryColor.copy(alpha = 0.35f),
                    start = start,
                    end = end,
                    strokeWidth = if (isMajor) 2f else 1.2f
                )
            }

            // 3. Middle Segmented Arcs (Rotating Opposite)
            val middleRadius = baseRadius * 0.78f + (reactiveOffset * 0.5f)
            val segments = 4
            val arcSpan = 65f
            for (i in 0 until segments) {
                val startAngle = middleRotation + i * (360f / segments)
                drawArc(
                    color = secondaryColor.copy(alpha = 0.85f),
                    startAngle = startAngle,
                    sweepAngle = arcSpan,
                    useCenter = false,
                    topLeft = Offset(center.x - middleRadius, center.y - middleRadius),
                    size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )

                // Outer accent dot on each arc
                val dotAngle = (startAngle + arcSpan / 2f) * (PI / 180f)
                drawCircle(
                    color = primaryColor,
                    radius = 3.5f,
                    center = Offset(
                        center.x + cos(dotAngle).toFloat() * middleRadius,
                        center.y + sin(dotAngle).toFloat() * middleRadius
                    )
                )
            }

            // 4. Voice-Reactive Energy Waveform Ring
            val waveRadius = baseRadius * 0.58f
            val wavePath = Path()
            val pointsCount = 60
            for (p in 0..pointsCount) {
                val angle = (p * (360f / pointsCount)) * (PI / 180f)
                // Modulate radius with sine wave and live audioLevel
                val waveDistortion = if (state == CoreState.LISTENING || state == CoreState.SPEAKING) {
                    sin(angle * 6.0 + outerRotation * 0.1).toFloat() * (audioLevel * 14f)
                } else {
                    sin(angle * 4.0 + outerRotation * 0.05).toFloat() * 3f
                }
                val r = waveRadius + waveDistortion
                val px = center.x + cos(angle).toFloat() * r
                val py = center.y + sin(angle).toFloat() * r

                if (p == 0) wavePath.moveTo(px, py) else wavePath.lineTo(px, py)
            }
            wavePath.close()

            drawPath(
                path = wavePath,
                color = primaryColor.copy(alpha = 0.9f),
                style = Stroke(width = 2.5f)
            )

            // 5. Central Reactor Sphere
            val reactorRadius = (baseRadius * 0.38f) * corePulse + (reactiveOffset * 0.4f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        secondaryColor,
                        primaryColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = reactorRadius
                ),
                radius = reactorRadius,
                center = center
            )

            // 6. Central Sci-Fi Emblem (ARC-X Core Symbol)
            val emblemRadius = reactorRadius * 0.48f
            drawCircle(
                color = Color.White.copy(alpha = 0.95f),
                radius = emblemRadius * 0.5f,
                center = center
            )
            // Surrounding Tri-Notches
            for (notch in 0 until 3) {
                val nAngle = (middleRotation * 1.5f + notch * 120f) * (PI / 180f)
                val nx = center.x + cos(nAngle).toFloat() * (emblemRadius * 1.15f)
                val ny = center.y + sin(nAngle).toFloat() * (emblemRadius * 1.15f)
                drawCircle(
                    color = primaryColor,
                    radius = 2.5f,
                    center = Offset(nx, ny)
                )
            }
        }
    }
}
