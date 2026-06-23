package com.aiexport.shareordirect.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aiexport.shareordirect.data.AppViewModel
import com.aiexport.shareordirect.ui.screens.HomeScreen
import com.aiexport.shareordirect.ui.screens.SettingsScreen
import com.aiexport.shareordirect.ui.screens.ShareScreenContent

@Composable
fun AppNavigation(vm: AppViewModel, initialRoute: String = "home") {
    val nav = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(nav) }
    ) { padding ->
        NavHost(navController = nav, startDestination = initialRoute) {
            composable("home")     { HomeScreen(vm = vm, padding = padding) }
            composable("share")    { ShareScreenContent(vm = vm, onBack = null, padding = padding) }
            composable("settings") { SettingsScreen(vm = vm, padding = padding) }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    NavigationBar {
        NavigationBarItem(
            selected  = current == "home",
            onClick   = { nav.navigate("home")     { launchSingleTop = true } },
            icon      = { Icon(Icons.Default.Home,     null) },
            label     = { Text("Home") }
        )
        NavigationBarItem(
            selected  = current == "share",
            onClick   = { nav.navigate("share")    { launchSingleTop = true } },
            icon      = { Icon(Icons.Default.Share,    null) },
            label     = { Text("Share") }
        )
        NavigationBarItem(
            selected  = current == "settings",
            onClick   = { nav.navigate("settings") { launchSingleTop = true } },
            icon      = { Icon(Icons.Default.Settings, null) },
            label     = { Text("Settings") }
        )
    }
}
