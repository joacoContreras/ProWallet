package com.undef.prowallet.util

import com.undef.prowallet.domain.Purchase
import java.util.Calendar

fun Purchase.isInMonth(year: Int, month: Int): Boolean {
    if (timestampMs == 0L) return false
    val cal = Calendar.getInstance().apply { timeInMillis = timestampMs }
    return cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
}

fun Purchase.isCurrentMonth(): Boolean {
    val now = Calendar.getInstance()
    return isInMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
}
