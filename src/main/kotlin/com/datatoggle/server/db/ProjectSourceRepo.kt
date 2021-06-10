package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

@Table("project_source")
data class DbProjectSource(
    @Id val id: Int = 0,
    val uri: String,
    val name: String,
    val projectId: Int,
    val apiKey: UUID
)

interface ProjectSourceRepo : CoroutineCrudRepository<DbProjectSource, Int>{

    suspend fun findByProjectId(projectId: Int): List<DbProjectSource>
}
