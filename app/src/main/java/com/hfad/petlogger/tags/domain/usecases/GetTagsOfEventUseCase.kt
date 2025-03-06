package com.hfad.petlogger.tags.domain.usecases

import com.hfad.petlogger.common.usecases.GetItemsUseCase
import com.hfad.petlogger.events.domain.EventRepository
import com.hfad.petlogger.tags.data.Tag

class GetTagsOfEventUseCase(private val eventRepository: EventRepository, private val eventId: Long):
    GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return eventRepository.getTagsOfEvent(eventId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}