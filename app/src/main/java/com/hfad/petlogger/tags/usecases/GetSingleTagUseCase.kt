package com.hfad.petlogger.tags.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.tags.TagDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSingleTagUseCase(private val tagDao: TagDao, private val tagId: Long): GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> = withContext(Dispatchers.IO) {
        listOf(tagDao.getTag(tagId))
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}