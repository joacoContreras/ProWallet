package com.undef.prowallet.util

import kotlin.math.max

object StringSimilarity {
    /**
     * Calculates the Levenshtein similarity between two strings.
     * Returns a value between 0.0 (completely different) and 1.0 (identical).
     */
    fun calculateSimilarity(s1: String, s2: String): Double {
        val str1 = s1.trim().lowercase()
        val str2 = s2.trim().lowercase()
        if (str1 == str2) return 1.0
        val len1 = str1.length
        val len2 = str2.length
        if (len1 == 0 && len2 == 0) return 1.0
        if (len1 == 0 || len2 == 0) return 0.0

        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) {
            dp[i][0] = i
        }
        for (j in 0..len2) {
            dp[0][j] = j
        }

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }

        val maxLength = max(len1, len2)
        val distance = dp[len1][len2]
        return (maxLength - distance).toDouble() / maxLength
    }
}
