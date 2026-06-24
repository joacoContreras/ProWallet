package com.undef.prowallet.data.ocr

import java.util.Locale

data class ParsedItem(val name: String, val price: Double)

data class ParsedTicket(
    val storeName: String? = null,
    val date: String? = null,
    val time: String? = null,
    val items: List<ParsedItem> = emptyList(),
    val total: Double? = null
) {
    val isEmpty: Boolean
        get() = storeName == null && date == null && time == null && items.isEmpty() && total == null
}

/**
 * Parsing heurístico por regex sobre el texto crudo de ML Kit. No es perfecto -
 * por eso siempre se exige confirmación/edición manual antes de aplicar al formulario.
 */
object TicketParser {

    private val SUMMARY_LINE_KEYWORDS = listOf(
        "total", "subtotal", "iva", "cambio", "efectivo", "tarjeta", "vuelto", "descuento"
    )

    private val PRICE_LINE_REGEX = Regex("""^(.+?)\s+\$?(\d{1,3}(?:[.,]\d{3})*[.,]\d{2})$""")
    private val AMOUNT_REGEX = Regex("""(\d{1,3}(?:[.,]\d{3})*[.,]\d{2})""")
    private val DATE_REGEX = Regex("""\b(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})\b""")
    private val TIME_REGEX = Regex("""\b(\d{1,2}):(\d{2})(?::\d{2})?\b""")

    fun parse(rawText: String): ParsedTicket {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        return ParsedTicket(
            storeName = extractStoreName(lines),
            date = extractDate(lines),
            time = extractTime(lines),
            items = extractItems(lines),
            total = extractTotal(lines)
        )
    }

    private fun isSummaryLine(line: String): Boolean {
        val lower = line.lowercase()
        return SUMMARY_LINE_KEYWORDS.any { lower.contains(it) }
    }

    private fun normalizeAmount(raw: String): Double {
        val cleaned = when {
            raw.contains(",") && raw.contains(".") -> raw.replace(".", "").replace(",", ".")
            raw.contains(",") -> raw.replace(",", ".")
            else -> raw
        }
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    private fun extractTotal(lines: List<String>): Double? {
        val candidates = lines.filter {
            val lower = it.lowercase()
            lower.contains("total") && !lower.contains("subtotal")
        }
        for (line in candidates.asReversed()) {
            val match = AMOUNT_REGEX.find(line) ?: continue
            return normalizeAmount(match.groupValues[1])
        }
        return null
    }

    private fun extractDate(lines: List<String>): String? {
        for (line in lines) {
            val match = DATE_REGEX.find(line) ?: continue
            val (d, m, y) = match.destructured
            val day = d.toIntOrNull() ?: continue
            val month = m.toIntOrNull() ?: continue
            if (day !in 1..31 || month !in 1..12) continue
            var year = y.toIntOrNull() ?: continue
            if (year < 100) year += 2000
            val yy = year % 100
            return String.format(Locale.US, "%02d/%02d/%02d", month, day, yy)
        }
        return null
    }

    private fun extractTime(lines: List<String>): String? {
        for (line in lines) {
            val match = TIME_REGEX.find(line) ?: continue
            val (h, min) = match.destructured
            val hour = h.toIntOrNull() ?: continue
            val minute = min.toIntOrNull() ?: continue
            if (hour !in 0..23 || minute !in 0..59) continue
            return String.format(Locale.US, "%02d:%02d", hour, minute)
        }
        return null
    }

    private fun extractStoreName(lines: List<String>): String? {
        return lines.take(5).firstOrNull { line ->
            line.length >= 3 &&
                line.any { it.isLetter() } &&
                !DATE_REGEX.containsMatchIn(line) &&
                !PRICE_LINE_REGEX.matches(line) &&
                !isSummaryLine(line)
        }
    }

    private fun extractItems(lines: List<String>): List<ParsedItem> {
        val items = mutableListOf<ParsedItem>()
        for (line in lines) {
            if (isSummaryLine(line)) continue
            val match = PRICE_LINE_REGEX.find(line) ?: continue
            val name = match.groupValues[1].trim()
            val price = normalizeAmount(match.groupValues[2])
            if (name.isBlank() || price <= 0.0) continue
            items.add(ParsedItem(name = name, price = price))
        }
        return items
    }
}
