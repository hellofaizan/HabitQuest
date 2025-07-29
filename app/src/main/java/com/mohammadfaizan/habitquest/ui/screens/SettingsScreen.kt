package com.mohammadfaizan.habitquest.ui.screens

import com.mohammadfaizan.habitquest.R
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.mohammadfaizan.habitquest.ui.theme.Typography
import com.mohammadfaizan.habitquest.utils.getAppVersionName

data class MenuItem(
    val title: String,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit
)

data class MenuSection(
    val title: String? = null,
    val items: List<MenuItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val versionName = getAppVersionName(context)
    
    val menuSections = remember {
        listOf(
            // Main Features/Settings Section
            MenuSection(
                items = listOf(
                    MenuItem(
                        title = "Settings",
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT).show()
                    },
                    MenuItem(
                        title = "Theme",
                        icon = { Icon(painter = painterResource(R.drawable.ic_color), contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Follow on Twitter", Toast.LENGTH_SHORT).show()
                    },
                    MenuItem(
                        title = "Reorder",
                        icon = { Icon(painter = painterResource(R.drawable.ic_lines), contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Follow on Twitter", Toast.LENGTH_SHORT).show()
                    },
                    MenuItem(
                        title = "Analytics",
                        icon = { Icon(painter = painterResource(R.drawable.ic_chart), contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Coming soon! Work in progress", Toast.LENGTH_SHORT).show()
                    },
                    MenuItem(
                        title = "Backup and Restore",
                        icon = { Icon(painter = painterResource(R.drawable.ic_backup), contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Coming soon! Work in progress", Toast.LENGTH_SHORT).show()
                    },
                )
            ),

            MenuSection(
                items = listOf(
                    MenuItem(
                        title = "App Developer",
                        icon = { Icon(painter = painterResource(R.drawable.ic_person), contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Win Rewards! clicked", Toast.LENGTH_SHORT).show()
                    },
                    MenuItem(
                        title = "Follow on Twitter",
                        icon = { Icon(painter = painterResource(R.drawable.ic_person), contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Follow on Twitter", Toast.LENGTH_SHORT).show()
                    },
                    MenuItem(
                        title = "Follow on Instagram",
                        icon = { Icon(painter = painterResource(R.drawable.ic_person), contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Win Rewards! clicked", Toast.LENGTH_SHORT).show()
                    }
                )
            ),

            MenuSection(
                items = listOf(
                    MenuItem(
                        title = "Share the App",
                        icon = { Icon(Icons.Default.Share, contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Send feedback clicked", Toast.LENGTH_SHORT).show()
                    },
                    MenuItem(
                        title = "Give feedback",
                        icon = { Icon(painter = painterResource(R.drawable.ic_feedback), contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Follow on Twitter", Toast.LENGTH_SHORT).show()
                    },
                    MenuItem(
                        title = "Rate 5 star",
                        icon = { Icon(Icons.Default.Star, contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Follow on Twitter", Toast.LENGTH_SHORT).show()
                    },
                )
            ),
            
            // Legal/Privacy Section
            MenuSection(
                items = listOf(
                    MenuItem(
                        title = "Privacy policy",
                        icon = { Icon(Icons.Default.Lock, contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Privacy policy clicked", Toast.LENGTH_SHORT).show()
                    },
                    MenuItem(
                        title = "Terms and Conditions",
                        icon = { Icon(Icons.Default.Lock, contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Follow on Twitter", Toast.LENGTH_SHORT).show()
                    },
                )
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        IconButton(
            onClick = {
                try {
                    view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                } catch (e: Exception) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onBackClick()
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go Back"
            )
        }

        // Menu Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            menuSections.forEach { section ->
                item {
                    MenuSectionCard(
                        section = section,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Version text at the bottom
            item {
                Text(
                    text = "Mohammad Faizan  •  Android v$versionName",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MenuSectionCard(
    section: MenuSection,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            section.items.forEach { menuItem ->
                MenuItemRow(
                    menuItem = menuItem,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MenuItemRow(
    menuItem: MenuItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { menuItem.onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            menuItem.icon()
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Title
        Text(
            text = menuItem.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        // Chevron
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

