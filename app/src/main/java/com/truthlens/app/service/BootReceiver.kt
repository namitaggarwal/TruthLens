package com.truthlens.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.truthlens.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        Timber.d("Boot/update received — checking if monitoring should restart")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = userPreferencesRepository.get().first()
                if (prefs.masterMonitoringEnabled) {
                    Timber.d("Restarting monitoring service after boot")
                    ContextCompat.startForegroundService(
                        context,
                        MonitoringForegroundService.startIntent(context)
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
