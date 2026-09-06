package com.example.invoicely.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.invoicely.R

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit = {}) {
    val isPreview = LocalInspectionMode.current

    // 1. Load the JSON file from the raw folder
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))

    // 2. Track the progress of the animation (from 0.0 to 1.0)
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = 1 // Sirf ek baar play karna hai
    )

    // 3. The trigger: Jab animation khatam ho jaye, move to next screen
    LaunchedEffect(progress) {
        if (progress == 1.0f) {
            onAnimationFinished()
        }
    }

    // 4. The UI: Center everything on the Canvas color from your design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F5EC)),
        contentAlignment = Alignment.Center
    ) {
        if (isPreview) {
            Text(
                text = "Invoicely",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF151A11)
            )
        } else {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(250.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen()
    }
}