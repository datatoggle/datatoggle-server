package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table("project_destination")
data class DbProjectDestination(
    @Id val id: Int = 0,
    val enabled: Boolean,
    val projectId: Int,
    val destinationUri: String,
    val destinationSpecificConfig: io.r2dbc.postgresql.codec.Json
)

interface ProjectDestinationRepo : CoroutineCrudRepository<DbProjectDestination, Int>{

    suspend fun findByProjectId(projectId: Int): List<DbProjectDestination>

    suspend fun findByDestinationUriAndProjectId(destinationUri: String, projectId: Int): DbProjectDestination?
}
