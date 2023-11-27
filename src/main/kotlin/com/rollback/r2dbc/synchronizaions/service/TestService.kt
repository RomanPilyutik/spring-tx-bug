package com.rollback.r2dbc.synchronizaions.service

import com.rollback.r2dbc.synchronizaions.dbconfig.TransactionOperator
import com.rollback.r2dbc.synchronizaions.repo.Test2Entity
import com.rollback.r2dbc.synchronizaions.repo.TestRepository2
import com.rollback.r2dbc.synchronizaions.repo.TestEntity
import com.rollback.r2dbc.synchronizaions.repo.TestRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@Component
class TestService(
    private val testRepository: TestRepository,
    private val testRepository2: TestRepository2,
    private val transactionOperator: TransactionOperator,
) {

    fun findEntitiesWithOneTransactionManager(id: UUID): Mono<TestEntity> {
        return testRepository2.findByIdForUpdate(id)
            .flatMap {
                Mono.zip(
                    testRepository.findById(UUID.randomUUID()),
                    testRepository2.insert(Test2Entity(UUID.randomUUID(), "TEST", LocalDateTime.now()))
                        .`as`(transactionOperator::inNewTransaction)
                )
            }
            .`as`(transactionOperator::inTransaction)
            .map { it.t1 }
    }
}