package com.example.invoicely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.invoicely.ui.theme.AppNavigation
import com.example.invoicely.ui.theme.InvoicelyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InvoicelyTheme {
                AppNavigation()
            }
        }
    }
}   