package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant


@Table
data class User(
    @Id val id: Int,
    val user_uuid: String,
    val last_connection: Instant,
    val project_id: Int
)

interface UserRepo : CoroutineCrudRepository<User, Int>
