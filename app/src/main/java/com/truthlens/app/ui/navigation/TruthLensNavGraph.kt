package com.truthlens.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.truthlens.app.ui.dashboard.DashboardScreen
import com.truthlens.app.ui.details.FactCheckDetailScreen
import com.truthlens.app.ui.history.ScanHistoryScreen
import com.truthlens.app.ui.onboarding.OnboardingScreen
import com.truthlens.app.ui.settings.HowItWorksScreen
import com.truthlens.app.ui.settings.PermissionsScreen
import com.truthlens.app.ui.settings.PrivacyPolicyScreen
import com.truthlens.app.ui.settings.SettingsScreen

@Composable
fun TruthLensNavGraph(
    pendingResultId: String? = null,
    onResultIdConsumed: () -> Unit = {},
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onCompleted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToHistory  = { navController.navigate(Screen.ScanHistory.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.FactCheckDetail.route,
            arguments = listOf(navArgument("resultId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resultId = backStackEntry.arguments?.getString("resultId") ?: ""
            FactCheckDetailScreen(
                resultId = resultId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ScanHistory.route) {
            ScanHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenDetail = { id ->
                    navController.navigate(Screen.FactCheckDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack             = { navController.popBackStack() },
                onPrivacyPolicy    = { navController.navigate(Screen.PrivacyPolicy.route) },
                onHowItWorks       = { navController.navigate(Screen.HowItWorks.route) },
                onManagePermissions = { navController.navigate(Screen.Permissions.route) }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.HowItWorks.route) {
            HowItWorksScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Permissions.route) {
            PermissionsScreen(onBack = { navController.popBackStack() })
        }
    }

    // Handle deep-link from overlay "View Details" tap
    LaunchedEffect(pendingResultId) {
        if (!pendingResultId.isNullOrEmpty()) {
            navController.navigate(Screen.FactCheckDetail.createRoute(pendingResultId))
            onResultIdConsumed()
        }
    }
}
