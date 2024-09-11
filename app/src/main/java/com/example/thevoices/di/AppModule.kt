package com.example.thevoices.di

import android.content.Context
import com.example.thevoices.utils.SharedPreferencesHelper
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
}