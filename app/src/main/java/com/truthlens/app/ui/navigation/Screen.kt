package com.truthlens.app.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding       : Screen("onboarding")
    object Dashboard        : Screen("dashboard")
    object ScanHistory      : Screen("scan_history")
    object Settings         : Screen("settings")
    object PrivacyPolicy    : Screen("privacy_policy")
    object HowItWorks       : Screen("how_it_works")
    object Permissions      : Screen("permissions")
    object FactCheckDetail  : Screen("fact_check_detail/{resultId}") {
        fun createRoute(resultId: String) = "fact_check_detail/$resultId"
    }
}
