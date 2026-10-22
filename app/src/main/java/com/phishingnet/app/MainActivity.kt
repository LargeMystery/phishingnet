package com.phishingnet.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.phishingnet.app.data.ProgressStore
import com.phishingnet.app.ui.SharedContent
import com.phishingnet.app.ui.screens.CheckerScreen
import com.phishingnet.app.ui.screens.CourseScreen
import com.phishingnet.app.ui.screens.HomeScreen
import com.phishingnet.app.ui.screens.LessonScreen
import com.phishingnet.app.ui.screens.LinkCheckerScreen
import com.phishingnet.app.ui.screens.QuizScreen
import com.phishingnet.app.ui.theme.PhishingNetTheme

class MainActivity : ComponentActivity() {

    /** Content shared into the app, waiting to be picked up by the matching checker. */
    private var sharedEmail by mutableStateOf<SharedContent?>(null)
    private var sharedUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) route(intent)

        setContent {
            PhishingNetTheme {
                val nav = rememberNavController()
                val progress = remember { ProgressStore(applicationContext) }

                // When something is shared in, jump straight to the matching checker.
                LaunchedEffect(sharedEmail) { if (sharedEmail != null) nav.navigate(Routes.CHECKER) { launchSingleTop = true } }
                LaunchedEffect(sharedUrl) { if (sharedUrl != null) nav.navigate(Routes.LINK) { launchSingleTop = true } }

                NavHost(navController = nav, startDestination = Routes.HOME) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            progress = progress,
                            onOpenCourse = { nav.navigate(Routes.COURSE) },
                            onOpenPart = { id ->
                                nav.navigate(Routes.COURSE)
                                nav.navigate(Routes.lesson(id))
                            },
                            onOpenChecker = { nav.navigate(Routes.CHECKER) },
                            onOpenLinkChecker = { nav.navigate(Routes.LINK) },
                        )
                    }
                    composable(Routes.COURSE) {
                        CourseScreen(
                            progress = progress,
                            onBack = { nav.popBackStack() },
                            onOpenLessons = { id -> nav.navigate(Routes.lesson(id)) },
                            onOpenQuiz = { id -> nav.navigate(Routes.quiz(id)) },
                        )
                    }
                    composable(
                        Routes.LESSON,
                        arguments = listOf(navArgument("id") { type = NavType.IntType }),
                    ) { entry ->
                        val id = entry.arguments?.getInt("id") ?: 1
                        LessonScreen(
                            moduleId = id,
                            onBack = { nav.popBackStack() },
                            onStartQuiz = {
                                nav.navigate(Routes.quiz(id)) { popUpTo(Routes.COURSE) }
                            },
                        )
                    }
                    composable(
                        Routes.QUIZ,
                        arguments = listOf(navArgument("id") { type = NavType.IntType }),
                    ) { entry ->
                        val id = entry.arguments?.getInt("id") ?: 1
                        QuizScreen(
                            moduleId = id,
                            progress = progress,
                            onBack = { nav.popBackStack() },
                            onReviewLessons = { nav.navigate(Routes.lesson(id)) { popUpTo(Routes.COURSE) } },
                            onNextPart = { nextId ->
                                nav.navigate(Routes.lesson(nextId)) { popUpTo(Routes.COURSE) }
                            },
                            onFinishCourse = { nav.popBackStack(Routes.COURSE, inclusive = false) },
                        )
                    }
                    composable(Routes.CHECKER) {
                        CheckerScreen(
                            initialShare = sharedEmail,
                            onInitialShareConsumed = { sharedEmail = null },
                            onBack = { nav.popBackStack() },
                        )
                    }
                    composable(Routes.LINK) {
                        LinkCheckerScreen(
                            initialUrl = sharedUrl,
                            onInitialUrlConsumed = { sharedUrl = null },
                            onBack = { nav.popBackStack() },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        route(intent)
    }

    /** Sends shared content to the right checker: a lone link → link checker, anything else (text, files) → email checker. */
    private fun route(intent: Intent?) {
        val s = intent.extractShared() ?: return
        val text = s.text?.trim()
        val lonelyUrl = text != null && s.uris.isEmpty() && !text.contains(Regex("\\s")) &&
            (text.startsWith("http://", true) || text.startsWith("https://", true) || text.startsWith("www.", true))
        when {
            lonelyUrl -> sharedUrl = text
            else -> sharedEmail = s
        }
    }

    /** Reads text and/or files from Share (SEND / SEND_MULTIPLE) or "Open with" (VIEW, e.g. a .eml file). */
    private fun Intent?.extractShared(): SharedContent? {
        if (this == null) return null
        return when (action) {
            Intent.ACTION_SEND -> {
                val subject = getStringExtra(Intent.EXTRA_SUBJECT)
                val text = getStringExtra(Intent.EXTRA_TEXT)
                val uri = IntentCompat.getParcelableExtra(this, Intent.EXTRA_STREAM, Uri::class.java)
                val fullText = text?.let { if (subject.isNullOrBlank()) it else "Subject: $subject\n\n$it" }
                if (fullText == null && uri == null) null else SharedContent(fullText, listOfNotNull(uri))
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = IntentCompat.getParcelableArrayListExtra(this, Intent.EXTRA_STREAM, Uri::class.java).orEmpty()
                if (uris.isEmpty()) null else SharedContent(getStringExtra(Intent.EXTRA_TEXT), uris)
            }
            Intent.ACTION_VIEW -> data?.let { SharedContent(null, listOf(it)) }
            else -> null
        }
    }
}

object Routes {
    const val HOME = "home"
    const val COURSE = "course"
    const val LESSON = "lesson/{id}"
    const val QUIZ = "quiz/{id}"
    const val CHECKER = "checker"
    const val LINK = "link"
    fun lesson(id: Int) = "lesson/$id"
    fun quiz(id: Int) = "quiz/$id"
}
