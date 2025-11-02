package com.example.mtgocr.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mtgocr.domain.model.CardDetails
import com.example.mtgocr.ui.camera.CameraScreen
import com.example.mtgocr.ui.history.HistoryScreen

private const val CAMERA_ROUTE = "camera"
private const val HISTORY_ROUTE = "history"

@Composable
fun MtgOcrNavGraph(
    onOpenScryfall: (String) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = CAMERA_ROUTE) {
        composable(CAMERA_ROUTE) {
            CameraScreen(
                onOpenHistory = { navController.navigate(HISTORY_ROUTE) },
                onOpenScryfall = onOpenScryfall
            )
        }
        composable(HISTORY_ROUTE) {
            HistoryScreen(
                onNavigateUp = { navController.popBackStack() },
                onViewCard = { card: CardDetails -> onOpenScryfall(card.name) }
            )
        }
    }
}