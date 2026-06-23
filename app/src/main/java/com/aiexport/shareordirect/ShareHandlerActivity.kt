package com.aiexport.shareordirect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.aiexport.shareordirect.data.AppViewModel
import com.aiexport.shareordirect.ui.screens.ShareScreenContent
import com.aiexport.shareordirect.ui.theme.ShareToFileTheme

class ShareHandlerActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }
            if (sharedText != null) {
                val callingPkg = callingActivity?.packageName ?: ""
                val label = knownApps[callingPkg] ?: callingPkg
                vm.processInput(sharedText, label)
            }
        }
        setContent {
            ShareToFileTheme {
                ShareScreenContent(vm = vm, onBack = { finish() })
            }
        }
    }

    companion object {
        private val knownApps = mapOf(
            "com.openai.chatgpt"           to "ChatGPT",
            "com.google.android.apps.bard" to "Gemini",
            "com.anthropic.claude"         to "Claude",
            "com.microsoft.bing"           to "Copilot",
            "com.perplexity.app"           to "Perplexity",
        )
    }
}
