package com.hfad.petlogger.common.util

import java.time.OffsetDateTime
import java.time.ZoneOffset

class Constants {
    companion object {
        const val eventTableHeader = "event_table"
        const val eventIdField = "event_id"
        const val eventTitleField = "event_title"
        const val eventDetailsField = "event_details"

        const val noteTableHeader = "note_table"
        const val noteIdField = "note_id"
        const val noteTitleField = "note_title"
        const val noteDetailsField = "note_details"

        const val tagTableHeader = "tag_table"
        const val tagIdField = "tag_id"
        const val tagNameField = "tag_name"

        const val noteTagTableHeader = "note_tag_table"

        const val newTagPlaceholderId: Long = -1

        const val defaultNullIdForNavigation: Long = -1L

        val OFFSET_DATE_TIME_MAX_ALLOWED: OffsetDateTime = OffsetDateTime.of(9999,12,31,1,1,1,1, ZoneOffset.UTC)
    }
}