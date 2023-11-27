package com.rollback.r2dbc.synchronizaions.dbconfig

import java.util.UUID
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.util.ClassTypeInformation
import org.springframework.data.util.Pair
import reactor.core.publisher.Mono

class R2dbcDatabaseService(
    val r2dbcEntityTemplate: R2dbcEntityTemplate,
    private val mappingR2dbcConverter: MappingR2dbcConverter
) {

    inline fun <reified T : Any> create(entity: T, clazz: Class<T>? = null): Mono<T> {
        val clazzType = clazz ?: T::class.java
        return r2dbcEntityTemplate
            .insert(clazzType)
            .using(entity)
            .then()
            .thenReturn(entity)
    }

    fun <T> findById(id: UUID, clazz: Class<T>): Mono<T> =
        r2dbcEntityTemplate.selectOne(Query.query(Criteria.where("id").`is`(id)), clazz)

    fun <T> executeSqlGetFirstWithParams(sql: String, params: Map<String, Any>, clazz: Class<T>): Mono<T> {
        return r2dbcEntityTemplate
            .databaseClient
            .sql(sql)
            .let {
                var spec = it
                params.forEach { (name, value) ->
                    spec = bindValueToSpec(value, spec, name)
                }
                spec
            }
            .map { row, rowMetadata -> mappingR2dbcConverter.read(clazz, row, rowMetadata) }
            .first()
    }

    private fun bindValueToSpec(
        value: Any?,
        spec: org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec,
        name: String
    ): org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec {
        val convertedValue = convertValue(value)
        return if (convertedValue == null) {
            spec.bindNull(name, String::class.java)
        } else {
            spec.bind(name, convertedValue)
        }
    }

    private fun convertValue(value: Any?): Any? {
        return value?.let {
            when {
                it is Pair<*, *> -> Pair.of(convertValue(it.first)!!, convertValue(it.second)!!)
                it is Iterable<*> -> it.map { item -> convertValue(item) }.takeIf { l -> l.isNotEmpty() }
                it.javaClass.isArray -> it
                else -> mappingR2dbcConverter.writeValue(it, ClassTypeInformation.OBJECT)!!
            }
        }
    }

}
