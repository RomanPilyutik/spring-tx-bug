package com.rollback.r2dbc.synchronizaions.dbconfig

import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Order
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.data.relational.core.query.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.rollback.r2dbc.synchronizaions.dbconfig.exception.EntityIncorrectResultSizeException
import com.rollback.r2dbc.synchronizaions.dbconfig.exception.EntityNotFoundException

class R2dbcDatabaseService(
    val databaseClient: DatabaseClient
) {

    final inline fun <reified T : Any> create(entityId: UUID, entity: T, clazz: Class<T>? = null): Mono<T> {
        val clazzType = clazz ?: T::class.java
        return databaseClient
            .insert()
            .into(clazzType)
            .using(entity)
            .then()
            .thenReturn(entity)
    }

    fun <T> findById(id: UUID, clazz: Class<T>): Mono<T> =
        databaseClient
            .select()
            .from(clazz)
            .matching(Criteria.where("id").`is`(id))
            .fetch()
            .one()

    fun executeSqlWithParams(sql: String, params: Map<String, Any>): Mono<Void> {
        return databaseClient.execute(sql)
            .let {
                params.forEach { (name, value) ->
                    it.bind(name, value)
                }
                it
            }
            .then()
    }

    fun <T> executeSqlGetFirstWithParams(sql: String, params: Map<String, Any>, clazz: Class<T>): Mono<T> {
        return databaseClient.execute(sql)
            .`as`(clazz)
            .let {
                var spec = it
                params.forEach { (name, value) ->
                    spec = spec.bind(name, value)
                }
                spec
            }.fetch().first()
    }
}
