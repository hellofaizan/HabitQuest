package com.mohammadfaizan.habitquest.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun shareBitmap(context: Context, bitmap: Bitmap, chooserTitle: String = "Share your progress") {
    val cacheDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val file = File(cacheDir, "habit_progress_${System.currentTimeMillis()}.png")

    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
