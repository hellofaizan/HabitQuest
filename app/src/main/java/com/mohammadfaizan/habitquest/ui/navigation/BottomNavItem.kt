package com.mohammadfaizan.habitquest.ui.navigation

import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "Home")
    object Stats : BottomNavItem("stats", Icons.Filled.Person, "Stats")
    object Profile : BottomNavItem("profile", Icons.Filled.Person, "Profile")
}