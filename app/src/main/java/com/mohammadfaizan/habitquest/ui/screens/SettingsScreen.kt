package com.mohammadfaizan.habitquest.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.HapticFeedbackConstantsCompat
import com.mohammadfaizan.habitquest.R
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
    onNavigateToGeneral: () -> Unit,
    habitViewModel: com.mohammadfaizan.habitquest.ui.viewmodel.HabitViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val versionName = getAppVersionName(context)
    
    var showThemeBottomSheet by remember { mutableStateOf(false) }
    var isDarkTheme by remember { mutableStateOf(false) }

    BackHandler {
        onBackClick()
    }

    val menuSections = remember {
        listOf(
            // Main Features/Settings Section
            MenuSection(
                items = listOf(
                    MenuItem(
                        title = "General",
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    ) {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onNavigateToGeneral()
                    },
                    MenuItem(
                        title = "Theme",
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_color),
                                contentDescription = null
                            )
                        }
                    ) {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        showThemeBottomSheet = true
                    },
                    MenuItem(
                        title = "Reorder",
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_lines),
                                contentDescription = null
                            )
                        }
                    ) {
                        Toast.makeText(context, "Coming soon! Work in progress", Toast.LENGTH_SHORT)
                            .show()
                    },
                    MenuItem(
                        title = "Analytics",
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_chart),
                                contentDescription = null
                            )
                        }
                    ) {
                        Toast.makeText(context, "Coming soon! Work in progress", Toast.LENGTH_SHORT)
                            .show()
                    },
                    MenuItem(
                        title = "Backup and Restore",
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_backup),
                                contentDescription = null
                            )
                        }
                    ) {
                        Toast.makeText(context, "Coming soon! Work in progress", Toast.LENGTH_SHORT)
                            .show()
                    },
                    MenuItem(
                        title = "Fill Graph with Random Data",
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_chart),
                                contentDescription = null
                            )
                        }
                    ) {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        habitViewModel?.generateRandomData(days = 182, completionProbability = 0.7f)
                        Toast.makeText(
                            context,
                            "Generating random data for screenshots...",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                )
            ),

            MenuSection(
                items = listOf(
                    MenuItem(
                        title = "App Developer",
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_person),
                                contentDescription = null
                            )
                        }
                    ) {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        val intent =
                            Intent(Intent.ACTION_VIEW, "https://mohammadfaizan.com".toUri())
                        context.startActivity(intent)
                    },
                    MenuItem(
                        title = "Follow on Twitter",
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_person),
                                contentDescription = null
                            )
                        }
                    ) {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }

                        val twitterAppIntent = Intent(
                            Intent.ACTION_VIEW,
                            "twitter://user?screen_name=mofaizandev".toUri()
                        )
                        val twitterWebIntent =
                            Intent(Intent.ACTION_VIEW, "https://twitter.com/mofaizandev".toUri())

                        if (context.packageManager.resolveActivity(
                                twitterAppIntent,
                                PackageManager.MATCH_DEFAULT_ONLY
                            ) != null
                        ) {
                            context.startActivity(twitterAppIntent)
                        } else {
                            context.startActivity(twitterWebIntent)
                        }
                    },
                    MenuItem(
                        title = "Follow on Instagram",
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_person),
                                contentDescription = null
                            )
                        }
                    ) {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }

                        val instagramAppIntent = Intent(
                            Intent.ACTION_VIEW,
                            "instagram://user?username=hellofaizaan".toUri()
                        )
                        val instagramWebIntent =
                            Intent(Intent.ACTION_VIEW, "https://instagram.com/hellofaizaan".toUri())

                        if (context.packageManager.resolveActivity(
                                instagramAppIntent,
                                PackageManager.MATCH_DEFAULT_ONLY
                            ) != null
                        ) {
                            context.startActivity(instagramAppIntent)
                        } else {
                            context.startActivity(instagramWebIntent)
                        }
                    }
                )
            ),

            MenuSection(
                items = listOf(
                    /*MenuItem(
                        title = "Share the App",
                        icon = { Icon(Icons.Default.Share, contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Share the app with people", Toast.LENGTH_SHORT).show()
                    },

                     */
                    MenuItem(
                        title = "Give feedback",
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_feedback),
                                contentDescription = null
                            )
                        }
                    ) {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                        } catch (e: Exception) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }

                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:faizan@mohammadfaizan.in?subject=Feedback for Habit Quest&body=App Version: $versionName\n\nPlease provide your feedback here:\n".toUri()
                        }

                        try {
                            context.startActivity(Intent.createChooser(emailIntent, "Send feedback via email"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    /*
                    MenuItem(
                        title = "Rate 5 star",
                        icon = { Icon(Icons.Default.Star, contentDescription = null) }
                    ) {
                        Toast.makeText(context, "Follow on Twitter", Toast.LENGTH_SHORT).show()
                    },
                    
                     */
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

    if (showThemeBottomSheet) {
        ThemeBottomSheet(
            isDarkTheme = isDarkTheme,
            onThemeChanged = { darkTheme ->
                isDarkTheme = darkTheme
                val sharedPrefs = context.getSharedPreferences("app_prefs", 0)
                sharedPrefs.edit { putBoolean("is_dark_theme", darkTheme) }
            },
            onDismiss = {
                showThemeBottomSheet = false
            }
        )
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

        Text(
            text = menuItem.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

