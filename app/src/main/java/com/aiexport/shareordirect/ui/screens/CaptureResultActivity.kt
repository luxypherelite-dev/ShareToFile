package com.aiexport.shareordirect.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.aiexport.shareordirect.data.AppViewModel
import com.aiexport.shareordirect.ui.theme.ShareToFileTheme

class CaptureResultActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transcript = intent?.getStringExtra("transcript")?.takeIf { it.isNotBlank() }
        if (transcript != null) {
            vm.processInput(transcript, "Overlay Capture")
        }
        setContent {
            ShareToFileTheme {
                ShareScreenContent(vm = vm, onBack = { finish() })
            }
        }
    }
}
