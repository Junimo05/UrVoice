package com.example.urvoices.utils.Worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.urvoices.viewmodel.UploadWorker
import javax.inject.Inject

class CustomWorkerFactory @Inject constructor(
	private val uploadWorkerFactory: UploadWorkerFactory
) : WorkerFactory() {

	override fun createWorker(
		appContext: Context,
		workerClassName: String,
		workerParameters: WorkerParameters
	): ListenableWorker? {
		return when (workerClassName) {
			UploadWorker::class.java.name ->
				uploadWorkerFactory.create(appContext, workerParameters)
			else -> null
		}
	}
}