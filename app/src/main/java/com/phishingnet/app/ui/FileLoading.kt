package com.phishingnet.app.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.phishingnet.app.ai.AttachedFile
import com.phishingnet.app.ai.FileParts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A file picked or shared into the app: either a whole email (.eml) or an attachment to inspect. */
sealed class LoadedFile {
    data class Email(val name: String, val text: String) : LoadedFile()
    class Attachment(val file: AttachedFile) : LoadedFile()
    data class Failed(val name: String, val reason: String) : LoadedFile()
}

/** Reads a file from a content Uri (file picker or Share). Never runs or opens the file — just reads its bytes. */
suspend fun loadFile(context: Context, uri: Uri): LoadedFile = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    var name = uri.lastPathSegment?.substringAfterLast('/') ?: "file"
    var size = -1L
    runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                c.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { i -> c.getString(i)?.let { name = it } }
                c.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { i -> if (!c.isNull(i)) size = c.getLong(i) }
            }
        }
    }
    val mime = runCatching { resolver.getType(uri) }.getOrNull().orEmpty()
    try {
        val bytes = resolver.openInputStream(uri)?.use { input ->
            val out = java.io.ByteArrayOutputStream()
            val buf = ByteArray(64 * 1024)
            while (out.size() < FileParts.MAX_FILE_BYTES) {
                val n = input.read(buf); if (n <= 0) break
                out.write(buf, 0, n)
            }
            out.toByteArray()
        } ?: return@withContext LoadedFile.Failed(name, "The file couldn't be opened.")

        val ext = name.substringAfterLast('.', "").lowercase()
        // A saved email (.eml) is loaded as the email itself, not as an attachment.
        val isEmail = mime == "message/rfc822" || ext == "eml"
        if (isEmail) LoadedFile.Email(name, String(bytes, Charsets.UTF_8))
        else LoadedFile.Attachment(AttachedFile(name, mime, bytes))
    } catch (e: SecurityException) {
        LoadedFile.Failed(name, "Phishing net wasn't allowed to read this file.")
    } catch (e: Exception) {
        LoadedFile.Failed(name, "The file couldn't be read (${e.javaClass.simpleName}).")
    }
}

fun formatSize(bytes: Int): String = when {
    bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes / 1024.0 / 1024.0)
    bytes >= 1024 -> "${bytes / 1024} KB"
    else -> "$bytes B"
}

/** Something shared into the app from another app: text and/or files. */
data class SharedContent(val text: String?, val uris: List<Uri>)
