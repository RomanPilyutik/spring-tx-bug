package com.rollback.r2dbc.synchronizaions.service

import com.rollback.r2dbc.synchronizaions.dbconfig.TransactionOperator
import com.rollback.r2dbc.synchronizaions.repo.Test2Entity
import com.rollback.r2dbc.synchronizaions.repo.Test2Repository
import com.rollback.r2dbc.synchronizaions.repo.TestEntity
import com.rollback.r2dbc.synchronizaions.repo.TestRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Component
class TestService(
    private val testRepository: TestRepository,
    private val test2Repository: Test2Repository,
    private val transactionOperator: TransactionOperator,
    private val anotherTestService: AnotherTestService
) {

    fun findEntitiesWithTwoTransactionManagers(id: UUID): Mono<TestEntity> {
        return test2Repository.findByIdForUpdate(id)
            .flatMap {
                Mono.zip(
                    testRepository.findById(UUID.randomUUID()),
                    anotherTestService.doSomething()
                )
            }
            .`as`(transactionOperator::inTransaction)
            .map { it.t1 }
    }

    fun findEntitiesWithOneTransactionManager(id: UUID): Mono<TestEntity> {
        return test2Repository.findByIdForUpdate(id)
            .flatMap {
                Mono.zip(
                    testRepository.findById(UUID.randomUUID()),
                    test2Repository.insert(Test2Entity(UUID.randomUUID(), "TEST", LocalDateTime.now()))
                        .`as`(transactionOperator::inNewTransaction)
                )
            }
            .`as`(transactionOperator::inTransaction)
            .map { it.t1 }
    }
}