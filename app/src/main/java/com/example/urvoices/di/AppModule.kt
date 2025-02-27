package com.example.urvoices.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.example.urvoices.utils.SharedPreferencesHelper
import com.example.urvoices.utils.UserPreferences
import com.example.urvoices.utils.Worker.CustomWorkerFactory
import com.example.urvoices.viewmodel.State.AppGlobalState
import com.facebook.appevents.UserDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSharedPreferencesHelper(
        @ApplicationContext context: Context
    ): SharedPreferencesHelper {
        return SharedPreferencesHelper(context)
    }

    @Provides
    @Singleton
    fun provideUserDataStore(
        @ApplicationContext context: Context
    ): UserPreferences = UserPreferences(context)

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWorkerFactory(
        customWorkerFactory: CustomWorkerFactory
    ): WorkerFactory = customWorkerFactory

}