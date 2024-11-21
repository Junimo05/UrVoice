package com.example.urvoices.utils.Worker

import android.content.Context
import androidx.work.WorkerParameters
import com.example.urvoices.viewmodel.UploadWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory

@AssistedFactory
interface UploadWorkerFactory {
	fun create(
		@Assisted context: Context,
		@Assisted params: WorkerParameters
	): UploadWorker
}