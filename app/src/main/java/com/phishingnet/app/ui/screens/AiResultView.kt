package com.phishingnet.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.phishingnet.app.ai.AiLink
import com.phishingnet.app.ai.AiReport
import com.phishingnet.app.ai.Verdict
import com.phishingnet.app.ui.theme.Buoy
import com.phishingnet.app.ui.theme.Foam
import com.phishingnet.app.ui.theme.InkBlue
import com.phishingnet.app.ui.theme.Kelp
import com.phishingnet.app.ui.theme.MonoStyle
import com.phishingnet.app.ui.theme.Signal

/** The state of an AI check, shared by the email, link and file screens. */
sealed class CheckState {
    data object Idle : CheckState()
    data object Loading : CheckState()
    data class Done(val report: AiReport) : CheckState()
    data class Failed(val message: String) : CheckState()
}

fun verdictColor(v: Verdict): Color = when (v) {
    Verdict.DANGEROUS -> Buoy
    Verdict.SUSPICIOUS -> Signal
    Verdict.SAFE -> Kelp
}

/** Shows loading, an error or the AI's report. */
@Composable
fun CheckStateView(state: CheckState, onRetry: () -> Unit, linksTitle: String = "Links in this email") {
    when (state) {
        CheckState.Idle -> {}
        CheckState.Loading -> LoadingCard()
        is CheckState.Failed -> ErrorCard(state.message, onRetry)
        is CheckState.Done -> AiReportView(state.report, linksTitle)
    }
}

@Composable
private fun LoadingCard() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text("The AI is checking…", style = MaterialTheme.typography.titleLarge)
            Text("This usually takes a few seconds.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Buoy.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Text("Couldn't check it", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
fun AiReportView(r: AiReport, linksTitle: String) {
    val (banner, onBanner) = when (r.verdict) {
        Verdict.DANGEROUS -> Color(0xFFC63B17) to Foam
        Verdict.SUSPICIOUS -> Signal to InkBlue
        Verdict.SAFE -> Color(0xFF22704F) to Foam
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // ── Verdict ──
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(banner)
                .padding(22.dp),
        ) {
            Text(r.verdict.label, style = MaterialTheme.typography.headlineLarge, color = onBanner)
            Spacer(Modifier.height(4.dp))
            Text("Risk ${r.riskScore} of 100", style = MaterialTheme.typography.labelLarge, color = onBanner.copy(alpha = 0.85f))
            if (r.summary.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(r.summary, style = MaterialTheme.typography.bodyLarge, color = onBanner)
            }
        }

        r.fileType?.let {
            Section("What this file really is")
            Text(it, style = MaterialTheme.typography.bodyLarge)
        }

        if (r.advice.isNotBlank()) {
            Section("What to do")
            Text(r.advice, style = MaterialTheme.typography.bodyLarge)
        }

        if (r.redFlags.isNotEmpty()) {
            Section("${r.redFlags.size} red flag${if (r.redFlags.size == 1) "" else "s"}")
            r.redFlags.forEach { f ->
                FlagRow(verdictColor(if (r.verdict == Verdict.SAFE) Verdict.SUSPICIOUS else r.verdict)) {
                    Column {
                        Text(f.title, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(2.dp))
                        Text(f.explanation, style = MaterialTheme.typography.bodyMedium)
                        if (f.evidence.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("“${f.evidence}”", style = MonoStyle, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        if (r.links.isNotEmpty()) {
            Section(linksTitle)
            r.links.take(15).forEach { LinkCard(it) }
            Text(
                "Don't open links to test them. To visit a service, type its address yourself or use its official app.",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (r.safeSignals.isNotEmpty()) {
            Section("What looks normal")
            r.safeSignals.forEach { BulletRow(it, color = Kelp) }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Checked by Google Gemini (${r.model}). AI can make mistakes — when money, passwords or codes are involved, verify with the sender another way.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun Section(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 10.dp))
}

@Composable
private fun LinkCard(l: AiLink) {
    val color = verdictColor(l.verdict)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                l.url,
                style = MonoStyle.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(8.dp))
            Pill(l.verdict.label, color)
        }
        if (l.reason.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(l.reason, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
