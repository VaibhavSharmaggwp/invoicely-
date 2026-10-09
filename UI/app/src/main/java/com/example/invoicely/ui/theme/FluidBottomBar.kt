package com.example.invoicely.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * Tab specification for Fluid Navigation with signature color palette
 */
data class FluidTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val primaryAccent: Color,
    val secondaryAccent: Color
)

val defaultFluidTabs = listOf(
    FluidTab(
        route = "dashboard",
        label = "Home",
        icon = Icons.Default.Home,
        primaryAccent = Color(0xFFDCEF3C), // Invoicely Chartreuse
        secondaryAccent = Color(0xFFA3E635)
    ),
    FluidTab(
        route = "ledger",
        label = "Invoices",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        primaryAccent = Color(0xFF2DD4BF), // Mint Teal
        secondaryAccent = Color(0xFF10B981)
    ),
    FluidTab(
        route = "history",
        label = "History",
        icon = Icons.Default.History,
        primaryAccent = Color(0xFFFBBF24), // Solar Gold
        secondaryAccent = Color(0xFFF59E0B)
    ),
    FluidTab(
        route = "settings",
        label = "Settings",
        icon = Icons.Default.Settings,
        primaryAccent = Color(0xFFA78BFA), // Titanium Violet
        secondaryAccent = Color(0xFF818CF8)
    )
)

/**
 * Linear color interpolation helper
 */
private fun lerpColor(c1: Color, c2: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = c1.red + (c2.red - c1.red) * f,
        green = c1.green + (c2.green - c1.green) * f,
        blue = c1.blue + (c2.blue - c1.blue) * f,
        alpha = c1.alpha + (c2.alpha - c1.alpha) * f
    )
}

/**
 * Helper to interpolate tab colors based on continuous float index
 */
private fun interpolateColorForProgress(tabs: List<FluidTab>, progress: Float, isPrimary: Boolean): Color {
    if (tabs.isEmpty()) return Color.White
    val clamped = progress.coerceIn(0f, (tabs.size - 1).toFloat())
    val lowerIndex = clamped.toInt()
    val upperIndex = (lowerIndex + 1).coerceAtMost(tabs.size - 1)
    val fraction = clamped - lowerIndex

    val color1 = if (isPrimary) tabs[lowerIndex].primaryAccent else tabs[lowerIndex].secondaryAccent
    val color2 = if (isPrimary) tabs[upperIndex].primaryAccent else tabs[upperIndex].secondaryAccent
    return lerpColor(color1, color2, fraction)
}

