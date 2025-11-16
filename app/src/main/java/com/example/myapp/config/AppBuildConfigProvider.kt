package com.example.myapp.config

import com.example.domain.config.BuildConfigProvider
import com.example.myapp.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBuildConfigProvider @Inject constructor() : BuildConfigProvider {
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val buildType: String = BuildConfig.BUILD_TYPE
    override val environment: String = BuildConfig.ENVIRONMENT
    override val versionName: String = BuildConfig.VERSION_NAME
    override val apiBaseUrl: String = BuildConfig.API_BASE_URL
}
