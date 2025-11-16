package com.example.myapp.di

import com.example.domain.config.BuildConfigProvider
import com.example.network.qualifier.ApiBaseUrl
import com.example.myapp.config.AppBuildConfigProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * App configuration module providing app-specific config values.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {

    /**
     * Provides API base URL from BuildConfigProvider.
     */
    @Provides
    @Singleton
    @ApiBaseUrl
    fun provideApiBaseUrl(buildConfigProvider: BuildConfigProvider): String {
        return buildConfigProvider.apiBaseUrl
    }
}

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AppConfigBindingModule {

    @Binds
    @Singleton
    fun bindBuildConfigProvider(impl: AppBuildConfigProvider): BuildConfigProvider
}
