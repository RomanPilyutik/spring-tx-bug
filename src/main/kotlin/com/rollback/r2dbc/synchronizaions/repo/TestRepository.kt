package com.rollback.r2dbc.synchronizaions.repo

import com.rollback.r2dbc.synchronizaions.dbconfig.R2dbcDatabaseService
import com.rollback.r2dbc.synchronizaions.dbconfig.exception.EntityNotFoundException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class TestRepository(
    private val databaseService: R2dbcDatabaseService,
) {

    fun findByIdForUpdate(id: UUID): Mono<TestEntity> {
        return databaseService.executeSqlGetFirstWithParams(
            """select * from test_table where id=:id FOR UPDATE""",
            mapOf("id" to id), TestEntity::class.java
        )
            .switchIfEmpty(Mono.error(EntityNotFoundException("")))
    }

    fun findById(id: UUID): Mono<TestEntity> {
        return databaseService.findById(id, TestEntity::class.java)
            .switchIfEmpty(Mono.error(EntityNotFoundException("")))
    }

    fun insert(testEntity: TestEntity): Mono<TestEntity> {
        return Mono.just(testEntity)
            .flatMap { databaseService.create(it.id, it, TestEntity::class.java) }
    }
}