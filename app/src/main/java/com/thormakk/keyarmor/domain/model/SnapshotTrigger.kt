package com.thormakk.keyarmor.domain.model

enum class SnapshotTrigger {
    WEEKLY,
    MANUAL,
    PRE_BULK_DELETE,
    PRE_MASTER_CHANGE,
    PRE_RESTORE,
    PRE_IMPORT_CSV,
    PRE_IMPORT_JSON,
    PRE_IMPORT_KAB;

    val esPreImport: Boolean get() = this == PRE_IMPORT_CSV || this == PRE_IMPORT_JSON || this == PRE_IMPORT_KAB

}
