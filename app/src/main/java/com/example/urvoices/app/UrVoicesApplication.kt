package com.example.urvoices.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class UrVoicesApplication : Application(), Configuration.Provider {
	@Inject
	lateinit var workerFactory: WorkerFactory
	override fun getWorkManagerConfiguration(): Configuration {
		return Configuration.Builder()
			.setWorkerFactory(workerFactory)
			.build()
	}
}