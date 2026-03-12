package com.kcloud.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kcloud.plugin.ShellLocalServerService
import org.koin.compose.koinInject
import site.addzero.notes.App
import site.addzero.notes.api.NotesApiClient

@Composable
fun KCloudNotesScreen(
    shellLocalServerService: ShellLocalServerService = koinInject()
) {
    val baseUrl by shellLocalServerService.baseUrl.collectAsState()

    LaunchedEffect(baseUrl) {
        baseUrl?.let { url ->
            NotesApiClient.setBaseUrl("$url/")
        }
    }

    if (baseUrl == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    App()
}
