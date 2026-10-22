package com.phishingnet.app.ai

import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.Locale
import java.util.zip.ZipInputStream

/** A piece of a Gemini request: text, or a file sent inline (base64). */
sealed class Part {
    data class Text(val text: String) : Part()
    class Inline(val mimeType: String, val base64: String) : Part()
}

/**
 * Turns a file into something Gemini can read. This does NOT judge the file —
 * it only chooses how to show it to the AI:
 *  - PDFs and photos are sent as they are (Gemini reads them directly);
 *  - text-like files (HTML, SVG, scripts, .txt, .eml…) are sent as text;
 *  - everything else (programs, ZIP/Office files, unknown formats) is described: size, first bytes,
 *    readable strings inside it, and the list of files inside archives.
 */
object FileParts {

    /** Gemini allows about 20 MB per request; base64 makes files ~33% bigger. */
    const val MAX_INLINE_BYTES = 12 * 1024 * 1024
    const val MAX_FILE_BYTES = 25 * 1024 * 1024
    private const val MAX_TEXT_CHARS = 60_000

    fun partsFor(file: AttachedFile, index: Int? = null): List<Part> {
        val b = file.bytes
        val label = (if (index != null) "Attachment ${index + 1}" else "File") +
            ": name=\"${file.name}\", size=${b.size} bytes" +
            (if (file.mimeType.isNotBlank()) ", type reported by the phone=${file.mimeType}" else "")

        val inlineMime = inlineMimeType(b)
        if (inlineMime != null && b.size <= MAX_INLINE_BYTES) {
            return listOf(
                Part.Text("$label. The file itself follows."),
                Part.Inline(inlineMime, Base64.getEncoder().encodeToString(b)),
            )
        }
        if (looksLikeText(b)) {
            val text = String(b, Charsets.UTF_8)
            val cut = if (text.length > MAX_TEXT_CHARS) text.take(MAX_TEXT_CHARS) + "\n[…file truncated…]" else text
            return listOf(Part.Text("$label. Its text content:\n<file_content>\n$cut\n</file_content>"))
        }
        return listOf(Part.Text(describeBinary(file, label)))
    }

    /** Formats Gemini can read directly. */
    fun inlineMimeType(b: ByteArray): String? = when {
        startsWith(b, 0x25, 0x50, 0x44, 0x46) -> "application/pdf"                   // %PDF
        startsWith(b, 0x89, 0x50, 0x4E, 0x47) -> "image/png"
        startsWith(b, 0xFF, 0xD8, 0xFF) -> "image/jpeg"
        b.size > 12 && String(b, 0, 4, Charsets.ISO_8859_1) == "RIFF" && String(b, 8, 4, Charsets.ISO_8859_1) == "WEBP" -> "image/webp"
        b.size > 12 && String(b, 4, 8, Charsets.ISO_8859_1).let { it == "ftypheic" || it == "ftypheix" || it == "ftypmif1" } -> "image/heic"
        else -> null
    }

    fun looksLikeText(b: ByteArray): Boolean {
        if (b.isEmpty()) return true
        val n = minOf(b.size, 8192)
        var bad = 0
        for (i in 0 until n) {
            val c = b[i].toInt() and 0xFF
            if (c == 0) return false
            if (c < 9 || c in 14..31) bad++
        }
        return bad <= n / 100
    }

    private fun describeBinary(file: AttachedFile, label: String): String {
        val b = file.bytes
        val sb = StringBuilder()
        sb.append(label).append(". This is a binary file, so it is described instead of sent.\n")
        sb.append("First 64 bytes (hex): ").append(hex(b, 64)).append('\n')
        sb.append("First 64 bytes (as text): ").append(asciiPreview(b, 64)).append('\n')

        val entries = zipEntries(b)
        if (entries.isNotEmpty()) {
            sb.append("It is a ZIP-based package containing ${entries.size} file(s)")
            if (entries.any { it.second }) sb.append(" (some are password-protected/encrypted)")
            sb.append(":\n")
            entries.take(80).forEach { (name, enc) -> sb.append(" - ").append(name).append(if (enc) " [encrypted]" else "").append('\n') }
            // Small text files inside (Office XML relationships, web pages, shortcuts) help the AI judge.
            zipTextEntries(b).forEach { (name, text) ->
                sb.append("Content of ").append(name).append(":\n").append(text).append('\n')
            }
        }
        val strings = printableStrings(b)
        if (strings.isNotEmpty()) {
            sb.append("Readable text found inside the file (sample):\n").append(strings).append('\n')
        }
        return sb.toString()
    }

    // ───────────────────────────────────────────── helpers

