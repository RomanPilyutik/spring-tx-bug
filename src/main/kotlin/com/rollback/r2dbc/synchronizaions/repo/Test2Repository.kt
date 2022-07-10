package com.rollback.r2dbc.synchronizaions.repo

import com.rollback.r2dbc.synchronizaions.dbconfig.R2dbcDatabaseService
import com.rollback.r2dbc.synchronizaions.dbconfig.exception.EntityNotFoundException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class Test2Repository(
    private val databaseService: R2dbcDatabaseService,
) {

    fun findByIdForUpdate(id: UUID): Mono<Test2Entity> {
        return databaseService.executeSqlGetFirstWithParams(
            """select * from test_2_table where id = :entityId FOR UPDATE""",
            mapOf("entityId" to id),
            Test2Entity::class.java
        )
            .switchIfEmpty(Mono.error(EntityNotFoundException("")))
    }

    fun findById(id: UUID): Mono<Test2Entity> {
        return databaseService.findById(id, Test2Entity::class.java)
            .switchIfEmpty(Mono.error(EntityNotFoundException("")))
    }

    fun insert(testEntity: Test2Entity): Mono<Test2Entity> {
        return Mono.just(testEntity)
            .flatMap { databaseService.create(it.id, it, Test2Entity::class.java) }
    }
}