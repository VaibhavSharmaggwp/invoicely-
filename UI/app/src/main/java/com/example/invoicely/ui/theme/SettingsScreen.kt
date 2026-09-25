package com.example.invoicely.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userName: String = "Vaibhav Sharma", // Will come from ViewModel later
    businessName: String = "Invoicely HQ",
    onNavigateToCompanyDetails: () -> Unit = {},
    onNavigateToBankUpi: () -> Unit = {},

    onLogoutClick: () -> Unit
){
    val canvasBg = Color(0xFFF6F5EC)
    val inkColor = Color(0xFF151A11)
    val chartreuseColor = Color(0xFFDCEF3C)
    val whiteCard = Color(0xFFFFFFFF)
    val cardBorder = Color(0xFFE5E3D8)
    val subtextColor = Color(0xFF73786D)

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = inkColor,
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = canvasBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // --- 1. PROFILE HERO CARD ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(whiteCard)
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(inkColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            color = chartreuseColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = OutfitFontFamily,
                            color = inkColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = businessName,
                            fontSize = 13.sp,
                            fontFamily = OutfitFontFamily,
                            color = subtextColor
                        )
                    }

                    // Workspace Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1EFE6))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 10.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = inkColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // --- 2. BUSINESS SETUP ---
            item {
                Text(
                    text = "BUSINESS SETUP",
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = subtextColor,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(whiteCard)
                        .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                ) {
                    SettingsRow(
                        icon = Icons.Outlined.Business,
                        title = "Company Details",
                        subtitle = "Logo, GSTIN, Address",
                        onClick =onNavigateToCompanyDetails
                    )
                    HorizontalDivider(color = Color(0xFFF6F5EC), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Outlined.AccountBalance,
                        title = "Bank & UPI Links",
                        subtitle = "Beneficiary IFSC, A/C & VPA settlement rail",
                        onClick = onNavigateToBankUpi

                    )
                }
            }

            // --- 3. APP PREFERENCES ---
            item {
                Text(
                    text = "APP PREFERENCES",
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = subtextColor,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(whiteCard)
                        .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                ) {
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Push Notifications",
                        subtitle = "Overdue alerts & payment updates"
                    )
                    HorizontalDivider(color = Color(0xFFF6F5EC), thickness = 1.dp)
                    SettingsRow(
                        icon = Icons.Outlined.Person,
                        title = "Account Security",
                        subtitle = "Password & 2FA"
                    )
                }
            }

            // --- 4. LOGOUT & FOOTER ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEBEE),
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Secure Logout",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = OutfitFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Subtle app version footer
                Text(
                    text = "Invoicely v1.0 • Neo-Fintech Edition",
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = Color(0xFFA0A59A),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(90.dp)) // Nav bar padding
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF6F5EC)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF151A11),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = OutfitFontFamily,
                color = Color(0xFF151A11)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontFamily = OutfitFontFamily,
                color = Color(0xFF73786D)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFB4B2A8),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(onLogoutClick = {})
    }
}