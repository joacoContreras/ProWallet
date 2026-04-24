package com.undef.ProWallet.domain.model

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: Double,
    val date: String,
    val isExpense: Boolean
)
