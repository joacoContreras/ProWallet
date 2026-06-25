package com.undef.prowallet.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.data.remote.RetrofitClient
import com.undef.prowallet.data.remote.SyncRequest
import com.undef.prowallet.util.SessionManager
import kotlinx.coroutines.flow.first

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = AppRepository(applicationContext)
        val sessionManager = SessionManager(applicationContext)

        val userEmail = sessionManager.email.first() ?: return Result.success()
        if (userEmail.isBlank()) return Result.success()

        val lastSync = sessionManager.lastSyncTime.first()

        try {
            // 1. Gather all dirty local changes
            val dirtyPurchases = repository.getDirtyPurchases(userEmail)

            val dirtyCategories = repository.getDirtyCategories(userEmail)
            val dirtyAccounts = repository.getDirtyAccounts(userEmail)
            val dirtyFixedExpenses = repository.getDirtyFixedExpenses(userEmail)

            // 2. Perform the Sync API call using Retrofit
            val response = RetrofitClient.syncApiService.syncData(
                token = "Bearer mock_token_key", // In production, replace with real JWT from auth session
                syncRequest = SyncRequest(
                    lastSyncTime = lastSync,
                    userEmail = userEmail,
                    modifiedPurchases = dirtyPurchases,
                    modifiedCategories = dirtyCategories,
                    modifiedAccounts = dirtyAccounts,
                    modifiedFixedExpenses = dirtyFixedExpenses
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val syncResponse = response.body()!!

                // 3. Apply remote changes downloaded from the server
                repository.applySyncResponse(
                    purchases = syncResponse.remotePurchases,
                    categories = syncResponse.remoteCategories,
                    accounts = syncResponse.remoteAccounts,
                    fixedExpenses = syncResponse.remoteFixedExpenses
                )

                // 4. Clear dirty flags of the local items we successfully pushed
                repository.clearDirtyFlags(
                    purchases = dirtyPurchases,
                    categories = dirtyCategories,
                    accounts = dirtyAccounts,
                    fixedExpenses = dirtyFixedExpenses
                )

                // 5. Save the server timestamp as the new lastSyncTime
                sessionManager.saveLastSyncTime(syncResponse.serverTime)

                return Result.success()
            } else {
                return Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    companion object {
        fun enqueuePeriodicWork(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val periodicSyncRequest = androidx.work.PeriodicWorkRequestBuilder<SyncWorker>(
                1, java.util.concurrent.TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "periodic_cloud_sync_work",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )
        }
    }
}
