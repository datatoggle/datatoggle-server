package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

@Table("project")
data class DbProject(
    @Id val id: Int = 0,
    val uri: String,
    val name: String
)

interface ProjectRepo : CoroutineCrudRepository<DbProject, Int>{

    @Query("SELECT p.* FROM project p " +
            "JOIN project_member pm ON pm.project_id = p.id " +
            "WHERE pm.user_account_id = :userAccountId")
    suspend fun findByUserAccountId(userAccountId: Int): List<DbProject>

    @Query("SELECT p.* FROM project p " +
            "JOIN project_member pm ON pm.project_id = p.id " +
            "WHERE pm.user_account_id = :userAccountId " +
            "AND p.uri = :projectUri"
    )
    suspend fun findByUserAccountIdAndProjectUri(userAccountId: Int, projectUri: String): DbProject
}
