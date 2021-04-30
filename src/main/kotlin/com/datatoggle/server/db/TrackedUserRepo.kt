package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant


@Table("tracked_user")
data class TrackedUser(
    @Id val id: Int,
    val userUuid: String,
    val lastConnection: Instant,
    val projectId: Int
)

interface TrackedUserRepo : CoroutineCrudRepository<TrackedUser, Int>
