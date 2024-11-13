package com.example.urvoices.di

import android.content.Context
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.db.AppDatabase
import com.example.urvoices.data.db.Dao.BlockedUserDao
import com.example.urvoices.data.repository.NotificationRepository
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.repository.UserRepository
import com.example.urvoices.data.service.FirebaseNotificationService
import com.example.urvoices.data.service.FirebasePostService
import com.example.urvoices.data.service.FirebaseUserService
import com.example.urvoices.utils.SharedPreferencesHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /*
        Local Data
    */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideBlockDao(
        @ApplicationContext context: Context,
        database: AppDatabase
    ): BlockedUserDao {
        return database.blockedUserDao()
    }

    /*
        Repository
    */

    @Provides
    @Singleton
    fun provideUserRepo(
        firebaseUserService: FirebaseUserService,
        sharedPref: SharedPreferencesHelper
    ): UserRepository = UserRepository(firebaseUserService, sharedPref)

    @Provides
    @Singleton
    fun provideNotificationRepo(
        notificationService: FirebaseNotificationService
    ): NotificationRepository = NotificationRepository(notificationService)
}