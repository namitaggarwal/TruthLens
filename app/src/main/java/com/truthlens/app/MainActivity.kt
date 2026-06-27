package com.truthlens.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.truthlens.app.service.OverlayService
import com.truthlens.app.ui.navigation.TruthLensNavGraph
import com.truthlens.app.ui.theme.TruthLensTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingResultId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            TruthLensTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TruthLensNavGraph(
                        pendingResultId = pendingResultId,
                        onResultIdConsumed = { pendingResultId = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == OverlayService.ACTION_OPEN_DETAILS) {
            pendingResultId = intent.getStringExtra(OverlayService.EXTRA_RESULT_ID)
        }
    }
}
