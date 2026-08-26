package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

/**
 * Utilitaires de formatage pour les nombres, statistiques et dates.
 */
object NumberFormatUtils {

    /**
     * Formate un grand nombre avec suffixe K, M, B (ex: 1 200 000 -> 1.2M).
     */
    fun formatCompactNumber(count: Long): String {
        if (count < 1000) return count.toString()
        val exp = (ln(count.toDouble()) / ln(1000.0)).toInt()
        val format = DecimalFormat("0.#")
        val value = count / 1000.0.pow(exp.toDouble())
        val suffix = when (exp) {
            1 -> " k"
            2 -> " M"
            3 -> " Md"
            4 -> " B"
            else -> ""
        }
        return "${format.format(value)}$suffix"
    }

    /**
     * Formate un nombre avec séparateurs de milliers (ex: 1 234 567).
     */
    fun formatFullNumber(count: Long): String {
        return DecimalFormat("#,###").format(count)
    }

    /**
     * Formate un timestamp Unix en date lisible (ex: "26 août 2026 à 14:30").
     */
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy 'à' HH:mm", Locale.FRENCH)
        return sdf.format(Date(timestamp))
    }

    /**
     * Formate une date ISO-8601 (ex: "2023-05-14T12:00:00Z") en date lisible.
     */
    fun formatIsoDate(isoString: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val date = parser.parse(isoString) ?: return isoString
            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
            formatter.format(date)
        } catch (e: Exception) {
            isoString.take(10)
        }
    }

    /**
     * Masque une clé API pour un affichage sécurisé (ex: "AIzaSy...8xZ2").
     */
    fun maskApiKey(key: String): String {
        if (key.length <= 8) return "••••••••"
        return "${key.take(6)}••••••••${key.takeLast(4)}"
    }
}
