package com.example.urvoices.di

import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.service.FirebaseAudioService
import com.example.urvoices.data.service.FirebaseBlockService
import com.example.urvoices.data.service.FirebaseNotificationService
import com.example.urvoices.data.service.FirebasePostService
import com.example.urvoices.data.service.FirebaseUserService
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
        auth: FirebaseAuth
    ): FirebaseUserService {
        return FirebaseUserService(storage, firebaseFirestore, auth)
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
        firestore: FirebaseFirestore
    ): FirebaseNotificationService {
        return FirebaseNotificationService(firestore)
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