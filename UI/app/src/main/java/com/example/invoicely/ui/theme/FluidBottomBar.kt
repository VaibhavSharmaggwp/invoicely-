package com.example.invoicely.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Highly optimized, GPU-accelerated Fluid Liquid Bottom Bar
 * Built with dual-spring momentum stretch physics, glowing liquid capsule,
 * and tactile expand/contract micro-animations.
 */
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

    val activeTab = tabs.getOrElse(activeIndex) { tabs.first() }

    // 💧 DUAL-SPRING LIQUID MOMENTUM PHYSICS:
    // Head moves fast with high stiffness,
    // Tail follows with elastic delay,
    // producing momentum stretch and silky organic rebound.
    val headIndex by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.70f,
            stiffness = 380f
        ),
        label = "fluidHead"
    )

    val tailIndex by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = 250f
        ),
        label = "fluidTail"
    )

    // Animated glow color transition
    val activeAccent by animateColorAsState(
        targetValue = activeTab.primaryAccent,
        animationSpec = tween(durationMillis = 260),
        label = "activeAccent"
    )

    // Design Tokens
    val dockBgColor = Color(0xFF131711)
    val dockBorderColor = Color(0xFF242C1F)
    val inactiveIconColor = Color(0xFF7E8679)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Sculpted Dock Pill
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = activeAccent.copy(alpha = 0.20f),
                    spotColor = activeAccent.copy(alpha = 0.35f)
                )
                .clip(RoundedCornerShape(34.dp))
                .background(dockBgColor)
                .border(
                    width = 1.dp,
                    color = dockBorderColor,
                    shape = RoundedCornerShape(34.dp)
                )
                // 👆 INTERACTIVE DRAG-TO-SELECT
                .pointerInput(tabs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val tabWidthPx = size.width / tabs.size
                            val selectedIndex = (offset.x / tabWidthPx).toInt().coerceIn(0, tabs.size - 1)
                            if (tabs[selectedIndex].route != currentRoute) {
                                onNavigate(tabs[selectedIndex].route)
                            }
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val tabWidthPx = size.width / tabs.size
                            val selectedIndex = (change.position.x / tabWidthPx).toInt().coerceIn(0, tabs.size - 1)
                            if (tabs[selectedIndex].route != currentRoute) {
                                onNavigate(tabs[selectedIndex].route)
                            }
                        }
                    )
                }
        ) {
            val totalWidth = maxWidth
            val tabCount = tabs.size
            if (tabCount > 0) {
                val tabWidth = totalWidth / tabCount

                // Momentum stretch math
                val minIdx = min(headIndex, tailIndex)
                val maxIdx = max(headIndex, tailIndex)
                val stretchFactor = maxIdx - minIdx // 0.0 at rest, elongates during flight

                val restingPillWidth = (tabWidth * 0.90f).coerceAtMost(tabWidth - 4.dp)
                val pillWidth = restingPillWidth + tabWidth * (stretchFactor * 0.35f)
                val pillHeight = 52.dp

                val centerIdx = (headIndex + tailIndex) / 2f
                val pillOffsetX = (tabWidth * centerIdx) + (tabWidth - pillWidth) / 2f

                // 💧 1. AMBIENT GLOW BACKDROP
                Box(
                    modifier = Modifier
                        .offset(x = pillOffsetX - 6.dp, y = (68.dp - (pillHeight + 8.dp)) / 2)
                        .width(pillWidth + 12.dp)
                        .height(pillHeight + 8.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    activeAccent.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape((pillHeight + 8.dp) / 2)
                        )
                )

                // 💧 2. LIQUID SELECTION CAPSULE BODY
                Box(
                    modifier = Modifier
                        .offset(x = pillOffsetX, y = (68.dp - pillHeight) / 2)
                        .width(pillWidth)
                        .height(pillHeight)
                        .clip(RoundedCornerShape(pillHeight / 2))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    activeTab.secondaryAccent.copy(alpha = 0.16f),
                                    activeAccent.copy(alpha = 0.22f),
                                    activeTab.secondaryAccent.copy(alpha = 0.16f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    activeTab.secondaryAccent.copy(alpha = 0.45f),
                                    activeAccent.copy(alpha = 0.75f),
                                    activeTab.secondaryAccent.copy(alpha = 0.45f)
                                )
                            ),
                            shape = RoundedCornerShape(pillHeight / 2)
                        )
                )
            }

            // 3. TAB ICONS & DYNAMIC REVEAL LABELS
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == activeIndex

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.06f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "iconScale_$index"
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
                                tint = if (isSelected) activeAccent else inactiveIconColor,
                                modifier = Modifier
                                    .size(21.dp)
                                    .scale(iconScale)
                            )

                            // Label expands dynamically when active
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn(tween(160)) + expandVertically(tween(160)),
                                exit = fadeOut(tween(120)) + shrinkVertically(tween(120))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tab.label,
                                        color = activeAccent,
                                        fontSize = 10.sp,
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.2.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
