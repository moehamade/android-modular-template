package com.acksession.zencastr.config

import com.acksession.domain.config.BuildConfigProvider
import com.acksession.zencastr.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBuildConfigProvider @Inject constructor() : BuildConfigProvider {
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val buildType: String = BuildConfig.BUILD_TYPE
    override val environment: String = BuildConfig.ENVIRONMENT
    override val versionName: String = BuildConfig.VERSION_NAME
}
