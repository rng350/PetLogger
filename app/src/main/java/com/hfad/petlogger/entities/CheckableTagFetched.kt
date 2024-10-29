package com.hfad.petlogger.entities

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.CheckableItem

data class CheckableTagFetched(
    val tagId: Long,
    val tagName: String,
    val isChecked: Boolean
) {
    fun toCheckableItem(): CheckableItem<Tag> {
        return CheckableItem(item=Tag(tagId, tagName), isChecked = MutableLiveData(isChecked))
    }
}