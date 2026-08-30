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

    /**
     * Convertit une durée ISO-8601 (ex: "PT1H23M45S", "PT15M30S", "PT45S") en secondes.
     */
    fun parseIsoDurationToSeconds(durationIso: String?): Long {
        if (durationIso.isNullOrBlank()) return 0L
        return try {
            val regex = Regex("P(?:(\\d+)D)?T?(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val match = regex.matchEntire(durationIso.trim())
            if (match != null) {
                val days = match.groupValues[1].toLongOrNull() ?: 0L
                val hours = match.groupValues[2].toLongOrNull() ?: 0L
                val minutes = match.groupValues[3].toLongOrNull() ?: 0L
                val seconds = match.groupValues[4].toLongOrNull() ?: 0L
                days * 86400 + hours * 3600 + minutes * 60 + seconds
            } else {
                java.time.Duration.parse(durationIso).seconds
            }
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Formate un nombre de secondes en texte lisible (ex: "1h 23m 45s", "15m 30s", "45s").
     */
    fun formatDurationSeconds(totalSeconds: Long): String {
        if (totalSeconds <= 0) return "0s"
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format(Locale.US, "%dh %02dm %02ds", hours, minutes, seconds)
            minutes > 0 -> String.format(Locale.US, "%dm %02ds", minutes, seconds)
            else -> String.format(Locale.US, "%ds", seconds)
        }
    }

    /**
     * Formate directement une durée ISO-8601 de YouTube.
     */
    fun formatIsoDuration(durationIso: String?): String {
        val seconds = parseIsoDurationToSeconds(durationIso)
        return if (seconds > 0) formatDurationSeconds(seconds) else "Durée n/d"
    }
}
