package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table("workspace_member")
data class DbWorkspaceMember(
    @Id val id: Int = 0,
    val workspaceId: Int,
    val userAccountId: Int
)

interface WorkspaceMemberRepo : CoroutineCrudRepository<DbWorkspaceMember, Int>
