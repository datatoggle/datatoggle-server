package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

@Table("customer")
data class DbCustomer(
    @Id val id: Int = 0,
    val uri: String,
    val userApiKey: UUID,
    val firebaseAuthUid: String
)

interface CustomerRepo : CoroutineCrudRepository<DbCustomer, Int>{

    suspend fun findByFirebaseAuthUid(firebaseAuthUid: String): DbCustomer?

}
