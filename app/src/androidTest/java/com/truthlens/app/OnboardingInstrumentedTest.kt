package com.truthlens.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OnboardingInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun onboardingScreenIsDisplayed() {
        // First screen should show the Welcome title
        composeRule.onNodeWithText("Welcome to TruthLens").assertIsDisplayed()
    }

    @Test
    fun continueButtonAdvancesToNextPage() {
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("How It Works").assertIsDisplayed()
    }

    @Test
    fun canNavigateThroughAllPages() {
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Continue").performClick()
        // Should now be on permissions page
        composeRule.onNodeWithText("Permissions Needed").assertIsDisplayed()
    }
}
