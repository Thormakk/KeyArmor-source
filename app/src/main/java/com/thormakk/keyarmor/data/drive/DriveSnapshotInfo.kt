package com.thormakk.keyarmor.data.drive

import com.thormakk.keyarmor.domain.model.SnapshotTrigger

data class DriveSnapshotInfo(
    val driveFileId: String,
    val snapshotId:  String,
    val trigger:     SnapshotTrigger,
    val timestamp:   Long
) {
    companion object {
        private const val PREFIX = "tm"
        private const val EXT    = ".snap"

        fun buildFilename(snapshotId: String, trigger: SnapshotTrigger, timestamp: Long): String =
            "$PREFIX.$snapshotId.${trigger.name}.$timestamp$EXT"

        fun parseFilename(filename: String): Triple<String, SnapshotTrigger, Long>? {
            if (!filename.startsWith("$PREFIX.") || !filename.endsWith(EXT)) return null
            val body  = filename.removePrefix("$PREFIX.").removeSuffix(EXT)
            val firstDot  = body.indexOf('.')
            val lastDot   = body.lastIndexOf('.')
            if (firstDot == -1 || lastDot == firstDot) return null
            val id        = body.substring(0, firstDot)
            val triggerStr = body.substring(firstDot + 1, lastDot)
            val tsStr      = body.substring(lastDot + 1)
            val trigger    = SnapshotTrigger.entries.firstOrNull { it.name == triggerStr } ?: return null
            val timestamp  = tsStr.toLongOrNull() ?: return null
            return Triple(id, trigger, timestamp)
        }
    }
}
