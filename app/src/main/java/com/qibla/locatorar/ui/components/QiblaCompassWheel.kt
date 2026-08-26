package com.qibla.locatorar.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val EmeraldPrimary = Color(0xFF0F7655)
private val EmeraldDark = Color(0xFF064E3B)
private val EmeraldAccent = Color(0xFF10B981)
private val GoldAccent = Color(0xFFD4AF37)
private val GoldLight = Color(0xFFFFE082)
private val DarkCharcoal = Color(0xFF1E293B)
private val SoftSlate = Color(0xFF64748B)
private val BorderTrack = Color(0xFFE2E8F0)
private val NeedleLight = Color(0xFF34D399)
private val NeedleDark = Color(0xFF047857)

@Composable
fun QiblaCompassWheel(
    heading: Float,
    qiblaBearing: Float,
    modifier: Modifier = Modifier
) {
    var currentRotation by remember { mutableFloatStateOf(0f) }
    var lastHeading by remember { mutableFloatStateOf(0f) }

    val delta = heading - lastHeading
    val shortestDelta = ((delta + 540f) % 360f) - 180f
    currentRotation -= shortestDelta
    lastHeading = heading

    val animatedRotation by animateFloatAsState(
        targetValue = currentRotation,
        animationSpec = spring(stiffness = 450f, dampingRatio = 0.85f),
        label = "CompassRotation"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.minDimension / 2f) - 18.dp.toPx()
            val dialRadius = outerRadius - 10.dp.toPx()

            // 1. Outer Metallic Bezel Rings
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFF1F5F9), Color(0xFFCBD5E1)),
                    center = center,
                    radius = outerRadius + 8.dp.toPx()
                ),
                radius = outerRadius + 6.dp.toPx(),
                center = center
            )
            drawCircle(
                color = BorderTrack,
                radius = outerRadius + 6.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Outer Emerald Accent Rim
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(EmeraldPrimary, EmeraldAccent, EmeraldDark, EmeraldPrimary),
                    center = center
                ),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 4.5.dp.toPx())
            )

            // Dial Background Surface
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFAFCFB), Color(0xFFF0FDF4), Color(0xFFE2E8F0)),
                    center = center,
                    radius = dialRadius
                ),
                radius = dialRadius,
                center = center
            )

            // Decorative Geometric Inner Track
            drawCircle(
                color = EmeraldPrimary.copy(alpha = 0.12f),
                radius = dialRadius * 0.72f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = GoldAccent.copy(alpha = 0.25f),
                radius = dialRadius * 0.40f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Rotating Wheel Elements
            rotate(animatedRotation, pivot = center) {
                val cardinalPoints = mapOf(
                    0 to "N",
                    90 to "E",
                    180 to "S",
                    270 to "W"
                )

                // Subdivided Precision Ticks
                for (deg in 0 until 360 step 5) {
                    val angleRad = (deg - 90) * (PI / 180f).toFloat()
                    val isMajor = deg % 90 == 0
                    val isMedium = deg % 30 == 0 && !isMajor
                    val isMinor = deg % 5 == 0 && !isMajor && !isMedium

                    val tickLength = when {
                        isMajor -> 14.dp.toPx()
                        isMedium -> 9.dp.toPx()
                        else -> 5.dp.toPx()
                    }

                    val tickColor = when {
                        isMajor -> if (deg == 0) GoldAccent else EmeraldPrimary
                        isMedium -> DarkCharcoal.copy(alpha = 0.7f)
                        else -> SoftSlate.copy(alpha = 0.35f)
                    }

                    val strokeW = when {
                        isMajor -> 2.5.dp.toPx()
                        isMedium -> 1.5.dp.toPx()
                        else -> 1.dp.toPx()
                    }

                    val innerOffset = Offset(
                        x = center.x + (dialRadius - 6.dp.toPx() - tickLength) * cos(angleRad),
                        y = center.y + (dialRadius - 6.dp.toPx() - tickLength) * sin(angleRad)
                    )
                    val outerOffset = Offset(
                        x = center.x + (dialRadius - 6.dp.toPx()) * cos(angleRad),
                        y = center.y + (dialRadius - 6.dp.toPx()) * sin(angleRad)
                    )

                    drawLine(
                        color = tickColor,
                        start = innerOffset,
                        end = outerOffset,
                        strokeWidth = strokeW
                    )
                }

                // Upright Cardinal Letters
                cardinalPoints.forEach { (deg, label) ->
                    val angleRad = (deg - 90) * (PI / 180f).toFloat()
                    val labelRadius = dialRadius - 32.dp.toPx()
                    val textPos = Offset(
                        x = center.x + labelRadius * cos(angleRad),
                        y = center.y + labelRadius * sin(angleRad)
                    )

                    rotate(-animatedRotation, pivot = textPos) {
                        drawContext.canvas.nativeCanvas.apply {
                            val isNorth = deg == 0
                            val paint = Paint().apply {
                                color = if (isNorth) GoldAccent.toArgb() else DarkCharcoal.toArgb()
                                textSize = (if (isNorth) 17 else 15).dp.toPx()
                                textAlign = Paint.Align.CENTER
                                isFakeBoldText = true
                                isAntiAlias = true
                            }
                            drawText(label, textPos.x, textPos.y + 6.dp.toPx(), paint)
                        }
                    }
                }
                // Polished Kaaba Floating Capsule
                val badgeAngleRad = (qiblaBearing - 90) * (PI / 180f).toFloat()
                val badgeRadius = outerRadius + 2.dp.toPx()
                val badgeCenter = Offset(
                    x = center.x + badgeRadius * cos(badgeAngleRad),
                    y = center.y + badgeRadius * sin(badgeAngleRad)
                )

                // Outer Shadow & Gold Rim
                drawCircle(
                    color = Color.Black.copy(alpha = 0.15f),
                    radius = 21.dp.toPx(),
                    center = Offset(badgeCenter.x, badgeCenter.y + 2.dp.toPx())
                )
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(GoldLight, GoldAccent),
                        start = Offset(badgeCenter.x - 20.dp.toPx(), badgeCenter.y - 20.dp.toPx()),
                        end = Offset(badgeCenter.x + 20.dp.toPx(), badgeCenter.y + 20.dp.toPx())
                    ),
                    radius = 20.dp.toPx(),
                    center = badgeCenter
                )
                drawCircle(
                    color = EmeraldDark,
                    radius = 17.dp.toPx(),
                    center = badgeCenter
                )

                // --- NEW: Precision Notch — points from badge toward wheel center ---
                run {
                    // Unit vector pointing from badge toward the dial center
                    val towardCenter = Offset(-cos(badgeAngleRad), -sin(badgeAngleRad))
                    // Perpendicular vector, for the notch's base width
                    val perp = Offset(-sin(badgeAngleRad), cos(badgeAngleRad))

                    val notchBaseDist = 20.dp.toPx()   // sits right on the badge's inner edge
                    val notchTipDist = 20.dp.toPx() + 11.dp.toPx() // apex extends inward toward center
                    val notchHalfWidth = 7.dp.toPx()

                    val baseCenter = Offset(
                        x = badgeCenter.x + towardCenter.x * notchBaseDist,
                        y = badgeCenter.y + towardCenter.y * notchBaseDist
                    )
                    val tip = Offset(
                        x = badgeCenter.x + towardCenter.x * notchTipDist,
                        y = badgeCenter.y + towardCenter.y * notchTipDist
                    )
                    val baseLeft = Offset(
                        baseCenter.x + perp.x * notchHalfWidth,
                        baseCenter.y + perp.y * notchHalfWidth
                    )
                    val baseRight = Offset(
                        baseCenter.x - perp.x * notchHalfWidth,
                        baseCenter.y - perp.y * notchHalfWidth
                    )

                    val notchPath = Path().apply {
                        moveTo(baseLeft.x, baseLeft.y)
                        lineTo(tip.x, tip.y)
                        lineTo(baseRight.x, baseRight.y)
                        close()
                    }

                    // Subtle shadow first, for depth against the dial
                    drawPath(
                        path = notchPath,
                        color = Color.Black.copy(alpha = 0.18f)
                    )
                    drawPath(
                        path = notchPath,
                        brush = Brush.linearGradient(
                            colors = listOf(GoldAccent, GoldAccent)
                        )
                    )
                    drawPath(
                        path = notchPath,
                        color = EmeraldDark.copy(alpha = 0.4f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                // --- END notch ---

                // Upright Kaaba Icon
                rotate(-animatedRotation, pivot = badgeCenter) {
                    val kWidth = 7.5.dp.toPx()
                    val kHeight = 9.dp.toPx()
                    val cubeTopLeft = Offset(badgeCenter.x - kWidth, badgeCenter.y - kHeight)

                    drawRoundRect(
                        color = Color(0xFF18181B),
                        topLeft = cubeTopLeft,
                        size = Size(kWidth * 2, kHeight * 2),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )

                    // Kiswah Gold Stripe & Door
                    drawLine(
                        brush = Brush.horizontalGradient(listOf(GoldLight, GoldAccent)),
                        start = Offset(cubeTopLeft.x, cubeTopLeft.y + 4.5.dp.toPx()),
                        end = Offset(cubeTopLeft.x + (kWidth * 2), cubeTopLeft.y + 4.5.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawRoundRect(
                        color = GoldAccent,
                        topLeft = Offset(badgeCenter.x + 1.dp.toPx(), cubeTopLeft.y + 8.dp.toPx()),
                        size = Size(3.5.dp.toPx(), 6.5.dp.toPx()),
                        cornerRadius = CornerRadius(1.dp.toPx())
                    )
                }
            }

            // 3. Central Fixed Target Needle (Facet-shaded & crisp)
            val needleLen = dialRadius * 0.68f
            val baseWidth = 16.dp.toPx()
            val tailLen = 14.dp.toPx()

            // Subtle Drop Shadow for Arrow
            val shadowPath = Path().apply {
                moveTo(center.x, center.y - needleLen + 2.dp.toPx())
                lineTo(center.x - baseWidth, center.y + 2.dp.toPx())
                lineTo(center.x, center.y + tailLen + 2.dp.toPx())
                lineTo(center.x + baseWidth, center.y + 2.dp.toPx())
                close()
            }
            drawPath(shadowPath, color = Color.Black.copy(alpha = 0.08f))

            // Left Highlight Facet
            val leftFacet = Path().apply {
                moveTo(center.x, center.y - needleLen)
                lineTo(center.x - baseWidth, center.y)
                lineTo(center.x, center.y + tailLen)
                close()
            }
            drawPath(leftFacet, NeedleLight)

            // Right Shaded Facet
            val rightFacet = Path().apply {
                moveTo(center.x, center.y - needleLen)
                lineTo(center.x + baseWidth, center.y)
                lineTo(center.x, center.y + tailLen)
                close()
            }
            drawPath(rightFacet, NeedleDark)

            // 4. Center Cap / Pivot Hub
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldLight, GoldAccent),
                    center = center,
                    radius = 6.dp.toPx()
                ),
                radius = 6.dp.toPx(),
                center = center
            )
            drawCircle(
                color = EmeraldDark,
                radius = 2.5.dp.toPx(),
                center = center
            )
        }
    }
}

@Preview(showBackground = true, name = "Qibla Compass Wheel - Facing North")
@Composable
fun QiblaCompassWheelPreview() {
    QiblaCompassWheel(
        heading = 0f,
        qiblaBearing = 292f,
        modifier = Modifier
            .size(330.dp)
            .padding(16.dp)
    )
}

@Preview(showBackground = true, name = "Qibla Compass Wheel - Rotated 135°")
@Composable
fun QiblaCompassWheelRotatedPreview() {
    QiblaCompassWheel(
        heading = 135f,
        qiblaBearing = 292f,
        modifier = Modifier
            .size(330.dp)
            .padding(16.dp)
    )
}