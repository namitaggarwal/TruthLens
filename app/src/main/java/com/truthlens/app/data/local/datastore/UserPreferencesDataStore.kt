package com.truthlens.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.truthlens.app.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val MASTER_MONITORING = booleanPreferencesKey("master_monitoring")
        val OVERLAY_ENABLED   = booleanPreferencesKey("overlay_enabled")
        val SCAN_DELAY        = intPreferencesKey("scan_delay_seconds")
        val BATTERY_SAVER     = booleanPreferencesKey("battery_saver")
        val WIFI_ONLY_BACKEND = booleanPreferencesKey("wifi_only_backend")
        val OCR_ENABLED       = booleanPreferencesKey("ocr_enabled")
        val NOTIF_ACCESS      = booleanPreferencesKey("notification_access")
        val ONBOARDING_DONE   = booleanPreferencesKey("onboarding_completed")
    }

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { e ->
            Timber.e(e, "Error reading preferences")
            emit(emptyPreferences())
        }
        .map { prefs ->
            UserPreferences(
                masterMonitoringEnabled = prefs[Keys.MASTER_MONITORING] ?: false,
                overlayEnabled          = prefs[Keys.OVERLAY_ENABLED]   ?: true,
                scanDelaySeconds        = prefs[Keys.SCAN_DELAY]        ?: 2,
                batterySaverMode        = prefs[Keys.BATTERY_SAVER]     ?: false,
                wifiOnlyBackend         = prefs[Keys.WIFI_ONLY_BACKEND] ?: false,
                ocrEnabled              = prefs[Keys.OCR_ENABLED]       ?: false,
                notificationAccessEnabled = prefs[Keys.NOTIF_ACCESS]   ?: false,
                onboardingCompleted     = prefs[Keys.ONBOARDING_DONE]   ?: false
            )
        }

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        val current = preferences.let {
            var value = UserPreferences()
            // We use updateData directly to keep it atomic
            dataStore.updateData { prefs ->
                val cur = UserPreferences(
                    masterMonitoringEnabled = prefs[Keys.MASTER_MONITORING] ?: false,
                    overlayEnabled          = prefs[Keys.OVERLAY_ENABLED]   ?: true,
                    scanDelaySeconds        = prefs[Keys.SCAN_DELAY]        ?: 2,
                    batterySaverMode        = prefs[Keys.BATTERY_SAVER]     ?: false,
                    wifiOnlyBackend         = prefs[Keys.WIFI_ONLY_BACKEND] ?: false,
                    ocrEnabled              = prefs[Keys.OCR_ENABLED]       ?: false,
                    notificationAccessEnabled = prefs[Keys.NOTIF_ACCESS]   ?: false,
                    onboardingCompleted     = prefs[Keys.ONBOARDING_DONE]   ?: false
                )
                val updated = transform(cur)
                prefs.toMutablePreferences().apply {
                    set(Keys.MASTER_MONITORING, updated.masterMonitoringEnabled)
                    set(Keys.OVERLAY_ENABLED,   updated.overlayEnabled)
                    set(Keys.SCAN_DELAY,        updated.scanDelaySeconds)
                    set(Keys.BATTERY_SAVER,     updated.batterySaverMode)
                    set(Keys.WIFI_ONLY_BACKEND, updated.wifiOnlyBackend)
                    set(Keys.OCR_ENABLED,       updated.ocrEnabled)
                    set(Keys.NOTIF_ACCESS,      updated.notificationAccessEnabled)
                    set(Keys.ONBOARDING_DONE,   updated.onboardingCompleted)
                }
            }
        }
    }

    suspend fun setMasterMonitoring(enabled: Boolean) {
        dataStore.edit { it[Keys.MASTER_MONITORING] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_DONE] = completed }
    }

    suspend fun setOcrEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.OCR_ENABLED] = enabled }
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.OVERLAY_ENABLED] = enabled }
    }
}
