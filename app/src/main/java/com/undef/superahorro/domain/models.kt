package com.undef.superahorro.domain

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String = ""
)

data class Product(
    val id: String,
    val code: String,
    val name: String,
    val price: Double
)

data class Purchase(
    val id: String,
    val storeName: String,
    val date: String,
    val totalAmount: Double,
    val category: String,
    val products: List<Product> = emptyList()
)
