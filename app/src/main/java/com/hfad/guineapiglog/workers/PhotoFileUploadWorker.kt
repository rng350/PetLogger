package com.hfad.guineapiglog.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class PhotoFileUploadWorker(context: Context, workerParams: WorkerParameters): Worker(context,
    workerParams
) {
    override fun doWork(): Result {
        return Result.success()
    }
}