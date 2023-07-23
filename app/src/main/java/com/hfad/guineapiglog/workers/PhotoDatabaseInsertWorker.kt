package com.hfad.guineapiglog.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hfad.guineapiglog.PetLoggerDatabase

// call after PhotoFileUploadWorker
class PhotoDatabaseInsertWorker(appContext: Context,
                                params: WorkerParameters
): CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        //PetLoggerDatabase.getInstance(applicationContext).photoDao.insert(photo)
        return Result.success()
    }
}