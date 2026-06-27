package com.truthlens.app.di

import com.truthlens.app.domain.provider.FactCheckProvider
import com.truthlens.app.factcheck.MockFactCheckProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FactCheckModule {

    // Swap MockFactCheckProvider → RemoteFactCheckProvider for production
    @Binds @Singleton
    abstract fun bindFactCheckProvider(impl: MockFactCheckProvider): FactCheckProvider
}
