package com.example.urvoices.di

import android.content.Context
import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.db.AppDatabase
import com.example.urvoices.data.db.Dao.PostDao
import com.example.urvoices.data.repository.PostRepository
import com.example.urvoices.data.service.FirebasePostService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun providePostDao(
        database: AppDatabase
    ): PostDao = database.postDao()

    @Provides
    @Singleton
    fun providePostRepo(
        manager: AudioManager,
        postDao: PostDao,
        firebasePostService: FirebasePostService
    ): PostRepository = PostRepository(manager, firebasePostService, postDao)
}