package com.phishingnet.app.ai

import com.phishingnet.app.BuildConfig

/**
 * Google AI Studio (Gemini API) settings.
 *
 * The API key is NOT stored in the code. Put it in `local.properties` (which git ignores):
 *
 *     GEMINI_API_KEY=your-key-here
 *
 * Gradle copies it into BuildConfig when the app is built.
 */
object GeminiConfig {

    val API_KEY: String = BuildConfig.GEMINI_API_KEY

    /** Main model. */
    const val MODEL = "gemini-3.8-flash"

    /** Used automatically when the main model is busy, rate-limited or not found. */
    const val FALLBACK_MODEL = "gemini-3.5-flash-lite"

    const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/"

    /** Language the AI writes its explanations in. */
    const val RESPONSE_LANGUAGE = "English"
}
