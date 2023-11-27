package com.rollback.r2dbc.synchronizaions.dbconfig

import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.support.DefaultTransactionDefinition
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class R2dbcTransactionOperator(
    transactionManager: ReactiveTransactionManager
): TransactionOperator {
    private val defaultOperator: TransactionalOperator = TransactionalOperator.create(transactionManager)
    private val requiresNewTransaction: TransactionalOperator = TransactionalOperator.create(
        transactionManager,
        DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
    )

    override fun <T> inTransaction(flux: Flux<T>): Flux<T> {
        return defaultOperator.transactional(flux)
    }

    override fun <T> inTransaction(mono: Mono<T>): Mono<T> {
        return defaultOperator.transactional(mono)
    }

    override fun <T> inNewTransaction(flux: Flux<T>): Flux<T> {
        return requiresNewTransaction.transactional(flux)
    }

    override fun <T> inNewTransaction(mono: Mono<T>): Mono<T> {
        return requiresNewTransaction.transactional(mono)
    }
}

interface TransactionOperator {

    fun <T> inTransaction(flux: Flux<T>): Flux<T>

    fun <T> inTransaction(mono: Mono<T>): Mono<T>

    fun <T> inNewTransaction(flux: Flux<T>): Flux<T>

    fun <T> inNewTransaction(mono: Mono<T>): Mono<T>
}