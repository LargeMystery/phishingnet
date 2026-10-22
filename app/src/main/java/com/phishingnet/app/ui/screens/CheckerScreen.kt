package com.phishingnet.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.phishingnet.app.ai.AttachedFile
import com.phishingnet.app.ai.Examples
import com.phishingnet.app.ai.GeminiClient
import com.phishingnet.app.ai.PhishingAi
import com.phishingnet.app.ui.LoadedFile
import com.phishingnet.app.ui.SharedContent
import com.phishingnet.app.ui.formatSize
import com.phishingnet.app.ui.loadFile
import com.phishingnet.app.ui.theme.Buoy
import com.phishingnet.app.ui.theme.MonoStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckerScreen(
    initialShare: SharedContent?,
    onInitialShareConsumed: () -> Unit,
    onBack: () -> Unit,
) {
    var sender by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    val files = remember { mutableStateListOf<AttachedFile>() }
    var fileMessage by remember { mutableStateOf<String?>(null) }
    var loadingFiles by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf<CheckState>(CheckState.Idle) }
    var job by remember { mutableStateOf<Job?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val resultsRequester = remember { BringIntoViewRequester() }

    fun check() {
        focus.clearFocus()
        job?.cancel()
        state = CheckState.Loading
        val s = sender; val sub = subject; val b = body; val f = files.toList()
        job = scope.launch {
            state = try {
                CheckState.Done(PhishingAi.checkEmail(s, sub, b, f))
            } catch (e: CancellationException) {
                throw e
            } catch (e: GeminiClient.AiException) {
                CheckState.Failed(e.message ?: "Something went wrong.")
            } catch (e: Throwable) {
                android.util.Log.e("PhishingNet", "Email check failed", e)
                CheckState.Failed("Something went wrong (${e.javaClass.simpleName}). Try again.")
            }
        }
    }

    fun reset() { job?.cancel(); state = CheckState.Idle }

    LaunchedEffect(state) {
        if (state != CheckState.Idle) {
            withFrameNanos { }
            resultsRequester.bringIntoView()
        }
    }

    /** A saved email (.eml) fills the form; any other file becomes an attachment. */
    suspend fun addUris(uris: List<android.net.Uri>) {
        if (uris.isEmpty()) return
        loadingFiles = true
        val problems = mutableListOf<String>()
        for (uri in uris.take(10)) {
            when (val f = loadFile(context, uri)) {
                is LoadedFile.Email -> { sender = ""; subject = ""; body = f.text }
                is LoadedFile.Attachment -> if (files.none { it.name == f.file.name && it.bytes.size == f.file.bytes.size }) files += f.file
                is LoadedFile.Failed -> problems += "${f.name}: ${f.reason}"
            }
        }
        fileMessage = problems.joinToString("\n").ifBlank { null }
        reset()
        loadingFiles = false
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        scope.launch { addUris(uris) }
    }

    // Text or files shared from another app (e.g. Gmail → Share → Phishing net).
    LaunchedEffect(initialShare) {
        val share = initialShare ?: return@LaunchedEffect
        scope.launch {
            if (share.text != null) { sender = ""; subject = ""; body = share.text }
            if (share.uris.isNotEmpty()) addUris(share.uris)
            check()
        }
        onInitialShareConsumed()
    }

    Scaffold(
        topBar = { AppTopBar("Check an email", onBack) },
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
                "Google's Gemini AI reads the email like a security expert would. The email and any attachments you add are sent to Google to be checked.",
                style = MaterialTheme.typography.bodySmall,
            )

            Text("Try an example", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Examples.emails.forEach { e ->
                    SuggestionChip(
                        onClick = { sender = e.sender; subject = e.subject; body = e.body; files.clear(); reset() },
                        label = { Text(e.label) },
                    )
                }
            }

            OutlinedTextField(
                value = sender,
                onValueChange = { sender = it },
                label = { Text("From") },
                placeholder = { Text("Name <address@domain.com>") },
                supportingText = { Text("The full sender address helps the AI a lot.") },
                singleLine = true,
                textStyle = MonoStyle,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Email text") },
                placeholder = {
                    Text("Paste the message here. For the best result, paste the full original email with headers — in Gmail: ⋮ › Show original › Copy to clipboard. Buttons don't copy with the text: long-press a button › Copy link address and paste it here too.")
                },
                minLines = 7,
                textStyle = MonoStyle,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Attachments ──
            OutlinedButton(
                onClick = { picker.launch(arrayOf("*/*")) },
                enabled = !loadingFiles,
                shape = RoundedCornerShape(14.dp),
            ) {
                if (loadingFiles) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (files.isEmpty()) "Add attachment" else "Add another attachment")
            }
            files.forEach { f -> FileChip(f) { files.remove(f); reset() } }
            fileMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Buoy) }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { sender = ""; subject = ""; body = ""; files.clear(); fileMessage = null; reset() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                ) { Text("Clear", modifier = Modifier.padding(vertical = 6.dp)) }
                Button(
                    onClick = { check() },
                    enabled = (sender.isNotBlank() || subject.isNotBlank() || body.isNotBlank() || files.isNotEmpty()) &&
                        state != CheckState.Loading && !loadingFiles,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(2f),
                ) { Text(if (state == CheckState.Loading) "Checking…" else "Check this email", modifier = Modifier.padding(vertical = 6.dp)) }
            }

            Spacer(Modifier.height(4.dp))
            Column(Modifier.bringIntoViewRequester(resultsRequester)) {
                CheckStateView(state, onRetry = { check() })
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

/** A file the user added, with a remove button. */
@Composable
fun FileChip(f: AttachedFile, onRemove: (() -> Unit)?) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 14.dp, end = if (onRemove == null) 14.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
            Text(f.name, style = MonoStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(formatSize(f.bytes.size), style = MaterialTheme.typography.bodySmall)
        }
        if (onRemove != null) {
            IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, contentDescription = "Remove ${f.name}") }
        }
    }
}
