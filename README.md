# Phishing net — learn to spot phishing

An Android app (Kotlin + Jetpack Compose) with a 6-part phishing course, a quiz after each part, and two
AI checkers powered by Google's Gemini (Google AI Studio): **email** and **link**.

## Open it in Android Studio

1. Clone the repo, then **File › Open** → the project folder.
2. Let Gradle sync (needs internet the first time). Gradle JDK must be 17–22 (Settings › Build Tools › Gradle › Gradle JDK → jbr-21).
3. Run on an emulator or phone (Android 8.0+). The phone needs internet for the checkers.

## Add your Gemini API key

The key is not in this repository. To run the checkers:

1. Get a free key at [Google AI Studio](https://aistudio.google.com/apikey).
2. Open `local.properties` in the project root (Android Studio creates it on first sync) and add a line:

   ```
   GEMINI_API_KEY=your-key-here
   ```

3. Sync Gradle and run. `local.properties` is in `.gitignore`, so the key never gets committed.

Without a key the course still works; the checkers show a message asking for one.
Models: `gemini-3.8-flash`, with automatic fallback to `gemini-3.5-flash-lite` when the first is busy or rate-limited.

## Features

- **Course** — 6 parts, 4 lessons each, all unlocked; 5-question quiz per part (80% = passed, saved on the phone).
- **Check an email** — paste sender, subject and text (or share from Gmail / open a saved `.eml`), optionally add
  attachments (PDFs and photos are sent as they are, other files are described). Gemini returns a verdict, risk score, summary, red flags with quoted evidence, every link judged,
  what looks normal, and what to do.
- **Check a link** — paste or share a URL. Gemini explains who really owns the site and whether it's a trap,
  without opening it.

There are no hand-written detection rules: every verdict comes from Gemini. What it looks for is set in `ai/Prompts.kt`.

## Project structure

```
app/src/main/java/com/phishingnet/app/
├── MainActivity.kt          navigation; sends shared links/files/emails to the right checker
├── ai/
│   ├── GeminiConfig.kt      model names, answer language; the key comes from local.properties
│   ├── Prompts.kt           instructions for the email and link checks + the JSON answer format
│   ├── GeminiClient.kt      calls the Gemini API, model fallback, error messages, reads the JSON answer
│   ├── PhishingAi.kt        checkEmail / checkLink
│   ├── FileParts.kt         prepares email attachments for Gemini (inline, text, or described)
│   ├── AiReport.kt          the result types
│   └── Examples.kt          "Try an example" emails and links
├── data/                    course text (CourseData.kt), models, saved progress
└── ui/
    ├── FileLoading.kt       reads files picked or shared from other apps
    ├── theme/Theme.kt       colours and fonts
    └── screens/             Home, Course, Lesson, Quiz, Checker (email), LinkChecker, AiResultView
app/src/test/.../ai/GeminiClientTest.kt   offline tests: request format, answer parsing, errors, file preparation
```

## Customising

- What the AI checks and how it explains: `ai/Prompts.kt`.
- Answer language (e.g. "Russian"): `GeminiConfig.RESPONSE_LANGUAGE`.
- Course text and quizzes: `data/CourseData.kt`. Pass mark: `CourseData.PASS_PERCENT`.
- Example emails/links: `ai/Examples.kt`. Colours: `ui/theme/Theme.kt`.
