package com.phishingnet.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException

/**
 * Calls Google's Gemini API (generateContent) and turns its answer into an [AiReport].
 * Uses only HttpURLConnection + org.json, which are built into Android.
 */
object GeminiClient {

    /** A problem the user should see, written in plain words. */
    class AiException(message: String, val tryFallback: Boolean = false) : Exception(message)

    suspend fun analyze(systemPrompt: String, parts: List<Part>): AiReport = withContext(Dispatchers.IO) {
        if (GeminiConfig.API_KEY.isBlank()) {
            throw AiException("No Gemini API key. Add GEMINI_API_KEY=your-key to local.properties and rebuild.")
        }
        val body = buildRequest(systemPrompt, parts)
        var lastError: AiException? = null
        for (model in listOf(GeminiConfig.MODEL, GeminiConfig.FALLBACK_MODEL).distinct()) {
            try {
                return@withContext parseResponse(post(model, body), model)
            } catch (e: AiException) {
                lastError = e
                if (!e.tryFallback) throw e
            }
        }
        throw lastError ?: AiException("The AI didn't answer. Try again.")
    }

    // ───────────────────────────────────────────── request

    internal fun buildRequest(systemPrompt: String, parts: List<Part>): String {
        val partsJson = JSONArray()
        parts.forEach { p ->
            when (p) {
                is Part.Text -> partsJson.put(JSONObject().put("text", p.text))
                is Part.Inline -> partsJson.put(
                    JSONObject().put("inlineData", JSONObject().put("mimeType", p.mimeType).put("data", p.base64)),
                )
            }
        }
        return JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            .put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", partsJson)))
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", 0.2)
                    .put("responseMimeType", "application/json"),
            )
            .toString()
    }

    private fun post(model: String, body: String): String {
        val url = URL(GeminiConfig.ENDPOINT + URLEncoder.encode(model, "UTF-8") + ":generateContent")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 90_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("x-goog-api-key", GeminiConfig.API_KEY)
        }
        try {
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw errorFor(code, text, model)
            return text
        } catch (e: AiException) {
            throw e
        } catch (e: SocketTimeoutException) {
            throw AiException("The AI took too long to answer. Try again.", tryFallback = true)
        } catch (e: UnknownHostException) {
            throw AiException("No internet connection. Connect to the internet and try again.")
        } catch (e: IOException) {
            throw AiException("Couldn't reach the AI. Check your internet connection and try again.")
        } finally {
            conn.disconnect()
        }
    }

    internal fun errorFor(code: Int, body: String, model: String): AiException {
        val msg = runCatching { JSONObject(body).getJSONObject("error").optString("message") }.getOrNull().orEmpty()
        return when {
            code == 400 && (msg.contains("API key", true) || msg.contains("API_KEY", true)) ->
                AiException("The Gemini API key was rejected. Put a valid key in local.properties (GEMINI_API_KEY) and rebuild.")
            code == 400 && msg.contains("size", true) ->
                AiException("This is too big for the AI to check. Try a smaller file or shorter text.")
            code == 400 -> AiException("The AI couldn't process this request. ${msg.take(160)}")
            code == 401 || code == 403 ->
                AiException("The AI refused access ($code). The API key may be invalid, restricted or not allowed in your region. ${msg.take(160)}")
            code == 404 -> AiException("The AI model \"$model\" wasn't found.", tryFallback = true)
            code == 429 -> AiException("Too many checks right now (the free limit was reached). Wait a minute and try again.", tryFallback = true)
            code >= 500 -> AiException("The AI service is busy. Try again in a moment.", tryFallback = true)
            else -> AiException("Unexpected answer from the AI ($code). ${msg.take(160)}")
        }
    }

    // ───────────────────────────────────────────── response

    internal fun parseResponse(raw: String, model: String): AiReport {
        val root = runCatching { JSONObject(raw) }.getOrElse { throw AiException("The AI's answer couldn't be read. Try again.", tryFallback = true) }
        val candidates = root.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            val reason = root.optJSONObject("promptFeedback")?.optString("blockReason").orEmpty()
            throw AiException(
                if (reason.isNotBlank()) "The AI refused to analyse this ($reason). Treat it with caution."
                else "The AI returned an empty answer. Try again.",
                tryFallback = reason.isBlank(),
            )
        }
        val first = candidates.getJSONObject(0)
        val parts = first.optJSONObject("content")?.optJSONArray("parts") ?: JSONArray()
        val text = buildString {
            for (i in 0 until parts.length()) {
                val p = parts.optJSONObject(i) ?: continue
                if (!p.optBoolean("thought", false)) append(p.optString("text"))
            }
        }
        if (text.isBlank()) {
            val finish = first.optString("finishReason")
            throw AiException(
                if (finish == "SAFETY" || finish == "PROHIBITED_CONTENT") "The AI refused to analyse this. Treat it with caution."
                else "The AI returned an empty answer. Try again.",
                tryFallback = finish != "SAFETY",
            )
        }
        val json = extractJson(text) ?: throw AiException("The AI's answer couldn't be read. Try again.", tryFallback = true)
        return toReport(json, model)
    }

    internal fun toReport(json: JSONObject, model: String): AiReport {
        val flags = json.optJSONArray("red_flags") ?: JSONArray()
        val safe = json.optJSONArray("safe_signals") ?: JSONArray()
        val links = json.optJSONArray("links") ?: JSONArray()
        val verdict = verdictOf(json.optString("verdict"))
        val score = json.optInt("risk_score", defaultScore(verdict)).coerceIn(0, 100)
        return AiReport(
            verdict = verdict,
            riskScore = score,
            summary = json.optString("summary").trim(),
            redFlags = (0 until flags.length()).mapNotNull { i ->
                val f = flags.optJSONObject(i) ?: return@mapNotNull null
                AiFlag(f.optString("title").trim(), f.optString("explanation").trim(), f.optString("evidence").trim())
            }.filter { it.title.isNotBlank() },
            safeSignals = (0 until safe.length()).map { safe.optString(it).trim() }.filter { it.isNotBlank() },
            links = (0 until links.length()).mapNotNull { i ->
                val l = links.optJSONObject(i) ?: return@mapNotNull null
                AiLink(l.optString("url").trim(), verdictOf(l.optString("verdict")), l.optString("reason").trim())
            }.filter { it.url.isNotBlank() },
            fileType = json.optString("file_type").trim().takeIf { it.isNotBlank() && it.lowercase() != "null" },
            advice = json.optString("advice").trim(),
            model = model,
        )
    }

    private fun verdictOf(s: String): Verdict = when {
        s.startsWith("danger", true) || s.startsWith("phish", true) || s.startsWith("malic", true) -> Verdict.DANGEROUS
        s.startsWith("susp", true) -> Verdict.SUSPICIOUS
        else -> Verdict.SAFE
    }

    private fun defaultScore(v: Verdict) = when (v) {
        Verdict.DANGEROUS -> 90
        Verdict.SUSPICIOUS -> 50
        Verdict.SAFE -> 10
    }

    /** Models sometimes wrap JSON in ``` fences or add a sentence; take the outer object. */
    private fun extractJson(text: String): JSONObject? {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return runCatching { JSONObject(text.substring(start, end + 1)) }.getOrNull()
    }
}
