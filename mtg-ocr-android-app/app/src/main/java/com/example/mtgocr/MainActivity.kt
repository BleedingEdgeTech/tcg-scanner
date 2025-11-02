package com.example.mtgocr

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mtgocr.ui.navigation.MtgOcrNavGraph
import com.example.mtgocr.ui.theme.MtgOcrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MtgOcrTheme {
                MtgOcrNavGraph(onOpenScryfall = ::openScryfall)
            }
        }
    }

    private fun openScryfall(cardName: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://scryfall.com/search?q=$cardName"))
        startActivity(intent)
    }
}