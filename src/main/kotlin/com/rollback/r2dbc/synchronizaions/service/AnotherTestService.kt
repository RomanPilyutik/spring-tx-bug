package com.rollback.r2dbc.synchronizaions.service

import com.rollback.r2dbc.synchronizaions.dbconfig.TransactionOperator
import com.rollback.r2dbc.synchronizaions.repo.AnotherTestEntity
import com.rollback.r2dbc.synchronizaions.repo.AnotherTestRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Component
class AnotherTestService(
    private val anotherTestRepository: AnotherTestRepository,
    private val anotherTransactionOperator: TransactionOperator
) {

    fun doSomething(): Mono<AnotherTestEntity> {
        return AnotherTestEntity(UUID.randomUUID(), "ANOTHER PAYLOAD", LocalDateTime.now()).toMono()
            .flatMap { anotherTestRepository.insert(it) }
            .`as`(anotherTransactionOperator::inTransaction)
    }
}