package com.rollback.r2dbc.synchronizaions.repo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("test_table")
data class TestEntity(
    @Id
    val id: UUID,
    val payload: String,
    val createdAt: LocalDateTime
)

@Table("test_2_table")
data class Test2Entity(
    @Id
    val id: UUID,
    val payload: String,
    val createdAt: LocalDateTime
)
