package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

@Table("project_connection")
data class DbProjectConnection(
    @Id val id: Int = 0,
    val projectId: Int,
    val sourceId: Int,
    val destinationId: Int
)

interface ProjectConnectionRepo : CoroutineCrudRepository<DbProjectConnection, Int>{

}
