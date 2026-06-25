package com.undef.prowallet.data.remote

import com.undef.prowallet.data.AccountEntity
import com.undef.prowallet.data.CategoryEntity
import com.undef.prowallet.data.FixedExpenseEntity
import com.undef.prowallet.data.PurchaseEntity
import com.undef.prowallet.domain.Purchase
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SyncApiService {
    @POST("api/sync")
    suspend fun syncData(
        @Header("Authorization") token: String,
        @Body syncRequest: SyncRequest
    ): Response<SyncResponse>
}

data class SyncRequest(
    val lastSyncTime: Long,
    val userEmail: String,
    val modifiedPurchases: List<Purchase>,
    val modifiedCategories: List<CategoryEntity>,
    val modifiedAccounts: List<AccountEntity>,
    val modifiedFixedExpenses: List<FixedExpenseEntity>
)

data class SyncResponse(
    val serverTime: Long,
    val remotePurchases: List<Purchase>,
    val remoteCategories: List<CategoryEntity>,
    val remoteAccounts: List<AccountEntity>,
    val remoteFixedExpenses: List<FixedExpenseEntity>
)
