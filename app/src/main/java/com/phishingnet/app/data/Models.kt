package com.phishingnet.app.data

/** One page of reading inside a course part. */
data class Lesson(
    val title: String,
    val paragraphs: List<String>,
    val keyPoints: List<String> = emptyList(),
    /** Optional example shown in a monospace "email" box, e.g. a fake phishing message. */
    val example: String? = null,
)

/** A single multiple-choice quiz question. */
data class Question(
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

/** A course part: several lessons followed by a quiz. */
data class CourseModule(
    val id: Int,
    val title: String,
    val summary: String,
    val lessons: List<Lesson>,
    val quiz: List<Question>,
)
