package com.rollback.r2dbc.synchronizaions.repo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("another_test_table")
data class AnotherTestEntity(
    @Id
    val id: UUID,
    val payload: String,
    val createdAt: LocalDateTime
)
