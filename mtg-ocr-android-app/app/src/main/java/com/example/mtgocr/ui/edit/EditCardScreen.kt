package com.example.mtgocr.ui.edit

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mtgocr.domain.model.CardDetails
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel

data class CardVersion(val name: String, val imageUrl: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    cardId: Long,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditCardViewModel = koinViewModel()
) {
    val cardState by viewModel.card.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // load the card when screen appears
    LaunchedEffect(cardId) { viewModel.load(cardId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val card = cardState
        if (card == null) {
            Column(modifier = modifier.padding(padding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Loading...")
            }
            return@Scaffold
        }

        var language by remember { mutableStateOf(card.language) }
        var foil by remember { mutableStateOf(card.foil) }
        var signed by remember { mutableStateOf(card.signed) }
        var condition by remember { mutableStateOf(card.condition) }
        var imageUrl by remember { mutableStateOf<String?>(null) }
        var cardVersions by remember { mutableStateOf<List<CardVersion>>(emptyList()) }
        var selectedVersion by remember { mutableStateOf<CardVersion?>(null) }

        // fetch Scryfall image async (best-effort)
        LaunchedEffect(card.name) {
            coroutineScope.launch {
                val client = OkHttpClient()
                try {
                    val url = "https://api.scryfall.com/cards/search?q=!%22${java.net.URLEncoder.encode(card.name, "utf-8")}%22&unique=prints"
                    val request = Request.Builder().url(url).get().build()
                    client.newCall(request).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string()
                            if (!body.isNullOrEmpty()) {
                                val json = JSONObject(body)
                                val data = json.getJSONArray("data")
                                val versions = mutableListOf<CardVersion>()
                                for (i in 0 until data.length()) {
                                    val cardJson = data.getJSONObject(i)
                                    val imageUris = cardJson.optJSONObject("image_uris")
                                    val imageUrl = imageUris?.optString("normal") ?: cardJson.optJSONArray("card_faces")?.optJSONObject(0)?.optJSONObject("image_uris")?.optString("normal")
                                    if (imageUrl != null) {
                                        versions.add(CardVersion(cardJson.getString("set_name"), imageUrl))
                                    }
                                }
                                cardVersions = versions
                                if (versions.isNotEmpty()) {
                                     val initialVersion = versions.find { it.name.equals(card.setName, ignoreCase = true) } ?: versions.first()
                                    selectedVersion = initialVersion
                                    imageUrl = initialVersion.imageUrl
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    imageUrl = null
                }
            }
        }

        Column(modifier = modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Image
            if (imageUrl != null) {
                AsyncImage(model = imageUrl, contentDescription = "Card image", modifier = Modifier.fillMaxWidth())
            } else {
                Text("No preview available", style = MaterialTheme.typography.bodyMedium)
            }

            Text(text = "Name: ${card.name}", style = MaterialTheme.typography.titleMedium)

            // Version Dropdown
            var versionExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = selectedVersion?.name ?: "Select Version",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Version") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth().clickable { versionExpanded = true }
                )
                DropdownMenu(
                    expanded = versionExpanded,
                    onDismissRequest = { versionExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    cardVersions.forEach { version ->
                        DropdownMenuItem(
                            text = { Text(version.name) },
                            onClick = {
                                selectedVersion = version
                                imageUrl = version.imageUrl
                                versionExpanded = false
                            }
                        )
                    }
                }
            }

            // Language Dropdown
            var languageExpanded by remember { mutableStateOf(false) }
            val languages = listOf("English", "Spanish", "French", "German", "Italian", "Portuguese", "Japanese", "Korean", "Russian", "Chinese (Simplified)", "Chinese (Traditional)")
            Box {
                OutlinedTextField(
                    value = language,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Language") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth().clickable { languageExpanded = true }
                )
                DropdownMenu(
                    expanded = languageExpanded,
                    onDismissRequest = { languageExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang) },
                            onClick = {
                                language = lang
                                languageExpanded = false
                            }
                        )
                    }
                }
            }


            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Foil")
                Switch(checked = foil, onCheckedChange = { foil = it })
                Text("Signed")
                Switch(checked = signed, onCheckedChange = { signed = it })
            }

            // Condition Dropdown
            var conditionExpanded by remember { mutableStateOf(false) }
            val conditions = listOf("Near Mint", "Lightly Played", "Moderately Played", "Heavily Played", "Damaged")
            Box {
                OutlinedTextField(
                    value = condition,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Condition") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "dropdown") },
                    modifier = Modifier.fillMaxWidth().clickable { conditionExpanded = true }
                )
                DropdownMenu(
                    expanded = conditionExpanded,
                    onDismissRequest = { conditionExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    conditions.forEach { cond ->
                        DropdownMenuItem(
                            text = { Text(cond) },
                            onClick = {
                                condition = cond
                                conditionExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = {
                val updated = card.copy(
                    language = language,
                    foil = foil,
                    signed = signed,
                    condition = condition,
                    setName = selectedVersion?.name ?: card.setName
                )
                viewModel.save(updated) { onNavigateUp() }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }
}
