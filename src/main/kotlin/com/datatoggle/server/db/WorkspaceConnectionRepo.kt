package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table("workspace_connection")
data class DbWorkspaceConnection(
    @Id val id: Int = 0,
    val workspaceId: Int,
    val sourceId: Int,
    val destinationId: Int
)

interface WorkspaceConnectionRepo : CoroutineCrudRepository<DbWorkspaceConnection, Int>
