package com.phishingnet.app.ai

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Offline tests: how requests are built and how Gemini's answers are read. No network calls. */
class GeminiClientTest {

    private fun response(innerJson: String, thought: Boolean = true): String {
        val escaped = JSONObject.quote(innerJson)
        val thoughtPart = if (thought) """{"text":"thinking about it","thought":true},""" else ""
        return """{"candidates":[{"content":{"role":"model","parts":[$thoughtPart{"text":$escaped}]},"finishReason":"STOP"}]}"""
    }

    @Test
    fun readsAFullReport_skippingThoughtsAndCodeFences() {
        val inner = "```json\n" + """
            {"verdict":"dangerous","risk_score":96,"summary":"Fake Kaspi login.",
             "red_flags":[{"title":"Look-alike domain","explanation":"Not Kaspi's site","evidence":"kaspi.kz.secure-login.top"}],
             "safe_signals":[],
             "links":[{"url":"http://kaspi.kz.secure-login.top/verify","verdict":"dangerous","reason":"Owned by secure-login.top"}],
             "file_type":"","advice":"Delete it."}
        """.trimIndent() + "\n```"
        val r = GeminiClient.parseResponse(response(inner), "gemini-test")
        assertEquals(Verdict.DANGEROUS, r.verdict)
        assertEquals(96, r.riskScore)
        assertEquals("kaspi.kz.secure-login.top", r.redFlags.single().evidence)
        assertEquals(Verdict.DANGEROUS, r.links.single().verdict)
        assertNull(r.fileType)
        assertEquals("gemini-test", r.model)
    }

    @Test
    fun toleratesMissingFieldsAndOddVerdictWords() {
        val r = GeminiClient.parseResponse(response("""{"verdict":"Suspicious","summary":"Hmm."}""", thought = false), "m")
        assertEquals(Verdict.SUSPICIOUS, r.verdict)
        assertEquals(50, r.riskScore)
        assertTrue(r.redFlags.isEmpty() && r.links.isEmpty())
        val p = GeminiClient.parseResponse(response("""{"verdict":"phishing","risk_score":150}"""), "m")
        assertEquals(Verdict.DANGEROUS, p.verdict)
        assertEquals(100, p.riskScore)
    }

    @Test
    fun blockedOrEmptyAnswersBecomeReadableErrors() {
        try {
            GeminiClient.parseResponse("""{"promptFeedback":{"blockReason":"SAFETY"}}""", "m"); fail()
        } catch (e: GeminiClient.AiException) {
            assertTrue(e.message!!.contains("refused"))
        }
        try {
            GeminiClient.parseResponse(response("not json at all"), "m"); fail()
        } catch (e: GeminiClient.AiException) {
            assertTrue(e.tryFallback)
        }
    }

    @Test
    fun httpErrorsExplainWhatToDo() {
        val badKey = GeminiClient.errorFor(400, """{"error":{"message":"API key not valid. Please pass a valid API key."}}""", "m")
        assertTrue(badKey.message!!.contains("API key"))
        assertTrue(!badKey.tryFallback)
        assertTrue(GeminiClient.errorFor(429, "", "m").tryFallback)
        assertTrue(GeminiClient.errorFor(503, "", "m").tryFallback)
        assertTrue(GeminiClient.errorFor(404, "", "m").tryFallback)
    }

    @Test
    fun requestContainsPromptTextAndInlineFile() {
        val body = JSONObject(
            GeminiClient.buildRequest("SYSTEM", listOf(Part.Text("hello"), Part.Inline("application/pdf", "QUJD"))),
        )
        assertEquals("SYSTEM", body.getJSONObject("systemInstruction").getJSONArray("parts").getJSONObject(0).getString("text"))
        val parts = body.getJSONArray("contents").getJSONObject(0).getJSONArray("parts")
        assertEquals("hello", parts.getJSONObject(0).getString("text"))
        assertEquals("application/pdf", parts.getJSONObject(1).getJSONObject("inlineData").getString("mimeType"))
        assertEquals("application/json", body.getJSONObject("generationConfig").getString("responseMimeType"))
    }

    @Test
    fun filesAreSentInTheRightForm() {
        // PDF → sent as the file itself.
        val pdf = FileParts.partsFor(AttachedFile("invoice.pdf", "application/pdf", "%PDF-1.4 test".toByteArray()))
        assertTrue(pdf.any { it is Part.Inline && it.mimeType == "application/pdf" })

        // HTML → sent as text.
        val html = FileParts.partsFor(AttachedFile("voice.htm", "text/html", "<form><input type=password></form>".toByteArray()))
        assertTrue((html.single() as Part.Text).text.contains("<input type=password>"))

        // Program disguised as a PDF → described with its real first bytes.
        val exe = FileParts.partsFor(AttachedFile("Invoice.pdf", "application/pdf", byteArrayOf(0x4D, 0x5A, 0x90.toByte(), 0, 3, 0, 0, 0) + ByteArray(100)))
        val exeText = (exe.single() as Part.Text).text
        assertTrue(exeText.contains("4D 5A"))
        assertTrue(exeText.contains("name=\"Invoice.pdf\""))

        // ZIP → the files inside are listed.
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { z -> z.putNextEntry(ZipEntry("scan/Invoice.pdf.exe")); z.write(byteArrayOf(0x4D, 0x5A)); z.closeEntry() }
        val zipText = (FileParts.partsFor(AttachedFile("docs.zip", "", out.toByteArray())).single() as Part.Text).text
        assertTrue(zipText.contains("scan/Invoice.pdf.exe"))
    }

    @Test
    fun promptsAskForTheJsonShapeTheParserReads() {
        for (p in listOf(Prompts.EMAIL, Prompts.LINK)) {
            listOf("\"verdict\"", "\"risk_score\"", "\"red_flags\"", "\"links\"", "\"advice\"").forEach { key ->
                assertTrue("Prompt is missing $key", p.contains(key))
            }
        }
    }
}
