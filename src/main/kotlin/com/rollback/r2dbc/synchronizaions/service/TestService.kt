package com.rollback.r2dbc.synchronizaions.service

import com.rollback.r2dbc.synchronizaions.dbconfig.TransactionOperator
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

    fun findEntities(id: UUID): Mono<TestEntity> {
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
}