package com.example.urvoices.di

import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.service.FirebaseAudioService
import com.example.urvoices.data.service.FirebaseNotificationService
import com.example.urvoices.data.service.FirebasePostService
import com.example.urvoices.data.service.FirebaseUserService
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
    fun proviceFireAuth(): FirebaseAuth {
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
        audioManager: AudioManager,
        storage: StorageReference,
        firebaseFirestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FirebaseUserService {
        return FirebaseUserService(audioManager, storage, firebaseFirestore, auth)
    }

    @Provides
    @Singleton
    fun provideFirebasePostService(
        audioManager: AudioManager,
        firestore: FirebaseFirestore,
        storage: StorageReference,
        firebaseAudioService: FirebaseAudioService,
        sharedPref: SharedPreferencesHelper
    ): FirebasePostService {
        return FirebasePostService(audioManager, firestore, storage)
    }

    @Provides
    @Singleton
    fun provideFirebaseNotificationService(
        storage: StorageReference,
        firestore: FirebaseFirestore
    ): FirebaseNotificationService {
        return FirebaseNotificationService(firestore)
    }
}