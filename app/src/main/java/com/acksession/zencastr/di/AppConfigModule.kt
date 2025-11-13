package com.acksession.zencastr.di

import com.acksession.domain.config.BuildConfigProvider
import com.acksession.network.qualifier.ApiBaseUrl
import com.acksession.zencastr.BuildConfig
import com.acksession.zencastr.config.AppBuildConfigProvider
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
     * Provides API base URL from BuildConfig.
     */
    @Provides
    @Singleton
    @ApiBaseUrl
    fun provideApiBaseUrl(): String {
        return BuildConfig.API_BASE_URL
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
