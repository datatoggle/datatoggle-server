package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*


@Table("tracked_session")
data class DbTrackedSession(
    @Id val id: Int = 0,
    val apiKey: UUID,
    val sampling: Int
)

interface TrackedSessionRepo : CoroutineCrudRepository<DbTrackedSession, Int> {



}
