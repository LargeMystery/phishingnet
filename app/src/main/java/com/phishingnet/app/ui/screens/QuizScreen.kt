package com.phishingnet.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phishingnet.app.data.CourseData
import com.phishingnet.app.data.ProgressStore
import com.phishingnet.app.ui.theme.Buoy
import com.phishingnet.app.ui.theme.Foam
import com.phishingnet.app.ui.theme.Kelp

@Composable
fun QuizScreen(
    moduleId: Int,
    progress: ProgressStore,
    onBack: () -> Unit,
    onReviewLessons: () -> Unit,
    onNextPart: (Int) -> Unit,
    onFinishCourse: () -> Unit,
) {
    val module = CourseData.modules.first { it.id == moduleId }
    val questions = module.quiz

    // rememberSaveable so the quiz survives screen rotation; `attempt` resets everything on retry.
    var attempt by rememberSaveable { mutableIntStateOf(0) }
    var index by rememberSaveable(attempt) { mutableIntStateOf(0) }
    var selected by rememberSaveable(attempt) { mutableStateOf<Int?>(null) }
    var checked by rememberSaveable(attempt) { mutableStateOf(false) }
    var correctCount by rememberSaveable(attempt) { mutableIntStateOf(0) }
    var finished by rememberSaveable(attempt) { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar("Part ${module.id} quiz", onBack) },
        bottomBar = {
            if (!finished) {
                Button(
                    onClick = {
                        if (!checked) {
                            checked = true
                            if (selected == questions[index].correctIndex) correctCount++
                        } else if (index < questions.lastIndex) {
                            index++
                            selected = null
                            checked = false
                        } else {
                            finished = true
                        }
                    },
                    enabled = selected != null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Text(
                        when {
                            !checked -> "Check answer"
                            index < questions.lastIndex -> "Next question"
                            else -> "See my score"
                        },
                        modifier = Modifier.padding(vertical = 6.dp),
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (finished) {
            val percent = correctCount * 100 / questions.size
            LaunchedEffect(attempt) { progress.recordScore(moduleId, percent) }
            ResultView(
                percent = percent,
                correct = correctCount,
                total = questions.size,
                nextTitle = CourseData.modules.getOrNull(CourseData.modules.indexOf(module) + 1)?.title,
                modifier = Modifier.padding(padding),
                onNext = {
                    val next = CourseData.modules.getOrNull(CourseData.modules.indexOf(module) + 1)
                    if (next != null) onNextPart(next.id) else onFinishCourse()
                },
                onReview = onReviewLessons,
                onRetry = { attempt++ },
            )
        } else {
            val q = questions[index]
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                // One dot per question: green/orange once answered.
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    questions.indices.forEach { i ->
                        val color = when {
                            i < index || (i == index && checked) -> MaterialTheme.colorScheme.primary
                            i == index -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color),
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "Question ${index + 1} of ${questions.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Text(q.text, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(22.dp))

                q.options.forEachIndexed { i, option ->
                    AnswerOption(
                        letter = ('A' + i).toString(),
                        text = option,
                        state = when {
                            checked && i == q.correctIndex -> AnswerState.Correct
                            checked && i == selected -> AnswerState.Wrong
                            i == selected -> AnswerState.Selected
                            else -> AnswerState.Idle
                        },
                        enabled = !checked,
                        onClick = { selected = i },
                    )
                    Spacer(Modifier.height(10.dp))
                }

                AnimatedVisibility(visible = checked, enter = fadeIn() + expandVertically()) {
                    val right = selected == q.correctIndex
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (right) Kelp.copy(alpha = 0.12f) else Buoy.copy(alpha = 0.10f))
                            .padding(18.dp),
                    ) {
                        Text(
                            if (right) "Correct" else "Not quite",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (right) Kelp else Buoy,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(q.explanation, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private enum class AnswerState { Idle, Selected, Correct, Wrong }

@Composable
private fun AnswerOption(letter: String, text: String, state: AnswerState, enabled: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val accent: Color = when (state) {
        AnswerState.Correct -> Kelp
        AnswerState.Wrong -> Buoy
        AnswerState.Selected -> primary
        AnswerState.Idle -> MaterialTheme.colorScheme.outlineVariant
    }
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                when (state) {
                    AnswerState.Correct -> Kelp.copy(alpha = 0.10f)
                    AnswerState.Wrong -> Buoy.copy(alpha = 0.08f)
                    else -> MaterialTheme.colorScheme.surface
                },
            )
            .border(BorderStroke(if (state == AnswerState.Idle) 1.dp else 2.dp, accent), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (state == AnswerState.Idle) MaterialTheme.colorScheme.surfaceVariant else accent),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                AnswerState.Correct -> Icon(Icons.Filled.Check, "Correct", tint = Foam, modifier = Modifier.size(18.dp))
                AnswerState.Wrong -> Icon(Icons.Filled.Close, "Wrong", tint = Foam, modifier = Modifier.size(18.dp))
                AnswerState.Selected -> Text(letter, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                AnswerState.Idle -> Text(letter, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ResultView(
    percent: Int,
    correct: Int,
    total: Int,
    nextTitle: String?,
    modifier: Modifier,
    onNext: () -> Unit,
    onReview: () -> Unit,
    onRetry: () -> Unit,
) {
    val passed = percent >= CourseData.PASS_PERCENT
    val color = if (passed) Kelp else Buoy
    val track = MaterialTheme.colorScheme.outlineVariant
    // The ring fills once when the score appears.
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(percent) { sweep.animateTo(percent / 100f, tween(900)) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val w = 14.dp.toPx()
                drawArc(track, 0f, 360f, false, style = Stroke(w))
                drawArc(color, -90f, 360f * sweep.value, false, style = Stroke(w, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$percent%", fontSize = 52.sp, fontWeight = FontWeight.Black, color = color, letterSpacing = (-1.5).sp)
                Text("$correct of $total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(
            if (passed) "Passed. You're getting harder to hook." else "Not passed yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (passed) "Your score is saved. Keep going while it's fresh."
            else "You need ${CourseData.PASS_PERCENT}% to pass. Re-read the lessons, then try again — the explanations are the best part.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        val btnShape = RoundedCornerShape(14.dp)
        if (passed) {
            Button(onClick = onNext, shape = btnShape, modifier = Modifier.fillMaxWidth()) {
                Text(if (nextTitle != null) "Next part: $nextTitle" else "Back to the course", modifier = Modifier.padding(vertical = 6.dp))
            }
        } else {
            Button(onClick = onReview, shape = btnShape, modifier = Modifier.fillMaxWidth()) {
                Text("Re-read the lessons", modifier = Modifier.padding(vertical = 6.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onRetry, shape = btnShape, modifier = Modifier.fillMaxWidth()) {
            Text("Retry quiz", modifier = Modifier.padding(vertical = 6.dp))
        }
        if (!passed && nextTitle != null) {
            Spacer(Modifier.height(4.dp))
            androidx.compose.material3.TextButton(onClick = onNext) { Text("Skip to next part") }
        }
    }
}