@Composable
fun FluidBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<FluidTab> = defaultFluidTabs
) {
    val activeIndex = remember(currentRoute, tabs) {
        val idx = tabs.indexOfFirst { it.route == currentRoute }
        if (idx >= 0) idx else 0
    }

    // 💧 LIQUID SELECTION PHYSICS:
    // Leading edge (head) springs ahead with fast momentum,
    // Trailing edge (tail) follows with elastic delay,
    // producing momentum stretch and wobble.
    val headProgress by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.64f, // Dynamic wobble
            stiffness = 420f      // Swift momentum
        ),
        label = "fluidHead"
    )

    val tailProgress by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.72f, // Elastic follow
            stiffness = 270f      // Elastic drag
        ),
        label = "fluidTail"
    )

    // Current blended liquid palette colors based on fluid progress
    val currentAccent = interpolateColorForProgress(tabs, headProgress, isPrimary = true)
    val currentSecondary = interpolateColorForProgress(tabs, headProgress, isPrimary = false)

    // Design Tokens matching Invoicely
    val dockBgColor = Color(0xFF141911)
    val dockBorderColor = Color(0xFF263121)
    val inactiveColor = Color(0xFF7E8679)

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Sculpted Dock Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = currentAccent.copy(alpha = 0.25f),
                    spotColor = currentAccent.copy(alpha = 0.45f)
                )
                .clip(RoundedCornerShape(34.dp))
                .background(dockBgColor)
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            dockBorderColor,
                            currentAccent.copy(alpha = 0.35f),
                            dockBorderColor
                        )
                    ),
                    shape = RoundedCornerShape(34.dp)
                )
                // 👆 DRAG-TO-SELECT GESTURE
                .pointerInput(tabs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val tabWidth = size.width / tabs.size
                            val selectedIndex = (offset.x / tabWidth).toInt().coerceIn(0, tabs.size - 1)
                            if (tabs[selectedIndex].route != currentRoute) {
                                onNavigate(tabs[selectedIndex].route)
                            }
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val tabWidth = size.width / tabs.size
                            val selectedIndex = (change.position.x / tabWidth).toInt().coerceIn(0, tabs.size - 1)
                            if (tabs[selectedIndex].route != currentRoute) {
                                onNavigate(tabs[selectedIndex].route)
                            }
                        }
                    )
                }
        ) {
            // 💧 LIQUID BLOB CANVAS (Custom draw behind tabs)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalWidth = size.width
                val tabCount = tabs.size
                if (tabCount == 0) return@Canvas

                val tabWidth = totalWidth / tabCount
                val headX = (headProgress + 0.5f) * tabWidth
                val tailX = (tailProgress + 0.5f) * tabWidth

                val minX = min(headX, tailX)
                val maxX = max(headX, tailX)

                val restingPillWidth = tabWidth * 0.74f
                val restingPillHeight = 46.dp.toPx()

                // Calculate momentum stretch
                val blobLeft = minX - restingPillWidth / 2f
                val blobRight = maxX + restingPillWidth / 2f
                val blobWidth = blobRight - blobLeft

                // Volume preservation: slight vertical squeeze during horizontal stretch
                val stretchDistance = (maxX - minX).absoluteValue
                val heightCompression = (stretchDistance / tabWidth * 4.dp.toPx()).coerceAtMost(5.dp.toPx())
                val blobHeight = restingPillHeight - heightCompression

                val blobTop = (size.height - blobHeight) / 2f
                val blobCenter = Offset((blobLeft + blobRight) / 2f, size.height / 2f)

                // 1. Soft radial fluid glow halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            currentAccent.copy(alpha = 0.28f),
                            currentAccent.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = blobCenter,
                        radius = blobWidth * 0.9f
                    ),
                    center = blobCenter,
                    radius = blobWidth * 0.9f
                )

                // 2. Liquid Capsule Body
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            currentSecondary.copy(alpha = 0.22f),
                            currentAccent.copy(alpha = 0.32f),
                            currentSecondary.copy(alpha = 0.22f)
                        ),
                        startX = blobLeft,
                        endX = blobRight
                    ),
                    topLeft = Offset(blobLeft, blobTop),
                    size = Size(blobWidth, blobHeight),
                    cornerRadius = CornerRadius(blobHeight / 2f, blobHeight / 2f)
                )

                // 3. Subtle luminous edge ring
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            currentSecondary.copy(alpha = 0.55f),
                            currentAccent.copy(alpha = 0.85f),
                            currentSecondary.copy(alpha = 0.55f)
                        ),
                        startX = blobLeft,
                        endX = blobRight
                    ),
                    topLeft = Offset(blobLeft, blobTop),
                    size = Size(blobWidth, blobHeight),
                    cornerRadius = CornerRadius(blobHeight / 2f, blobHeight / 2f),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // 4. TAB ICONS & MICRO-TYPOGRAPHY
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == activeIndex

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.16f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "iconScale_$index"
                    )

                    val itemColor by animateColorAsState(
                        targetValue = if (isSelected) tab.primaryAccent else inactiveColor,
                        animationSpec = tween(durationMillis = 240),
                        label = "tabColor_$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onNavigate(tab.route)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = itemColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(iconScale)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tab.label,
                                color = itemColor,
                                fontSize = 11.sp,
                                fontFamily = OutfitFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = if (isSelected) 0.3.sp else 0.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
