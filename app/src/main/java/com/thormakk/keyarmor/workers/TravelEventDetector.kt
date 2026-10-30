package com.thormakk.keyarmor.workers

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat

object TravelEventDetector {

    data class TripCandidate(
        val eventKey: String,
        val startMillis: Long
    )

    private const val DAY_MS = 24L * 60 * 60 * 1000L

    private val KEYWORDS = listOf(
        "vuelo", "aeropuerto", "embarque", "check-in", "check in", "itinerario",
        "flight", "airport", "boarding pass", "departure", "layover", "itinerary",
        "voo", "aeroporto", "embarque",
        "vol ", "aéroport",
        "flug", "flughafen",
        "hotel", "booking.com", "airbnb",
        "aerolineas argentinas", "aerolíneas argentinas", "latam", "avianca",
        "copa airlines", "american airlines", "united airlines", "delta air",
        "iberia", "air europa", "lufthansa", "klm", "gol linhas", "azul linhas"
    )

    fun findUpcomingTrip(context: Context, nowMillis: Long, windowDays: Int): TripCandidate? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) return null

        val end = nowMillis + windowDays * DAY_MS
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
            ContentUris.appendId(this, nowMillis)
            ContentUris.appendId(this, end)
        }.build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION
        )

        return runCatching {
            context.contentResolver.query(
                uri, projection, null, null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )?.use { cursor ->
                val idxEventId = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val idxBegin   = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val idxTitle   = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val idxDesc    = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)
                val idxLoc     = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)

                while (cursor.moveToNext()) {
                    val text = listOfNotNull(
                        cursor.getString(idxTitle),
                        cursor.getString(idxDesc),
                        cursor.getString(idxLoc)
                    ).joinToString(" ")

                    if (looksLikeTrip(text)) {
                        val eventId = cursor.getLong(idxEventId)
                        val begin   = cursor.getLong(idxBegin)
                        return@use TripCandidate(eventKey = "$eventId:$begin", startMillis = begin)
                    }
                }
                null
            }
        }.getOrNull()
    }

    private fun looksLikeTrip(text: String): Boolean {
        if (text.isBlank()) return false
        val lower = text.lowercase()
        return KEYWORDS.any { lower.contains(it) }
    }
}
