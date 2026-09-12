package com.example.invoicely.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.invoicely.state.DashboardUiState

// 1. MAIN DASHBOARD SCREEN (The Switcher)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState
){
    // Canvas background color from your design system
    val canvasColor = Color(0xFFF6F5EC)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(canvasColor)
        .padding(16.dp)){
        // Yeh 'when' block automatically UI change karega jab ViewModel state update karega
        when(uiState){
            is DashboardUiState.Loading -> DashboardLoadingSkeleton()
            is DashboardUiState.Empty -> Text("Empty State UI will go here")
            is DashboardUiState.Error -> Text("Error State UI will go here: ${uiState.message}")
            is DashboardUiState.Success -> DashboardContent(data = uiState.data)
        }
    }
}

// 2. THE SHIMMER SKELETON (Bento Layout Mockup)
@Composable
fun DashboardLoadingSkeleton(){
    // a. Infinite Animation: Yeh skeleton blocks ko pulse (fade in/out) effect dega
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, // little light
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    // Shimmer ka base color (Light Gray with animated alpha)
    val shimmerColor = Color.LightGray.copy(alpha = alpha)
    // b. Bento Layout Blueprint
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        // Top row (Spacing for the translucent nav bar if needed, or just header)
        Spacer(modifier = Modifier.height(8.dp))

        // HERO CARD SKELETON (Full width, tall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(shimmerColor)
        )
        // PAIR CARDS SKELETON (2 cards side by side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ){
            Box(modifier = Modifier
                .weight(1f) // Half width
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(shimmerColor)
            )
            Box(
                modifier = Modifier
                    .weight(1f) // Half width
                    .height(140.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(shimmerColor)
            )
        }

        // QUICK ACTION STRIP SKELETON (3 small pills)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            repeat(3){
                // 3 same size ke boxes loop me bana diye
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(shimmerColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // LIST ITEMS SKELETON (Recent invoices list)
        repeat(4){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(shimmerColor)
            )
        }
    }
}

// 3. THE PREVIEW
@Preview(showBackground = true, backgroundColor = 0xFFF6F5EC)
@Composable
fun DashboardLoadingPreview() {
    MaterialTheme {
        // Preview me hum directly Loading state pass kar rahe hain
        DashboardScreen(uiState = DashboardUiState.Loading)
    }
}