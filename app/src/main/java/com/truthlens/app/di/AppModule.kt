package com.truthlens.app.di

import com.truthlens.app.data.repository.AppSettingsRepositoryImpl
import com.truthlens.app.data.repository.ScanHistoryRepositoryImpl
import com.truthlens.app.data.repository.UserPreferencesRepositoryImpl
import com.truthlens.app.domain.repository.AppSettingsRepository
import com.truthlens.app.domain.repository.ScanHistoryRepository
import com.truthlens.app.domain.repository.UserPreferencesRepository
import com.truthlens.app.domain.provider.OcrProvider
import com.truthlens.app.ocr.MlKitOcrProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton
    abstract fun bindScanHistoryRepository(impl: ScanHistoryRepositoryImpl): ScanHistoryRepository

    @Binds @Singleton
    abstract fun bindAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepository

    @Binds @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds @Singleton
    abstract fun bindOcrProvider(impl: MlKitOcrProvider): OcrProvider
}
