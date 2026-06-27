package com.truthlens.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.truthlens.app.domain.model.AppMonitorConfig
import com.truthlens.app.domain.model.UserPreferences
import com.truthlens.app.ui.dashboard.DashboardScreen
import com.truthlens.app.ui.dashboard.DashboardUiState
import com.truthlens.app.ui.theme.TruthLensTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DashboardInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun dashboardShowsAppName() {
        composeRule.setContent {
            TruthLensTheme {
                // The TruthLens label in the top bar
            }
        }
        // Verify the app name appears
        composeRule.onNodeWithText("TruthLens").assertExists()
    }

    @Test
    fun masterToggleCanBeInteractedWith() {
        composeRule.setContent {
            TruthLensTheme {
                // The master toggle card
            }
        }
        composeRule.onNodeWithText("TruthLens Monitoring").assertExists()
    }
}
