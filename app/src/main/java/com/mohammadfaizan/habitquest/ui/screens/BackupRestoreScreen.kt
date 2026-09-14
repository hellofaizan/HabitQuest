package com.mohammadfaizan.habitquest.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohammadfaizan.habitquest.domain.usecase.ExportBackupResult
import com.mohammadfaizan.habitquest.ui.viewmodel.BackupViewModel
import com.mohammadfaizan.habitquest.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun BackupRestoreScreen(
    viewModel: BackupViewModel,
    onBackClick: () -> Unit,
    onImportComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isWorking by viewModel.isWorking.collectAsState()

    var showRestoreConfirmation by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.exportBackup { result -> writeExportResult(context, uri, result, scope) }
    }

    // "*/*" rather than "application/json" — plenty of file managers hand back backup files
    // tagged as text/plain or application/octet-stream, which would otherwise get greyed out.
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showRestoreConfirmation = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        BackupActionCard(
            emoji = "⬆️",
            title = "Export Backup",
            description = "Save every habit, completion, and streak to a JSON file you can keep or move to another device.",
            buttonLabel = "Export",
            enabled = !isWorking,
            onClick = { exportLauncher.launch("habitquest-backup-${DateUtils.getCurrentDateKey()}.json") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BackupActionCard(
            emoji = "⬇️",
            title = "Restore from Backup",
            description = "Replace everything currently in the app with the contents of a backup file. This can't be undone.",
            buttonLabel = "Restore",
            enabled = !isWorking,
            onClick = { importLauncher.launch(arrayOf("*/*")) }
        )

        if (isWorking) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }
    }

    if (showRestoreConfirmation && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmation = false
                pendingImportUri = null
            },
            title = {
                Text(
                    text = "Restore Backup",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This replaces every habit, completion, and streak currently in the app with what's in the backup file. This action is irreversible. Continue?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showRestoreConfirmation = false
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingImportUri
                        showRestoreConfirmation = false
                        pendingImportUri = null
                        if (uri != null) {
                            readAndImport(context, uri, viewModel, scope, onImportComplete)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Restore")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BackupActionCard(
    emoji: String,
    title: String,
    description: String,
    buttonLabel: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonLabel)
            }
        }
    }
}

private fun writeExportResult(
    context: Context,
    uri: Uri,
    result: ExportBackupResult,
    scope: CoroutineScope
) {
    if (!result.success || result.json == null) {
        Toast.makeText(context, result.error ?: "Failed to create backup", Toast.LENGTH_SHORT).show()
        return
    }
    scope.launch(Dispatchers.IO) {
        val error = try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(result.json.toByteArray())
            } ?: throw IOException("Couldn't open the selected file")
            null
        } catch (e: IOException) {
            e.message ?: "Failed to save backup"
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                error?.let { "Failed to save backup: $it" } ?: "Backup saved",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

private fun readAndImport(
    context: Context,
    uri: Uri,
    viewModel: BackupViewModel,
    scope: CoroutineScope,
    onImportComplete: () -> Unit
) {
    scope.launch(Dispatchers.IO) {
        val json = try {
            context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
        } catch (e: IOException) {
            null
        }
        withContext(Dispatchers.Main) {
            if (json == null) {
                Toast.makeText(context, "Couldn't read the selected file", Toast.LENGTH_SHORT).show()
                return@withContext
            }
            viewModel.importBackup(json) { result ->
                if (result.success && result.summary != null) {
                    Toast.makeText(
                        context,
                        "Restored ${result.summary.habitCount} habits, ${result.summary.completionCount} completions",
                        Toast.LENGTH_LONG
                    ).show()
                    onImportComplete()
                } else {
                    Toast.makeText(
                        context,
                        result.error ?: "Failed to restore backup",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
