package com.phishingnet.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.phishingnet.app.data.CourseData
import com.phishingnet.app.ui.theme.Abyss
import com.phishingnet.app.ui.theme.Foam

@Composable
fun LessonScreen(
    moduleId: Int,
    onBack: () -> Unit,
    onStartQuiz: () -> Unit,
) {
    val module = CourseData.modules.first { it.id == moduleId }
    var index by rememberSaveable(moduleId) { mutableIntStateOf(0) }
    val isLast = index == module.lessons.lastIndex

    Scaffold(
        topBar = { AppTopBar("Part ${module.id}: ${module.title}", onBack) },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { if (index > 0) index-- else onBack() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                ) { Text(if (index > 0) "Previous" else "Back", modifier = Modifier.padding(vertical = 4.dp)) }
                Button(
                    onClick = { if (isLast) onStartQuiz() else index++ },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                ) { Text(if (isLast) "Take the quiz" else "Next", modifier = Modifier.padding(vertical = 4.dp)) }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Segmented progress: one bar per lesson.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                module.lessons.indices.forEach { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (i <= index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                            ),
                    )
                }
            }

            // Pages slide left/right as you move through the lessons.
            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(250)) { it / 6 * dir } + fadeIn(tween(250))) togetherWith
                        (slideOutHorizontally(tween(200)) { -it / 6 * dir } + fadeOut(tween(200)))
                },
                label = "lesson",
                modifier = Modifier.weight(1f),
            ) { page ->
                val lesson = module.lessons[page]
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Lesson ${page + 1} of ${module.lessons.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(lesson.title, style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(20.dp))

                    lesson.paragraphs.forEach { p ->
                        Text(p, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(14.dp))
                    }

                    lesson.example?.let {
                        Spacer(Modifier.height(4.dp))
                        ExampleBox(it)
                        Spacer(Modifier.height(20.dp))
                    }

                    if (lesson.keyPoints.isNotEmpty()) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Abyss)
                                .padding(20.dp),
                        ) {
                            Text("Remember", style = MaterialTheme.typography.titleMedium, color = Foam)
                            Spacer(Modifier.height(8.dp))
                            lesson.keyPoints.forEach {
                                BulletRow(it, color = MaterialTheme.colorScheme.tertiary, textColor = Foam)
                            }
                        }
                    }
                    Spacer(Modifier.height(28.dp))
                }
            }
        }
    }
}
