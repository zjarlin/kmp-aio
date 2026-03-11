package com.kcloud.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kcloud.ui.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("设置页面", style = MaterialTheme.typography.headlineMedium)
    }
}
