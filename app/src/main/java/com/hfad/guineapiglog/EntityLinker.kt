package com.hfad.guineapiglog

interface EntityLinker {
    suspend fun associateWith(firstEntityID: Long, secondEntityID: Long)
}