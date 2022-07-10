package com.rollback.r2dbc.synchronizaions.repo

import com.rollback.r2dbc.synchronizaions.dbconfig.R2dbcDatabaseService
import com.rollback.r2dbc.synchronizaions.dbconfig.exception.EntityNotFoundException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class AnotherTestRepository(
    private val anotherDatabaseService: R2dbcDatabaseService,
) {

    fun findById(id: UUID): Mono<AnotherTestEntity> {
        return anotherDatabaseService.findById(id, AnotherTestEntity::class.java)
            .switchIfEmpty(Mono.error(EntityNotFoundException("")))
    }

    fun insert(anotherTestEntity: AnotherTestEntity): Mono<AnotherTestEntity> {
        return Mono.just(anotherTestEntity)
            .flatMap { anotherDatabaseService.create(it.id, it, AnotherTestEntity::class.java) }
    }
}