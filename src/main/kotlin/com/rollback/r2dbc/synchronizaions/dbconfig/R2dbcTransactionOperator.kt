package com.rollback.r2dbc.synchronizaions.dbconfig

import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class R2dbcTransactionOperator(
    private val transactionManager: ReactiveTransactionManager
): TransactionOperator {
    private val defaultOperator: TransactionalOperator = TransactionalOperator.create(transactionManager)

    override fun <T> inTransaction(flux: Flux<T>): Flux<T> {
        return defaultOperator.transactional(flux)
    }

    override fun <T> inTransaction(mono: Mono<T>): Mono<T> {
        return defaultOperator.transactional(mono)
    }
}

interface TransactionOperator {

    fun <T> inTransaction(flux: Flux<T>): Flux<T>

    fun <T> inTransaction(mono: Mono<T>): Mono<T>

}