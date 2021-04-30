package com.datatoggle.server.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table("project_member")
data class DbProjectMember(
    @Id val id: Int = 0,
    val projectId: Int,
    val userAccountId: Int
)

interface ProjectMemberRepo : CoroutineCrudRepository<DbProjectMember, Int>