    private fun startsWith(b: ByteArray, vararg sig: Int) =
        b.size >= sig.size && sig.indices.all { (b[it].toInt() and 0xFF) == sig[it] }

    private fun hex(b: ByteArray, n: Int) = (0 until minOf(n, b.size)).joinToString(" ") { String.format(Locale.ROOT, "%02X", b[it]) }

    private fun asciiPreview(b: ByteArray, n: Int) = (0 until minOf(n, b.size)).joinToString("") {
        val c = b[it].toInt() and 0xFF
        if (c in 32..126) c.toChar().toString() else "."
    }

    /** Like the Unix `strings` tool: runs of 6+ readable characters, limited in size. */
    private fun printableStrings(b: ByteArray, maxChars: Int = 6000): String {
        val out = StringBuilder()
        val cur = StringBuilder()
        val limit = minOf(b.size, 4 * 1024 * 1024)
        val seen = HashSet<String>()
        for (i in 0 until limit) {
            val c = b[i].toInt() and 0xFF
            if (c in 32..126) cur.append(c.toChar())
            else {
                if (cur.length >= 6) {
                    val s = cur.toString().trim()
                    if (s.length >= 6 && seen.add(s)) out.append(s.take(200)).append('\n')
                    if (out.length >= maxChars) break
                }
                cur.setLength(0)
            }
        }
        return out.toString().take(maxChars)
    }

    /** Reads the ZIP central directory (works for password-protected archives too). Returns (name, encrypted). */
    fun zipEntries(b: ByteArray): List<Pair<String, Boolean>> {
        if (!startsWith(b, 0x50, 0x4B)) return emptyList()
        fun u16(i: Int) = if (i + 1 < b.size) (b[i].toInt() and 0xFF) or ((b[i + 1].toInt() and 0xFF) shl 8) else 0
        fun u32(i: Int) = if (i + 3 < b.size) (u16(i).toLong() or (u16(i + 2).toLong() shl 16)) else 0L
        var eocd = -1
        var i = b.size - 22
        val stop = maxOf(0, b.size - 22 - 65_535)
        while (i >= stop) {
            if (b[i].toInt() == 0x50 && b[i + 1].toInt() == 0x4B && b[i + 2].toInt() == 0x05 && b[i + 3].toInt() == 0x06) { eocd = i; break }
            i--
        }
        if (eocd < 0) return emptyList()
        val count = u16(eocd + 10)
        var p = u32(eocd + 16).toInt()
        val out = mutableListOf<Pair<String, Boolean>>()
        repeat(minOf(count, 2000)) {
            if (p < 0 || p + 46 > b.size || u32(p) != 0x02014B50L) return out
            val flags = u16(p + 8)
            val nameLen = u16(p + 28); val extraLen = u16(p + 30); val commentLen = u16(p + 32)
            if (p + 46 + nameLen > b.size) return out
            out += String(b, p + 46, nameLen, if (flags and 0x800 != 0) Charsets.UTF_8 else Charsets.ISO_8859_1) to (flags and 1 != 0)
            p += 46 + nameLen + extraLen + commentLen
        }
        return out
    }

    /** Small text files inside a ZIP that tell the AI a lot: Office relationships, web pages, shortcuts. */
    private fun zipTextEntries(b: ByteArray): List<Pair<String, String>> {
        val out = mutableListOf<Pair<String, String>>()
        runCatching {
            ZipInputStream(ByteArrayInputStream(b)).use { zin ->
                var total = 0
                while (out.size < 8) {
                    val e = zin.nextEntry ?: break
                    val n = e.name.lowercase(Locale.ROOT)
                    val wanted = n.endsWith(".rels") || n.endsWith(".html") || n.endsWith(".htm") || n.endsWith(".svg") ||
                        n.endsWith(".url") || n.endsWith(".js") || n.endsWith(".vbs") || n.endsWith(".hta") || n.endsWith(".txt") ||
                        n == "word/document.xml"
                    if (!e.isDirectory && wanted) {
                        val bytes = zin.readNBytesCompat(20_000)
                        var text = String(bytes, Charsets.UTF_8)
                        if (n == "word/document.xml") text = text.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ")
                        out += e.name to text.take(4000)
                        total += text.length
                        if (total > 15_000) break
                    }
                }
            }
        }
        return out
    }

    private fun ZipInputStream.readNBytesCompat(max: Int): ByteArray {
        val buf = java.io.ByteArrayOutputStream()
        val chunk = ByteArray(4096)
        while (buf.size() < max) {
            val n = read(chunk, 0, minOf(chunk.size, max - buf.size()))
            if (n <= 0) break
            buf.write(chunk, 0, n)
        }
        return buf.toByteArray()
    }
}
