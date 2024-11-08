package com.hfad.petlogger.common.associationentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.Weight

@Entity(
    tableName = "weight_tag_table",
    primaryKeys = ["weight_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = Weight::class,
            parentColumns = ["weight_id"],
            childColumns = ["weight_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["tag_id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class WeightTag(
    @ColumnInfo(name = "weight_id", index = true)
    var weightId: Long,
    @ColumnInfo(name = "tag_id", index = true)
    var tagId: Long
)
