package com.example.mtgocr.ui.camera

import android.Manifest
import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onOpenHistory: () -> Unit,
    onOpenScryfall: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = rememberCameraController(context)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    LaunchedEffect(permissionState.status) {
        if (permissionState.status is PermissionStatus.Granted) {
            cameraController.bindToLifecycle(lifecycleOwner)
        } else if (permissionState.status is PermissionStatus.Denied && !(permissionState.status as PermissionStatus.Denied).shouldShowRationale) {
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (permissionState.status is PermissionStatus.Granted) {
                        capturePhoto(
                            context = context,
                            controller = cameraController,
                            cameraExecutor = cameraExecutor,
                            onImageCaptured = { bytes -> viewModel.processImage(bytes) },
                            onError = { message -> viewModel.reportError(message) }
                        )
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Camera permission is required")
                        }
                        permissionState.launchPermissionRequest()
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Capture")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(text = "MTG Card Scanner") },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CameraPreview(controller = cameraController)

            // Scryfall popup states
            val (scryfallUrl, setScryfallUrl) = remember { mutableStateOf<String?>(null) }
            val (scryImageUrl, setScryImageUrl) = remember { mutableStateOf<String?>(null) }
            val (showScryDialog, setShowScryDialog) = remember { mutableStateOf(false) }
            val (isFetchingScry, setIsFetchingScry) = remember { mutableStateOf(false) }
            var foil by remember { mutableStateOf(false) }
            var signed by remember { mutableStateOf(false) }
            var language by remember { mutableStateOf("") }
            var condition by remember { mutableStateOf("NM") }
            var cardMarketId by remember { mutableStateOf<Int?>(null) }


            if (uiState.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Recognizing card...", color = Color.White)
                    }
                }
            }

            uiState.cardDetails?.let { cardDetails ->
                LaunchedEffect(cardDetails) {
                    setIsFetchingScry(true)
                    setShowScryDialog(true)
                    foil = cardDetails.foil
                    signed = cardDetails.signed
                    condition = when (cardDetails.condition.uppercase()) {
                        "NM", "NEAR MINT" -> "NM"
                        "EX", "EXCELLENT" -> "EX"
                        "GD", "GOOD" -> "GD"
                        "PL", "PLAYED" -> "PL"
                        "PO", "POOR" -> "PO"
                        else -> "NM"
                    }
                    language = cardDetails.language.ifBlank { "English" }
                    cardMarketId = cardDetails.cardMarketId
                    coroutineScope.launch {
                        val client = OkHttpClient()
                        try {
                            val setCode = cardDetails.setCode.trim().lowercase()
                            val collectorNumber = cardDetails.collectorNumber.trim()

                            val endpoints = mutableListOf<Pair<String, Boolean>>().apply {
                                if (setCode.isNotBlank() && collectorNumber.isNotBlank()) {
                                    add("https://api.scryfall.com/cards/$setCode/${collectorNumber.lowercase()}" to false)
                                    add(
                                        "https://api.scryfall.com/cards/search?q=${URLEncoder.encode("set:$setCode cn:$collectorNumber", "utf-8")}&unique=prints" to true
                                    )
                                    add(
                                        "https://api.scryfall.com/cards/search?q=${URLEncoder.encode("set:$setCode ${cardDetails.name}", "utf-8")}&unique=prints" to true
                                    )
                                }
                                add(
                                    "https://api.scryfall.com/cards/search?q=${URLEncoder.encode(cardDetails.name, "utf-8")}&unique=prints" to true
                                )
                            }

                            setScryImageUrl(null)
                            setScryfallUrl(null)

                            val result = withContext(Dispatchers.IO) {
                                var imageUrl: String? = null
                                var scryfallLink: String? = null
                                var cardMarketIdResult: Int? = null

                                for ((url, expectsList) in endpoints) {
                                    val request = Request.Builder().url(url).get().build()
                                    try {
                                        client.newCall(request).execute().use { resp ->
                                            if (!resp.isSuccessful) return@use
                                            val body = resp.body?.string()
                                            if (body.isNullOrEmpty()) return@use
                                            val json = JSONObject(body)
                                            if (!expectsList && json.optString("object") == "card") {
                                                val directImage = json.optJSONObject("image_uris")
                                                    ?.optString("normal")
                                                    ?.takeIf { !it.isNullOrBlank() }
                                                    ?: json.optJSONArray("card_faces")
                                                        ?.takeIf { it.length() > 0 }
                                                        ?.getJSONObject(0)
                                                        ?.optJSONObject("image_uris")
                                                        ?.optString("normal")
                                                        ?.takeIf { !it.isNullOrBlank() }
                                                if (!directImage.isNullOrBlank()) {
                                                    imageUrl = directImage
                                                    scryfallLink = json.optString("scryfall_uri")
                                                    cardMarketIdResult = json.optInt("cardmarket_id", 0).takeIf { it > 0 }
                                                }
                                            } else if (expectsList && json.optString("object") == "list") {
                                                val data = json.optJSONArray("data")
                                                if (data != null && data.length() > 0) {
                                                    val cardJson = data.getJSONObject(0)
                                                    val listImage = cardJson.optJSONObject("image_uris")
                                                        ?.optString("normal")
                                                        ?.takeIf { !it.isNullOrBlank() }
                                                        ?: cardJson.optJSONArray("card_faces")
                                                            ?.takeIf { it.length() > 0 }
                                                            ?.getJSONObject(0)
                                                            ?.optJSONObject("image_uris")
                                                            ?.optString("normal")
                                                            ?.takeIf { !it.isNullOrBlank() }
                                                    if (!listImage.isNullOrBlank()) {
                                                        imageUrl = listImage
                                                        scryfallLink = cardJson.optString("scryfall_uri")
                                                        cardMarketIdResult = cardJson.optInt("cardmarket_id", 0).takeIf { it > 0 }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (_: Exception) {
                                        // try the next endpoint
                                    }

                                    if (!imageUrl.isNullOrBlank()) break
                                }

                                Triple(imageUrl, scryfallLink, cardMarketIdResult)
                            }

                            setScryImageUrl(result.first)
                            setScryfallUrl(result.second)
                            result.third?.let { cardMarketId = it }
                        } catch (_: Exception) {
                            setScryImageUrl(null)
                            setScryfallUrl(null)
                        } finally {
                            setIsFetchingScry(false)
                        }
                    }
                }

                if (showScryDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            setShowScryDialog(false)
                            viewModel.dismissResult()
                        },
                        title = { Text(text = cardDetails.name) },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                if (isFetchingScry) {
                                    CircularProgressIndicator()
                                } else {
                                    scryImageUrl?.let {
                                        AsyncImage(
                                            model = it,
                                            contentDescription = "Card image",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } ?: Text("No preview available")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Language: ${cardDetails.language}")
                                Text("Set: ${cardDetails.setCode}")
                                Text("Collector Number: ${cardDetails.collectorNumber}")
                                Text("Year: ${cardDetails.yearOfPrint}")
                                cardMarketId?.let { Text("CardMarket ID: $it") }
                                scryfallUrl?.let { url ->
                                    TextButton(onClick = {
                                        onOpenScryfall(url)
                                        uriHandler.openUri(url)
                                    }) {
                                        Text("View on Scryfall", textDecoration = TextDecoration.Underline)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Foil")
                                    Spacer(modifier = Modifier.weight(1f))
                                    Switch(checked = foil, onCheckedChange = { foil = it })
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Signed")
                                    Spacer(modifier = Modifier.weight(1f))
                                    Switch(checked = signed, onCheckedChange = { signed = it })
                                }

                                val baseLanguages = listOf("English", "Spanish", "French", "German", "Italian", "Portuguese", "Japanese", "Korean", "Russian", "Chinese (Simplified)", "Chinese (Traditional)")
                                val languageOptions = remember(cardDetails.language) {
                                    if (cardDetails.language.isNotBlank() && cardDetails.language !in baseLanguages) baseLanguages + cardDetails.language else baseLanguages
                                }
                                val languageScrollState = rememberScrollState()
                                Text("Language", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(languageScrollState),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    languageOptions.forEach { lang ->
                                        val selected = language == lang
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .selectable(
                                                    selected = selected,
                                                    onClick = { language = lang },
                                                    role = Role.RadioButton
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            RadioButton(selected = selected, onClick = null)
                                            Text(text = lang, modifier = Modifier.padding(start = 4.dp))
                                        }
                                    }
                                }

                                val conditions = listOf("NM", "EX", "GD", "PL", "PO")
                                val conditionScrollState = rememberScrollState()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Condition", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(conditionScrollState),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    conditions.forEach { cond ->
                                        val selected = condition == cond
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .selectable(
                                                    selected = selected,
                                                    onClick = { condition = cond },
                                                    role = Role.RadioButton
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            RadioButton(selected = selected, onClick = null)
                                            Text(text = cond, modifier = Modifier.padding(start = 4.dp))
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val updatedCard = cardDetails.copy(
                                    foil = foil,
                                    signed = signed,
                                    condition = condition,
                                    language = language,
                                    cardMarketId = cardMarketId
                                )
                                viewModel.saveCard(updatedCard)
                                setShowScryDialog(false)
                                viewModel.dismissResult()
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                setShowScryDialog(false)
                                viewModel.dismissResult()
                            }) {
                                Text("Dismiss")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(controller: LifecycleCameraController) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                this.controller = controller
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            previewView.controller = controller
        }
    )
}

@Composable
private fun rememberCameraController(context: Context): LifecycleCameraController {
    return remember(context) {
        LifecycleCameraController(context).apply {
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
        }
    }
}

private fun capturePhoto(
    context: Context,
    controller: LifecycleCameraController,
    cameraExecutor: ExecutorService,
    onImageCaptured: (ByteArray) -> Unit,
    onError: (String) -> Unit
) {
    val photoFile = File.createTempFile("mtg_card_", ".jpg", context.cacheDir)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    controller.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bytes = photoFile.inputStream().use { it.readBytes() }
                photoFile.delete()
                onImageCaptured(bytes)
            }

            override fun onError(exception: ImageCaptureException) {
                photoFile.delete()
                onError(exception.message ?: "Unable to capture image")
            }
        }
    )
}