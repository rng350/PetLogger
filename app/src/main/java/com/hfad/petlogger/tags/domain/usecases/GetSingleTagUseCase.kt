package com.hfad.petlogger.tags.domain.usecases

import com.hfad.petlogger.common.usecases.GetSingleItemUseCase
import com.hfad.petlogger.tags.data.Tag
import com.hfad.petlogger.tags.data.TagDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSingleTagUseCase(private val tagDao: TagDao, private val tagId: Long): GetSingleItemUseCase<Tag> {
    override suspend fun invoke(): Tag = withContext(Dispatchers.IO) {
        tagDao.getTag(tagId)
    }
}