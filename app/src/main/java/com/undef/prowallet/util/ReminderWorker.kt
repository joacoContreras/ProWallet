package com.undef.prowallet.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.util.isCurrentMonth
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sessionManager = SessionManager(applicationContext)

        if (!sessionManager.notificationsEnabled.first()) return Result.success()

        val repository = AppRepository(applicationContext)
        val purchases = repository.purchasesFlow.first()
        val budget = sessionManager.monthlyBudget.first()

        val now = System.currentTimeMillis()
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
        val weekSpend = purchases
            .filter { it.timestampMs > 0 && now - it.timestampMs <= sevenDaysMs }
            .sumOf { it.totalAmount }
        val monthSpend = purchases
            .filter { it.isCurrentMonth() }
            .sumOf { it.totalAmount }

        NotificationHelper.showWeeklyReminder(applicationContext, weekSpend, monthSpend, budget)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "weekly_spending_reminder"

        fun scheduleWeekly(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(7, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
