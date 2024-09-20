package com.example.urvoices.data.repository


import com.example.urvoices.data.AudioManager
import com.example.urvoices.data.service.FirebaseAudioService
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val manager: AudioManager,
    private val firebaseAudioService: FirebaseAudioService
){

}