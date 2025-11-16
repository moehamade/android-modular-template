package com.example.remoteconfig.di

import com.example.remoteconfig.FeatureFlagManager
import com.example.remoteconfig.FirebaseFeatureFlagManager
import com.example.remoteconfig.R
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return Firebase.remoteConfig.apply {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface RemoteConfigBindingModule {

    @Binds
    @Singleton
    fun bindFeatureFlagManager(impl: FirebaseFeatureFlagManager): FeatureFlagManager

}
