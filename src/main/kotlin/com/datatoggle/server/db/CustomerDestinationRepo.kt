package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

@Table("customer_destination")
data class DbCustomerDestination(
    @Id val id: Int = 0,
    val uri: String,
    val enabled: Boolean,
    val customerId: Int,
    val destinationUri: String,
    val config: Map<String,Any>
)

interface CustomerDestinationRepo : CoroutineCrudRepository<DbCustomerDestination, Int>{

    suspend fun findByCustomerId(customerId: Int): List<DbCustomerDestination>
}
