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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.launch
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
                AlertDialog(
                    onDismissRequest = { viewModel.dismissResult() },
                    title = { Text(text = cardDetails.name) },
                    text = {
                        Column {
                            Text("Language: ${cardDetails.language}")
                            Text("Set: ${cardDetails.setCode}")
                            Text("Collector Number: ${cardDetails.collectorNumber}")
                            Text("Year: ${cardDetails.yearOfPrint}")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { onOpenScryfall(cardDetails.name) }) {
                            Text("Search Scryfall")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissResult() }) {
                            Text("Dismiss")
                        }
                    }
                )
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