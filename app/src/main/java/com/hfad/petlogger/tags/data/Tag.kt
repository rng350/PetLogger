package com.hfad.petlogger.tags.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hfad.petlogger.common.util.Constants.Companion.newTagPlaceholderId
import com.hfad.petlogger.common.util.Constants.Companion.tagIdField
import com.hfad.petlogger.common.util.Constants.Companion.tagNameField
import com.hfad.petlogger.common.util.Constants.Companion.tagTableHeader

@Entity(
    tableName = tagTableHeader,
    indices = [
        Index(value=[tagNameField], unique=true)
    ]
)
data class Tag(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = tagIdField, index=true)
    var tagId: Long = 0L,
    @ColumnInfo(name = tagNameField)
    var tagName: String
) {
    fun isNewTag(): Boolean {
        return tagId == newTagPlaceholderId
    }
}