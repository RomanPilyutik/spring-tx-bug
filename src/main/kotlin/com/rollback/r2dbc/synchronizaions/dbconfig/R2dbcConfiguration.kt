package com.rollback.r2dbc.synchronizaions.dbconfig

import com.rollback.r2dbc.synchronizaions.dbconfig.converter.ByteArrayToUuidConverter
import com.rollback.r2dbc.synchronizaions.dbconfig.converter.UuidToByteArrayConverter
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.transaction.ReactiveTransactionManager

@Configuration
class R2dbcConfiguration(
    @Qualifier("connectionFactory")
    private val connectionFactory: ConnectionFactory
) : AbstractR2dbcConfiguration() {

    override fun connectionFactory(): ConnectionFactory {
        return connectionFactory
    }

    override fun getCustomConverters(): MutableList<Any> =
        mutableListOf(ByteArrayToUuidConverter, UuidToByteArrayConverter)

    @Bean
    fun transactionOperator(
        transactionManager: ReactiveTransactionManager
    ): TransactionOperator {
        return R2dbcTransactionOperator(transactionManager)
    }

    @Bean
    fun databaseService(
        r2dbcEntityTemplate: R2dbcEntityTemplate,
        mappingR2dbcConverter: MappingR2dbcConverter
    ): R2dbcDatabaseService {
        return R2dbcDatabaseService(r2dbcEntityTemplate, mappingR2dbcConverter)
    }
}