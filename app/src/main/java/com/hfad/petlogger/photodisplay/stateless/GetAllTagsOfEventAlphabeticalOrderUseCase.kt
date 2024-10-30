package com.hfad.petlogger.photodisplay.stateless

import com.hfad.petlogger.entities.Tag
import com.hfad.petlogger.repositories.EventRepository

class GetAllTagsOfEventAlphabeticalOrderUseCase(private val eventRepository: EventRepository, private val eventId: Long): GetItemsUseCase<Tag> {
    override val onLastPage: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun invoke(): List<Tag> {
        return eventRepository.getAllTagsOfEventAlphabeticalOrder(eventId)
    }

    override fun resetCurrentPoint() {
        TODO("Not yet implemented")
    }
}