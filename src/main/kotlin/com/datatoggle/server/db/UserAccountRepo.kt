package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table("user_account")
data class DbUserAccount(
    @Id val id: Int = 0,
    val uri: String,
    val firebaseAuthUid: String
)

interface UserAccountRepo : CoroutineCrudRepository<DbUserAccount, Int> {
    suspend fun findByFirebaseAuthUid(firebaseAuthUid: String): DbUserAccount?
}
