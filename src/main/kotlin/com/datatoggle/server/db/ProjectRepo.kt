package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

@Table("project")
data class DbProject(
    @Id val id: Int = 0,
    val uri: String,
    val name: String,
    val apiKey: UUID,
    val customerId: Int
)

interface ProjectRepo : CoroutineCrudRepository<DbProject, Int>{

    suspend fun findByCustomerId(customerId: Int): List<DbProject>
    suspend fun findByUriAndCustomerId(uri: String, customerId: Int): DbProject
}
