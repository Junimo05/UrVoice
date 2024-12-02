package com.example.urvoices.di

import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.repository.NotificationRepository
import com.example.urvoices.data.service.FirebaseAudioService
import com.example.urvoices.data.service.FirebaseBlockService
import com.example.urvoices.data.service.FirebaseNotificationService
import com.example.urvoices.data.service.FirebasePostService
import com.example.urvoices.data.service.FirebaseUserService
import com.example.urvoices.utils.MessagingService
import com.example.urvoices.utils.SharedPreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Firebase {
    @Provides
    @Singleton
    fun provideFireAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideStorageReference(): StorageReference {
        return FirebaseStorage.getInstance().getReference()
    }

    @Provides
    @Singleton
    fun provideFirebaseAudioService(
        audioManager: AudioManager,
        storage: StorageReference,
        firebaseFirestore: FirebaseFirestore
    ): FirebaseAudioService {
        return FirebaseAudioService(audioManager, storage, firebaseFirestore)
    }

    @Provides
    @Singleton
    fun provideFirebaseUserService(
        storage: StorageReference,
        firebaseFirestore: FirebaseFirestore,
        auth: FirebaseAuth,
        sharedPrefs: SharedPreferencesHelper
    ): FirebaseUserService {
        return FirebaseUserService(storage, firebaseFirestore, auth, sharedPrefs)
    }

    @Provides
    @Singleton
    fun provideFirebasePostService(
        audioManager: AudioManager,
        firestore: FirebaseFirestore,
        storage: StorageReference,
        auth: FirebaseAuth,
    ): FirebasePostService {
        return FirebasePostService(audioManager, auth, firestore, storage)
    }

    @Provides
    @Singleton
    fun provideFirebaseNotificationService(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FirebaseNotificationService {
        return FirebaseNotificationService(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideFirebaseBlockService(
        firebaseFirestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FirebaseBlockService {
        return FirebaseBlockService(firebaseFirestore, auth)
    }


}