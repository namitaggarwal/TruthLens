package com.truthlens.app.di

import android.content.Context
import androidx.room.Room
import com.truthlens.app.data.local.db.TruthLensDatabase
import com.truthlens.app.data.local.db.dao.AppSettingsDao
import com.truthlens.app.data.local.db.dao.ScanHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): TruthLensDatabase =
        Room.databaseBuilder(ctx, TruthLensDatabase::class.java, TruthLensDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideScanHistoryDao(db: TruthLensDatabase): ScanHistoryDao = db.scanHistoryDao()

    @Provides fun provideAppSettingsDao(db: TruthLensDatabase): AppSettingsDao = db.appSettingsDao()
}
