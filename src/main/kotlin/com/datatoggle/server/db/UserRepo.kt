package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository


@Table
data class User(
    @Id val id: Int,
    val user_uuid: String,
    val user_uuid_for_customer: String,
    val last_connection: String,
    val customer_id: Int
)

interface UserRepo : CoroutineCrudRepository<User, Int>{



}
