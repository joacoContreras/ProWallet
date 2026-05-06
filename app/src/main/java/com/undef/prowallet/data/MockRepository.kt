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
        Product("p001", "10492", "Leche entera 1L", "Milk 1L", 1.20),
        Product("p002", "20381", "Pan de molde", "Bread", 2.50),
        Product("p003", "30192", "Yogur natural x4", "Yogurt pack", 3.80),
        Product("p004", "40293", "Aceite de oliva 500ml", "Olive Oil", 6.90),
        Product("p005", "50183", "Detergente 750ml", "Detergent", 2.10)
    )

    val mockPurchases = listOf(
        Purchase(
            id = "pur_001",
            storeName = "Mercadona",
            date = "05/05/26",
            time = "14:30",
            totalAmount = 84.20,
            category = "Groceries",
            products = listOf(mockProducts[0], mockProducts[1], mockProducts[2])
        ),
        Purchase(
            id = "pur_002",
            storeName = "Repsol",
            date = "05/04/26",
            time = "10:15",
            totalAmount = 45.00,
            category = "Transport",
            products = listOf(Product("p006", "60001", "Gasolina 95", "Fuel", 45.00))
        ),
        Purchase(
            id = "pur_003",
            storeName = "La Trattoria",
            date = "10/13/25",
            time = "20:00",
            totalAmount = 112.50,
            category = "Dining",
            products = listOf(
                Product("p007", "70001", "Pasta carbonara x2", "Dinner", 28.00),
                Product("p008", "70002", "Vino tinto", "Wine", 18.00),
                Product("p009", "70003", "Tiramisú x2", "Dessert", 14.00)
            )
        ),
        Purchase(
            id = "pur_004",
            storeName = "Whole Foods Market",
            date = "10/10/25",
            time = "11:30",
            totalAmount = 320.00,
            category = "Groceries",
            products = listOf(mockProducts[0], mockProducts[3], mockProducts[4])
        ),
        Purchase(
            id = "pur_005",
            storeName = "Starbucks",
            date = "10/08/25",
            time = "08:45",
            totalAmount = 145.00,
            category = "Coffee",
            products = listOf(
                Product("p010", "80001", "Latte grande x3", "Coffee", 18.00),
                Product("p011", "80002", "Croissant x2", "Pastry", 8.00)
            )
        ),
        Purchase(
            id = "pur_006",
            storeName = "Shell Station",
            date = "10/05/25",
            time = "16:00",
            totalAmount = 85.20,
            category = "Transport",
            products = listOf(Product("p012", "90001", "Combustible", "Fuel", 85.20))
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
