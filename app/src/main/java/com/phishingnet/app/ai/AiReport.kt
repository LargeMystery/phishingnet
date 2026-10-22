package com.phishingnet.app.ai

/** What the AI concluded about an email, a link or a file. */
enum class Verdict(val label: String) {
    DANGEROUS("Likely phishing"),
    SUSPICIOUS("Be careful"),
    SAFE("Looks okay"),
}

/** One warning sign the AI found, with the exact text it is based on. */
data class AiFlag(val title: String, val explanation: String, val evidence: String = "")

/** The AI's opinion of one link. */
data class AiLink(val url: String, val verdict: Verdict, val reason: String)

data class AiReport(
    val verdict: Verdict,
    /** 0 = clearly safe, 100 = clearly phishing/malicious. */
    val riskScore: Int,
    /** One or two sentences explaining the verdict. */
    val summary: String,
    val redFlags: List<AiFlag>,
    val safeSignals: List<String>,
    /** Links found in the email / the checked link (with its parts explained). */
    val links: List<AiLink>,
    /** For files: what the file really is. */
    val fileType: String?,
    /** What the user should do next. */
    val advice: String,
    /** Which Gemini model answered. */
    val model: String,
)

/** A file the user added: an email attachment or a file to check. */
class AttachedFile(val name: String, val mimeType: String, val bytes: ByteArray)
