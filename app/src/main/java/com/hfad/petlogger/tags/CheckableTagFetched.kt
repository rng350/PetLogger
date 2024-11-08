package com.hfad.petlogger.tags

import androidx.lifecycle.MutableLiveData
import com.hfad.petlogger.common.CheckableItem
import com.hfad.petlogger.tags.Tag

data class CheckableTagFetched(
    val tagId: Long,
    val tagName: String,
    val isChecked: Boolean
) {
    fun toCheckableItem(): CheckableItem<Tag> {
        return CheckableItem(item= Tag(tagId, tagName), isChecked = MutableLiveData(isChecked))
    }
}