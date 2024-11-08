package com.example.mediaplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mediaplayer.ui.theme.MediaPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(modifier = Modifier.padding(innerPadding), this)
                }
            }
        }
    }
}

@Composable
fun AppContent(modifier: Modifier = Modifier, context: Context) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        var isPlaying by rememberSaveable { mutableStateOf(false) }

        // Start or stop the service based on the play state
        fun togglePlayback() {
            val intent = Intent(context, ForegroundService::class.java)
            if (isPlaying) {
                context.stopService(intent)
            } else {
                intent.action = ForegroundService.ACTION_START
                context.startService(intent)
            }
            isPlaying = !isPlaying
        }

        Button(onClick = { togglePlayback() }) {
            Text(if (isPlaying) "Stop Audio" else "Play Audio")
        }
    }
}

