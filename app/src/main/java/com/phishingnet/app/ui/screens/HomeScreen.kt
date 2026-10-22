package com.phishingnet.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.phishingnet.app.data.CourseData
import com.phishingnet.app.data.ProgressStore
import com.phishingnet.app.ui.theme.Abyss
import com.phishingnet.app.ui.theme.Buoy
import com.phishingnet.app.ui.theme.Foam
import com.phishingnet.app.ui.theme.Kelp
import com.phishingnet.app.ui.theme.Line

private val tips = listOf(
    "No real company will ever ask for your password or one-time code by email, SMS or phone.",
    "Read a link's domain from right to left: in paypal.com.login-check.io the owner is login-check.io.",
    "The padlock only means the connection is encrypted. Phishing sites have padlocks too.",
    "If a message makes you feel rushed or scared, that's the moment to slow down.",
    "Long-press a link on your phone to preview where it really goes before tapping.",
    "Only the last file extension counts: invoice.pdf.exe is a program.",
    "Caller ID can be faked. Hang up and call back on a number you find yourself.",
    "If your password manager won't autofill, double-check the site's address.",
    "Never approve a login request you didn't start yourself.",
)

@Composable
fun HomeScreen(
    progress: ProgressStore,
    onOpenCourse: () -> Unit,
    onOpenPart: (Int) -> Unit,
    onOpenChecker: () -> Unit,
    onOpenLinkChecker: () -> Unit,
) {
    val modules = CourseData.modules
    val passed = progress.passedCount()
    val next = progress.nextUnfinished()
    var tipIndex by rememberSaveable { mutableIntStateOf(tips.indices.random()) }

    // Show a new tip every minute while the home screen is open.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000)
            tipIndex = (tipIndex + 1) % tips.size
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            "Phishing net",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 14.dp),
        )

        Spacer(Modifier.height(20.dp))
        Text(
            "Don't take\nthe bait.",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Learn how phishing works, then check suspicious emails and links before you click.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))

        // ── Course block: deep water with the progress line ──
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Abyss)
                .clickable(onClick = onOpenCourse)
                .padding(22.dp),
        ) {
            Text("The course", style = MaterialTheme.typography.titleLarge, color = Foam)
            Text(
                if (passed == modules.size) "All ${modules.size} parts passed" else "$passed of ${modules.size} parts passed",
                style = MaterialTheme.typography.bodyMedium,
                color = Foam.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(20.dp))
            ProgressLine(total = modules.size, passed = { progress.isPassed(modules[it].id) })
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = { if (next != null) onOpenPart(next.id) else onOpenCourse() },
                colors = ButtonDefaults.buttonColors(containerColor = Foam, contentColor = Abyss),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when {
                        next == null -> "Review the course"
                        passed == 0 && progress.bestScores.isEmpty() -> "Start with part 1"
                        else -> "Continue: ${next.title}"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
            TextButton(onClick = onOpenCourse, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("See all parts", color = Foam.copy(alpha = 0.85f))
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── AI checkers ──
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp))
                .padding(22.dp),
        ) {
            Text("Check something", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            ToolButton("Check an email", "Paste a message or share it from your mail app", onOpenChecker)
            Spacer(Modifier.height(10.dp))
            ToolButton("Check a link", "See who really owns a website before you open it", onOpenLinkChecker)
        }

        Spacer(Modifier.height(28.dp))

        // ── Tip ──
        Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(Buoy),
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Tip", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                // Fades to the next tip every minute.
                androidx.compose.animation.Crossfade(targetState = tipIndex, label = "tip") { i ->
                    Text(tips[i], style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** A horizontal "line" with one float per part: filled when passed. */
@Composable
private fun ProgressLine(total: Int, passed: (Int) -> Boolean) {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(22.dp),
    ) {
        if (total <= 0) return@Canvas
        val r = size.height / 2.6f
        val y = size.height / 2
        val startX = r
        val endX = size.width - r
        val step = if (total == 1) 0f else (endX - startX) / (total - 1)
        drawLine(Line.copy(alpha = 0.5f), Offset(startX, y), Offset(endX, y), strokeWidth = 3f)
        for (i in 0 until total) {
            val c = Offset(startX + step * i, y)
            if (passed(i)) {
                drawCircle(Kelp, radius = r, center = c)
                drawCircle(Foam, radius = r * 0.35f, center = c)
            } else {
                drawCircle(Abyss, radius = r, center = c)
                drawCircle(Line, radius = r, center = c, style = Stroke(width = 3f))
            }
        }
    }
}

@Composable
private fun ToolButton(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
        }
        Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
    }
}
