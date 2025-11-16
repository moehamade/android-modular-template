package com.example.notifications.di

import com.example.notifications.NotificationHandler
import com.example.notifications.NotificationHandlerImpl
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationsModule {

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface NotificationsBindingModule {

    @Binds
    @Singleton
    fun bindNotificationManager(
        impl: NotificationHandlerImpl
    ): NotificationHandler
}
