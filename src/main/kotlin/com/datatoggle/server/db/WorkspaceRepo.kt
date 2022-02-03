package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table("workspace")
data class DbWorkspace(
    @Id val id: Int = 0,
    val uri: String,
    val name: String
)

interface WorkspaceRepo : CoroutineCrudRepository<DbWorkspace, Int>{

    @Query("SELECT p.* FROM workspace p " +
            "JOIN workspace_member pm ON pm.workspace_id = p.id " +
            "WHERE pm.user_account_id = :userAccountId")
    suspend fun findByUserAccountId(userAccountId: Int): List<DbWorkspace>

    @Query("SELECT p.* FROM workspace p " +
            "JOIN workspace_member pm ON pm.workspace_id = p.id " +
            "WHERE pm.user_account_id = :userAccountId " +
            "AND p.uri = :workspaceUri"
    )
    suspend fun findByUserAccountIdAndWorkspaceUri(userAccountId: Int, workspaceUri: String): DbWorkspace
}
