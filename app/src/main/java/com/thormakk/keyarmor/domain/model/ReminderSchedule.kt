package com.thormakk.keyarmor.domain.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

sealed class ReminderSchedule {

    abstract val id: String

    data class Punctual(
        override val id: String = UUID.randomUUID().toString(),
        val triggerAtMs: Long
    ) : ReminderSchedule()

    data class Weekly(
        override val id: String = UUID.randomUUID().toString(),
        val daysOfWeek: Set<Int>,
        val hour: Int,
        val minute: Int
    ) : ReminderSchedule()

    data class Monthly(
        override val id: String = UUID.randomUUID().toString(),
        val dayOfMonth: Int,
        val hour: Int,
        val minute: Int
    ) : ReminderSchedule()

    data class Yearly(
        override val id: String = UUID.randomUUID().toString(),
        val month: Int,
        val dayOfMonth: Int,
        val hour: Int,
        val minute: Int
    ) : ReminderSchedule()

    data class Interval(
        override val id: String = UUID.randomUUID().toString(),
        val startMs: Long,
        val intervalDays: Int,
        val hour: Int,
        val minute: Int
    ) : ReminderSchedule()

    data class BeforeDeadline(
        override val id: String = UUID.randomUUID().toString(),
        val daysBeforeCount: Int,
        val hour: Int,
        val minute: Int
    ) : ReminderSchedule()

    fun toJson(): JSONObject = when (this) {
        is Punctual -> JSONObject().apply {
            put("type",        "punctual")
            put("id",          id)
            put("triggerAtMs", triggerAtMs)
        }
        is Weekly -> JSONObject().apply {
            put("type",       "weekly")
            put("id",         id)
            put("daysOfWeek", JSONArray(daysOfWeek.sorted()))
            put("hour",       hour)
            put("minute",     minute)
        }
        is Monthly -> JSONObject().apply {
            put("type",       "monthly")
            put("id",         id)
            put("dayOfMonth", dayOfMonth)
            put("hour",       hour)
            put("minute",     minute)
        }
        is Yearly -> JSONObject().apply {
            put("type",       "yearly")
            put("id",         id)
            put("month",      month)
            put("dayOfMonth", dayOfMonth)
            put("hour",       hour)
            put("minute",     minute)
        }
        is Interval -> JSONObject().apply {
            put("type",         "interval")
            put("id",           id)
            put("startMs",      startMs)
            put("intervalDays", intervalDays)
            put("hour",         hour)
            put("minute",       minute)
        }
        is BeforeDeadline -> JSONObject().apply {
            put("type",            "before_deadline")
            put("id",              id)
            put("daysBeforeCount", daysBeforeCount)
            put("hour",            hour)
            put("minute",          minute)
        }
    }

    companion object {
        fun fromJson(obj: JSONObject): ReminderSchedule? = runCatching {
            val id   = obj.optString("id").ifBlank { UUID.randomUUID().toString() }
            when (obj.getString("type")) {
                "punctual" -> Punctual(
                    id          = id,
                    triggerAtMs = obj.getLong("triggerAtMs")
                )
                "weekly" -> {
                    val arr  = obj.getJSONArray("daysOfWeek")
                    val days = (0 until arr.length()).map { arr.getInt(it) }.toSet()
                    Weekly(
                        id         = id,
                        daysOfWeek = days,
                        hour       = obj.getInt("hour"),
                        minute     = obj.getInt("minute")
                    )
                }
                "monthly" -> Monthly(
                    id         = id,
                    dayOfMonth = obj.getInt("dayOfMonth"),
                    hour       = obj.getInt("hour"),
                    minute     = obj.getInt("minute")
                )
                "yearly" -> Yearly(
                    id         = id,
                    month      = obj.getInt("month"),
                    dayOfMonth = obj.getInt("dayOfMonth"),
                    hour       = obj.getInt("hour"),
                    minute     = obj.getInt("minute")
                )
                "interval" -> Interval(
                    id           = id,
                    startMs      = obj.getLong("startMs"),
                    intervalDays = obj.getInt("intervalDays"),
                    hour         = obj.getInt("hour"),
                    minute       = obj.getInt("minute")
                )
                "before_deadline" -> BeforeDeadline(
                    id               = id,
                    daysBeforeCount  = obj.getInt("daysBeforeCount"),
                    hour             = obj.getInt("hour"),
                    minute           = obj.getInt("minute")
                )
                else -> null
            }
        }.getOrNull()

        fun listToJson(schedules: List<ReminderSchedule>): String =
            JSONArray(schedules.map { it.toJson() }).toString()

        fun listFromJson(json: String?): List<ReminderSchedule> {
            if (json.isNullOrBlank()) return emptyList()
            return runCatching {
                val arr = JSONArray(json)
                (0 until arr.length()).mapNotNull { fromJson(arr.getJSONObject(it)) }
            }.getOrElse { emptyList() }
        }
    }
}
