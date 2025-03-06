package com.hfad.petlogger.tags.domain.usecases

import android.util.Log
import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.data.Tag
import com.hfad.petlogger.tags.domain.TagRepository

class GetAllTagsUseCase(private val tagRepository: TagRepository): GetItemsUseCase<Tag> {
    private var _onLastPage = false
    override val onLastPage: Boolean
        get() = _onLastPage

    override suspend fun invoke(): List<Tag> {
        _onLastPage = true
        val allTags = tagRepository.getAllTags()
        Log.d("GetAllTags", "AllTags: $allTags")
        return allTags
    }

    override fun resetCurrentPoint() {
        _onLastPage = false
    }
}