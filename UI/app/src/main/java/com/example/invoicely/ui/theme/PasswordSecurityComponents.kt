package com.example.invoicely.ui.theme



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. STRENGTH ENUM
enum class PasswordStrength { EMPTY, WEAK, MEDIUM, STRONG }

// 2. STRENGTH CALCULATOR FUNCTION
// Yeh function real-time check karega ki password kitna strong hai
fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.EMPTY

    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { "!@#$%^&*()_+=-/?<>".contains(it) }) score++

    return when {
        score <= 2 -> PasswordStrength.WEAK
        score in 3..4 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }
}

// 3. THE ANIMATED INDICATOR COMPONENT
@Composable
fun PasswordStrengthIndicator(password: String) {
    val strength = calculatePasswordStrength(password)

    // Animation States: Width multiplier (0.33, 0.66, 1.0) aur Color
    val targetWidth = when (strength) {
        PasswordStrength.EMPTY -> 0f
        PasswordStrength.WEAK -> 0.33f
        PasswordStrength.MEDIUM -> 0.66f
        PasswordStrength.STRONG -> 1f
    }

    val targetColor = when (strength) {
        PasswordStrength.EMPTY -> Color.Transparent
        PasswordStrength.WEAK -> Color(0xFFD32F2F) // Red
        PasswordStrength.MEDIUM -> Color(0xFFF57C00) // Orange
        PasswordStrength.STRONG -> Color(0xFF2FA84F) // Green
    }

    val targetText = when (strength) {
        PasswordStrength.EMPTY -> ""
        PasswordStrength.WEAK -> "Weak - Add numbers & symbols"
        PasswordStrength.MEDIUM -> "Medium - Add uppercase/special chars"
        PasswordStrength.STRONG -> "Strong - Looks good!"
    }

    val animatedWidth by animateFloatAsState(targetValue = targetWidth, animationSpec = tween(500))
    val animatedColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(500))

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        // The Progress Bar Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFEBE9DF)) // Light Gray track
        ) {
            // The Animated Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedWidth)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(animatedColor)
            )
        }

        // Helper Text
        if (strength != PasswordStrength.EMPTY) {
            Text(
                text = targetText,
                color = animatedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
            )
        }
    }
}

// 4. PASSWORD MATCH VALIDATOR COMPONENT
@Composable
fun PasswordMatchIndicator(password: String, confirmPassword: String) {
    // Agar dono khali hain, toh kuch mat dikhao
    if (confirmPassword.isEmpty()) return

    val isMatch = password == confirmPassword
    val icon = if (isMatch) Icons.Default.CheckCircle else Icons.Default.ErrorOutline
    val color = if (isMatch) Color(0xFF2FA84F) else Color(0xFFD32F2F)
    val text = if (isMatch) "Passwords match" else "Passwords do not match"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, start = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}