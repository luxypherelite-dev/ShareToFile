package com.aiexport.shareordirect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.aiexport.shareordirect.data.AppViewModel
import com.aiexport.shareordirect.ui.navigation.AppNavigation
import com.aiexport.shareordirect.ui.theme.ShareToFileTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShareToFileTheme {
                AppNavigation(vm = vm)
            }
        }
    }
}
