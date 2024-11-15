package com.hfad.petlogger.tags.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.tags.TagDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSingleTagUseCase(private val tagDao: TagDao, private val tagId: Long): GetSingleItemUseCase<Tag> {
    override suspend fun invoke(): Tag = withContext(Dispatchers.IO) {
        tagDao.getTag(tagId)
    }
}