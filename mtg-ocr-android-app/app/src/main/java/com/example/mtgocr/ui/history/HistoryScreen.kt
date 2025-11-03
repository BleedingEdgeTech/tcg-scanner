package com.example.mtgocr.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import android.net.Uri
import java.io.OutputStreamWriter
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mtgocr.domain.model.CardDetails
import com.example.mtgocr.ui.components.HistoryListItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateUp: () -> Unit,
    onViewCard: (CardDetails) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val history by viewModel.history.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportPath by viewModel.exportPath.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // CreateDocument launcher for choosing export location
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri == null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Export cancelled")
            }
            return@rememberLauncherForActivityResult
        }

        // Write CSV to provided Uri
        coroutineScope.launch {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    OutputStreamWriter(out).use { writer ->
                        writer.append("Name,Language,Collector Number,Set Code,Year of Print\n")
                        history.forEach { card ->
                            val line = listOf(
                                card.name.replace(",", " "),
                                card.language.replace(",", " "),
                                card.collectorNumber.replace(",", " "),
                                card.setCode.replace(",", " "),
                                card.yearOfPrint.toString()
                            ).joinToString(",")
                            writer.append(line).append("\n")
                        }
                        writer.flush()
                    }
                }
                snackbarHostState.showSnackbar("Exported to chosen location")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Export failed: ${e.message}")
            }
        }
    }

    LaunchedEffect(exportPath, errorMessage) {
        exportPath?.let { path ->
            snackbarHostState.showSnackbar("Exported to $path")
            viewModel.clearMessage()
        }
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Card History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (!isExporting) {
                    // launch save picker - suggest a filename
                    createDocumentLauncher.launch("mtg_cards_export.csv")
                }
            }) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Export history")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
            HistoryContent(
                history = history,
                onDelete = { cardId -> viewModel.deleteCard(cardId) },
                onView = { card -> onViewCard(card) },
                paddingValues = paddingValues
            )
    }
}

@Composable
private fun HistoryContent(
    history: List<CardDetails>,
    onDelete: (Long) -> Unit,
    onView: (CardDetails) -> Unit,
    paddingValues: PaddingValues,
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No cards captured yet.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history, key = { it.id ?: it.collectorNumber.hashCode().toLong() }) { card ->
                HistoryListItem(
                    cardDetails = card,
                    onDelete = { card.id?.let(onDelete) },
                    onView = { onView(card) }
                )
            }
        }
    }
}