package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.Instant

@Table("workspace_destination")
data class DbWorkspaceDestination(
    @Id val id: Int = 0,
    val enabled: Boolean,
    val workspaceId: Int,
    val destinationUri: String,
    val destinationSpecificConfig: io.r2dbc.postgresql.codec.Json,
    val lastModificationDatetime: Instant
)

interface WorkspaceDestinationRepo : CoroutineCrudRepository<DbWorkspaceDestination, Int>{

    suspend fun findByWorkspaceId(workspaceId: Int): List<DbWorkspaceDestination>

    suspend fun findByDestinationUriAndWorkspaceId(destinationUri: String, workspaceId: Int): DbWorkspaceDestination?
}
