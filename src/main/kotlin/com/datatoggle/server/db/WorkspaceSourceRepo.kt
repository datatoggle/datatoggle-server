package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

@Table("workspace_source")
data class DbWorkspaceSource(
    @Id val id: Int = 0,
    val uri: String,
    val name: String,
    val workspaceId: Int,
    val apiKey: UUID
)

interface WorkspaceSourceRepo : CoroutineCrudRepository<DbWorkspaceSource, Int>{

    suspend fun findByWorkspaceId(workspaceId: Int): List<DbWorkspaceSource>
}
