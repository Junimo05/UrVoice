package com.example.urvoices.di

import android.content.Context
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.db.AppDatabase
import com.example.urvoices.data.db.Dao.BlockedUserDao
import com.example.urvoices.data.db.Dao.DeletedPostDao
import com.example.urvoices.data.db.Dao.NotificationDao
import com.example.urvoices.data.db.Dao.SavedPostDao
import com.example.urvoices.data.repository.NotificationRepository
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.repository.UserRepository
import com.example.urvoices.data.service.FirebaseNotificationService
import com.example.urvoices.data.service.FirebasePostService
import com.example.urvoices.data.service.FirebaseUserService
import com.example.urvoices.utils.SharedPreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        database: AppDatabase
    ): BlockedUserDao {
        return database.blockedUserDao()
    }

    @Provides
    @Singleton
    fun provideSavedPostDao(
        database: AppDatabase
    ): SavedPostDao {
        return database.savedPostDao()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(
        database: AppDatabase
    ): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    @Singleton
    fun provideDeletedPostDao(
        database: AppDatabase
    ): DeletedPostDao {
        return database.deletedPostDao()
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
        notificationService: FirebaseNotificationService,
        firebaseFirestore: FirebaseFirestore,
        notificationDao: NotificationDao,
        auth: FirebaseAuth
    ): NotificationRepository = NotificationRepository(notificationService, firebaseFirestore, notificationDao, auth)
}