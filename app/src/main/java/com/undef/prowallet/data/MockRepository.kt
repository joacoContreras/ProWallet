package com.undef.prowallet.data

import com.undef.prowallet.domain.Product
import com.undef.prowallet.domain.Purchase
import com.undef.prowallet.domain.User

object MockRepository {

    val currentUser = User(
        id = "usr_001",
        fullName = "Martin González",
        email = "martin@example.com"
    )

    val mockProducts = listOf(
        Product("p001", "10492", "Leche entera 1L", 1.20),
        Product("p002", "20381", "Pan de molde", 2.50),
        Product("p003", "30192", "Yogur natural x4", 3.80),
        Product("p004", "40293", "Aceite de oliva 500ml", 6.90),
        Product("p005", "50183", "Detergente 750ml", 2.10)
    )

    val mockPurchases = listOf(
        Purchase(
            id = "pur_001",
            storeName = "Mercadona",
            date = "Today, 14:30",
            totalAmount = 84.20,
            category = "Groceries",
            products = listOf(mockProducts[0], mockProducts[1], mockProducts[2])
        ),
        Purchase(
            id = "pur_002",
            storeName = "Repsol",
            date = "Yesterday",
            totalAmount = 45.00,
            category = "Transport",
            products = listOf(Product("p006", "60001", "Gasolina 95", 45.00))
        ),
        Purchase(
            id = "pur_003",
            storeName = "La Trattoria",
            date = "Oct 13",
            totalAmount = 112.50,
            category = "Dining",
            products = listOf(
                Product("p007", "70001", "Pasta carbonara x2", 28.00),
                Product("p008", "70002", "Vino tinto", 18.00),
                Product("p009", "70003", "Tiramisú x2", 14.00)
            )
        ),
        Purchase(
            id = "pur_004",
            storeName = "Whole Foods Market",
            date = "Oct 10",
            totalAmount = 320.00,
            category = "Groceries",
            products = listOf(mockProducts[0], mockProducts[3], mockProducts[4])
        ),
        Purchase(
            id = "pur_005",
            storeName = "Starbucks",
            date = "Oct 8",
            totalAmount = 145.00,
            category = "Coffee",
            products = listOf(
                Product("p010", "80001", "Latte grande x3", 18.00),
                Product("p011", "80002", "Croissant x2", 8.00)
            )
        ),
        Purchase(
            id = "pur_006",
            storeName = "Shell Station",
            date = "Oct 5",
            totalAmount = 85.20,
            category = "Transport",
            products = listOf(Product("p012", "90001", "Combustible", 85.20))
        )
    )

    val monthlyBudget = 2500.0
    val totalMonthlySpend = 1842.50
    val remaining = 657.50
    val percentageVsLastMonth = -12

    val topStores = listOf(
        Triple("Whole Foods Market", 420.50, 0.85f),
        Triple("Starbucks", 145.00, 0.29f),
        Triple("Shell Station", 85.20, 0.17f)
    )

    val monthlyTrend = listOf(
        Pair("May", 980f),
        Pair("Jun", 1200f),
        Pair("Jul", 1050f),
        Pair("Aug", 1380f),
        Pair("Sep", 1100f),
        Pair("Oct", 1240f)
    )
}
