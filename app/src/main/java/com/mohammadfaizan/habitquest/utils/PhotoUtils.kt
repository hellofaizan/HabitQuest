package com.mohammadfaizan.habitquest.utils

import android.content.Context
import android.net.Uri
import java.io.File

private const val COMPLETION_PHOTOS_DIR = "completion_photos"

// Copies the picked image into the app's private files dir rather than keeping the picker's
// content:// Uri — that Uri's read grant isn't guaranteed to outlive this request, so anything
// we want to load again later (the next time the card renders) needs a durable local copy.
fun saveCompletionPhoto(context: Context, sourceUri: Uri, habitId: Long, dateKey: String): String? {
    return try {
        val dir = File(context.filesDir, COMPLETION_PHOTOS_DIR).apply { mkdirs() }
        val file = File(dir, "${habitId}_${dateKey}_${System.currentTimeMillis()}.jpg")
        val copied = context.contentResolver.openInputStream(sourceUri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
            true
        } ?: false
        if (copied) file.absolutePath else null
    } catch (e: Exception) {
        null
    }
}

// Best-effort cleanup — a leftover file on disk is harmless, so failures here are swallowed
// rather than surfaced (there's nothing the user could do about a stray file anyway).
fun deleteCompletionPhoto(photoPath: String?) {
    if (photoPath.isNullOrBlank()) return
    try {
        File(photoPath).delete()
    } catch (e: Exception) {
        // Ignored — see comment above.
    }
}
