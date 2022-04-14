package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table("event")
data class DbEvent(
    @Id val id: Int = 0,
    val userAccountUri: String,
    val eventName: String,
    val eventData: io.r2dbc.postgresql.codec.Json
)

interface EventRepo : CoroutineCrudRepository<DbEvent, Int>{
}
