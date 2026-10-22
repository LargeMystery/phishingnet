package com.phishingnet.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.phishingnet.app.ai.Examples
import com.phishingnet.app.ai.GeminiClient
import com.phishingnet.app.ai.PhishingAi
import com.phishingnet.app.ui.theme.MonoStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LinkCheckerScreen(
    initialUrl: String?,
    onInitialUrlConsumed: () -> Unit,
    onBack: () -> Unit,
) {
    var url by rememberSaveable { mutableStateOf("") }
    var state by remember { mutableStateOf<CheckState>(CheckState.Idle) }
    var job by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val clipboard = LocalClipboardManager.current
    val resultsRequester = remember { BringIntoViewRequester() }

    fun check() {
        if (url.isBlank()) return
        focus.clearFocus()
        job?.cancel()
        state = CheckState.Loading
        val u = url
        job = scope.launch {
            state = try {
                CheckState.Done(PhishingAi.checkLink(u))
            } catch (e: CancellationException) {
                throw e
            } catch (e: GeminiClient.AiException) {
                CheckState.Failed(e.message ?: "Something went wrong.")
            } catch (e: Throwable) {
                android.util.Log.e("PhishingNet", "Link check failed", e)
                CheckState.Failed("Something went wrong (${e.javaClass.simpleName}). Try again.")
            }
        }
    }

    LaunchedEffect(state) {
        if (state != CheckState.Idle) {
            withFrameNanos { }
            resultsRequester.bringIntoView()
        }
    }

    LaunchedEffect(initialUrl) {
        val shared = initialUrl ?: return@LaunchedEffect
        url = shared
        onInitialUrlConsumed()
        check()
    }

    Scaffold(
        topBar = { AppTopBar("Check a link", onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Paste a link from a message, QR code or website. The AI tells you who really owns the site and whether it looks like a trap — without opening it.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text("Try an example", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Examples.links.forEach { l ->
                    SuggestionChip(onClick = { url = l.url; job?.cancel(); state = CheckState.Idle }, label = { Text(l.label) })
                }
            }

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Link") },
                placeholder = { Text("https://…") },
                textStyle = MonoStyle,
                minLines = 2,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { check() }),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        clipboard.getText()?.text?.let { url = it.trim(); job?.cancel(); state = CheckState.Idle }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                ) { Text("Paste", modifier = Modifier.padding(vertical = 6.dp)) }
                Button(
                    onClick = { check() },
                    enabled = url.isNotBlank() && state != CheckState.Loading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(2f),
                ) { Text(if (state == CheckState.Loading) "Checking…" else "Check this link", modifier = Modifier.padding(vertical = 6.dp)) }
            }

            Spacer(Modifier.height(4.dp))
            Column(Modifier.bringIntoViewRequester(resultsRequester)) {
                CheckStateView(state, onRetry = { check() }, linksTitle = "The link, explained")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
