package com.hfad.petlogger.entitylinkers

interface EntityLinker {
    suspend fun associateWith(firstEntityID: Long, secondEntityID: Long)
}