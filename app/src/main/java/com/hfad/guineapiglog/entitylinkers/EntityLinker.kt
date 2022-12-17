package com.hfad.guineapiglog.entitylinkers

interface EntityLinker {
    suspend fun associateWith(firstEntityID: Long, secondEntityID: Long)
}