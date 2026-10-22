package com.phishingnet.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.phishingnet.app.data.CourseData
import com.phishingnet.app.data.CourseModule
import com.phishingnet.app.data.ProgressStore
import com.phishingnet.app.ui.theme.Abyss
import com.phishingnet.app.ui.theme.Foam
import com.phishingnet.app.ui.theme.Kelp
import com.phishingnet.app.ui.theme.Line
import com.phishingnet.app.ui.theme.Signal

private val NodeColumn = 52.dp
private val NodeSize = 40.dp

@Composable
fun CourseScreen(
    progress: ProgressStore,
    onBack: () -> Unit,
    onOpenLessons: (Int) -> Unit,
    onOpenQuiz: (Int) -> Unit,
) {
    var confirmReset by remember { mutableStateOf(false) }
    val modules = CourseData.modules
    // Line colours: shallow at the top, deep at the bottom (tuned for light and dark themes).
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val shallow = Color(0xFF4F80AE)
    val deep = if (dark) Color(0xFF2B5A88) else Abyss

    Scaffold(
        topBar = {
            AppTopBar("Course", onBack) {
                IconButton(onClick = { confirmReset = true }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset progress")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 20.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                // A fisherman in a boat: his fishing line is the line that runs through the parts.
                FisherHeader(lineColor = shallow)
            }
            itemsIndexed(modules, key = { _, m -> m.id }) { index, module ->
                // The line gets darker as you go "deeper" into the course.
                val depth = if (modules.size <= 1) 0f else index / (modules.size - 1).toFloat()
                PartRow(
                    module = module,
                    depthColor = lerp(shallow, deep, depth),
                    passed = progress.isPassed(module.id),
                    best = progress.bestScores[module.id],
                    isLast = index == modules.lastIndex,
                    onLessons = { onOpenLessons(module.id) },
                    onQuiz = { onOpenQuiz(module.id) },
                )
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progress?") },
            text = { Text("This clears all your quiz scores.") },
            confirmButton = {
                TextButton(onClick = { progress.reset(); confirmReset = false }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PartRow(
    module: CourseModule,
    depthColor: Color,
    passed: Boolean,
    best: Int?,
    isLast: Boolean,
    onLessons: () -> Unit,
    onQuiz: () -> Unit,
) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        // Line + node
        Box(
            Modifier
                .width(NodeColumn)
                .fillMaxHeight()
                .drawBehind {
                    val x = size.width / 2
                    val stroke = 3.dp.toPx()
                    if (!isLast) {
                        drawLine(depthColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = stroke)
                    } else {
                        // The line comes down to the last part and the hook hangs straight from it.
                        drawLine(depthColor, Offset(x, 0f), Offset(x, NodeSize.toPx()), strokeWidth = stroke)
                        drawHook(depthColor, x, NodeSize.toPx(), stroke)
                    }
                },
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                Modifier
                    .size(NodeSize)
                    .clip(CircleShape)
                    .background(if (passed) Kelp else depthColor),
                contentAlignment = Alignment.Center,
            ) {
                if (passed) {
                    Icon(Icons.Filled.Check, contentDescription = "Passed", tint = Foam)
                } else {
                    Text("${module.id}", color = Foam, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Spacer(Modifier.width(12.dp))

        Column(
            Modifier
                .weight(1f)
                .padding(bottom = 18.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onLessons)
                .padding(18.dp),
        ) {
            Text(module.title, style = MaterialTheme.typography.titleLarge)
            when {
                passed -> { Spacer(Modifier.height(10.dp)); Pill("Passed with ${best ?: 0}%", Kelp) }
                best != null -> { Spacer(Modifier.height(10.dp)); Pill("Best score ${best}%", Signal) }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onLessons, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                    Text(if (passed) "Review" else "Read")
                }
                OutlinedButton(onClick = onQuiz, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                    Text("Take quiz")
                }
            }
        }
    }
}

/**
 * A hook hanging from (x, top): a straight shank continuing the line, a round bend,
 * and a short point with a barb. Drawn with the same stroke so it joins the line seamlessly.
 */
private fun DrawScope.drawHook(color: Color, x: Float, top: Float, stroke: Float) {
    val shank = 30.dp.toPx()
    val r = 11.dp.toPx()
    val bendY = top + shank
    val hook = Path().apply {
        moveTo(x, top)
        lineTo(x, bendY)
        // Bend: half circle from the shank, down and round to the left.
        arcTo(Rect(x - 2 * r, bendY - r, x, bendY + r), 0f, 180f, false)
        // Point going back up.
        lineTo(x - 2 * r, bendY - 9.dp.toPx())
    }
    drawPath(hook, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    // Barb.
    drawLine(
        color,
        Offset(x - 2 * r, bendY - 9.dp.toPx()),
        Offset(x - 2 * r + 6.dp.toPx(), bendY - 3.dp.toPx()),
        strokeWidth = stroke, cap = StrokeCap.Round,
    )
}

/** Fisherman in a boat. His rod tip sits exactly above the course line, which drops down as his fishing line. */
@Composable
private fun FisherHeader(lineColor: Color) {
    val ink = MaterialTheme.colorScheme.primary
    val water = Line
    val sun = Signal
    val bobber = MaterialTheme.colorScheme.tertiary
    val face = MaterialTheme.colorScheme.background
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(190.dp),
    ) {
        val lineX = NodeColumn.toPx() / 2
        val stroke = 3.dp.toPx()
        val waterY = 122.dp.toPx()
        val tipY = 20.dp.toPx()
        val dp = 1.dp.toPx()

        // Sun.
        drawCircle(sun.copy(alpha = 0.85f), radius = 16 * dp, center = Offset(size.width - 40 * dp, 34 * dp))

        // Water: soft tint fading down into the page.
        drawRect(
            Brush.verticalGradient(listOf(water.copy(alpha = 0.22f), water.copy(alpha = 0f)), startY = waterY, endY = size.height),
            topLeft = Offset(0f, waterY), size = Size(size.width, size.height - waterY),
        )

        // Fishing line: from the rod tip straight down — it continues as the course line below.
        drawLine(lineColor, Offset(lineX, tipY), Offset(lineX, size.height), strokeWidth = stroke)

        // Waves.
        fun wave(y: Float, amp: Float, len: Float, alpha: Float, w: Float) {
            val path = Path().apply {
                moveTo(0f, y)
                var x = 0f
                while (x < size.width) {
                    quadraticTo(x + len / 4, y - amp, x + len / 2, y)
                    quadraticTo(x + 3 * len / 4, y + amp, x + len, y)
                    x += len
                }
            }
            drawPath(path, water.copy(alpha = alpha), style = Stroke(width = w, cap = StrokeCap.Round))
        }
        wave(waterY, 3 * dp, 34 * dp, 0.9f, 2 * dp)
        wave(waterY + 16 * dp, 2.5f * dp, 46 * dp, 0.45f, 1.5f * dp)
        wave(waterY + 34 * dp, 2 * dp, 58 * dp, 0.25f, 1.5f * dp)

        // Bobber where the line meets the water.
        drawCircle(bobber, radius = 6 * dp, center = Offset(lineX, waterY - 2 * dp))
        drawLine(face, Offset(lineX - 6 * dp, waterY - 2 * dp), Offset(lineX + 6 * dp, waterY - 2 * dp), strokeWidth = 2 * dp)

        // Boat.
        val bx = lineX + 150 * dp
        val hull = Path().apply {
            moveTo(bx - 62 * dp, waterY - 16 * dp)
            lineTo(bx + 62 * dp, waterY - 16 * dp)
            quadraticTo(bx + 58 * dp, waterY + 8 * dp, bx + 40 * dp, waterY + 10 * dp)
            lineTo(bx - 44 * dp, waterY + 10 * dp)
            quadraticTo(bx - 60 * dp, waterY + 6 * dp, bx - 62 * dp, waterY - 16 * dp)
            close()
        }
        drawPath(hull, ink)
        drawLine(face.copy(alpha = 0.5f), Offset(bx - 54 * dp, waterY - 8 * dp), Offset(bx + 54 * dp, waterY - 8 * dp), strokeWidth = 2 * dp)

        // Fisherman sitting in the boat.
        val px = bx - 8 * dp
        val seat = waterY - 16 * dp
        drawRoundRect(ink, topLeft = Offset(px - 10 * dp, seat - 34 * dp), size = Size(20 * dp, 34 * dp), cornerRadius = CornerRadius(8 * dp))
        drawCircle(ink, radius = 9 * dp, center = Offset(px, seat - 45 * dp))
        // Hat: brim + crown.
        drawLine(ink, Offset(px - 14 * dp, seat - 51 * dp), Offset(px + 14 * dp, seat - 51 * dp), strokeWidth = 3 * dp, cap = StrokeCap.Round)
        drawRoundRect(ink, topLeft = Offset(px - 8 * dp, seat - 62 * dp), size = Size(16 * dp, 11 * dp), cornerRadius = CornerRadius(4 * dp))
        // Eye looking toward the line.
        drawCircle(face, radius = 1.6f * dp, center = Offset(px - 4 * dp, seat - 46 * dp))

        // Arms reaching to the rod.
        val hands = Offset(px - 26 * dp, seat - 20 * dp)
        drawLine(ink, Offset(px - 4 * dp, seat - 26 * dp), hands, strokeWidth = 5 * dp, cap = StrokeCap.Round)

        // Rod: from his hands up and over to just above the course line, bending a little.
        val rod = Path().apply {
            moveTo(hands.x + 6 * dp, hands.y + 6 * dp)
            quadraticTo(lineX + 60 * dp, tipY - 6 * dp, lineX, tipY)
        }
        drawPath(rod, ink, style = Stroke(width = 3.5f * dp, cap = StrokeCap.Round))
        // Reel.
        drawCircle(ink, radius = 4.5f * dp, center = Offset(hands.x - 2 * dp, hands.y - 4 * dp))
    }
}
