package com.phishingnet.app.ai

/** The two checks the app offers. Each builds the input for Gemini and returns its report. */
object PhishingAi {

    private const val MAX_EMAIL_CHARS = 40_000

    suspend fun checkEmail(sender: String, subject: String, body: String, files: List<AttachedFile>): AiReport {
        val text = buildString {
            appendLine("Check this email.")
            appendLine("<email>")
            if (sender.isNotBlank()) appendLine("From: $sender")
            if (subject.isNotBlank()) appendLine("Subject: $subject")
            if (sender.isNotBlank() || subject.isNotBlank()) appendLine()
            appendLine(if (body.length > MAX_EMAIL_CHARS) body.take(MAX_EMAIL_CHARS) + "\n[…truncated…]" else body)
            appendLine("</email>")
            if (sender.isBlank()) appendLine("(The sender address was not provided.)")
            appendLine(if (files.isEmpty()) "(No attachment files were added.)" else "The email's ${files.size} attachment file(s) follow.")
        }
        val parts = mutableListOf<Part>(Part.Text(text))
        files.forEachIndexed { i, f -> parts += FileParts.partsFor(f, i) }
        return GeminiClient.analyze(Prompts.EMAIL, parts)
    }

    suspend fun checkLink(url: String): AiReport =
        GeminiClient.analyze(Prompts.LINK, listOf(Part.Text("Check this link:\n<link>\n${url.trim().take(4000)}\n</link>")))
}
