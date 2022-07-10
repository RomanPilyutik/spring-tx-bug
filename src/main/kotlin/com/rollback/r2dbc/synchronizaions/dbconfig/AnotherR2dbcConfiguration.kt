package com.rollback.r2dbc.synchronizaions.dbconfig

import com.rollback.r2dbc.synchronizaions.dbconfig.converter.ByteArrayToUuidConverter
import com.rollback.r2dbc.synchronizaions.dbconfig.converter.UuidToByteArrayConverter
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactory
import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryBuilder
import org.springframework.boot.autoconfigure.r2dbc.EmbeddedDatabaseConnection
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.convert.CustomConversions
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext
import org.springframework.data.r2dbc.support.R2dbcExceptionSubclassTranslator
import org.springframework.data.r2dbc.support.R2dbcExceptionTranslator
import org.springframework.data.relational.core.mapping.NamingStrategy
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.util.Assert
import java.util.*
import javax.sql.DataSource
import kotlin.collections.ArrayList

@Configuration
class AnotherR2dbcConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.another.r2dbc")
    fun anotherR2dbcProperties(): R2dbcProperties = R2dbcProperties()

    @Bean
    fun anotherConnectionFactory(
        anotherR2dbcProperties: R2dbcProperties,
        resourceLoader: ResourceLoader
    ): ConnectionFactory {
        val connectionFactory = ConnectionFactoryBuilder.of(anotherR2dbcProperties) {
            EmbeddedDatabaseConnection.get(resourceLoader.classLoader)
        }.build()

        val pool: R2dbcProperties.Pool = anotherR2dbcProperties.pool
        val builder = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxSize(pool.maxSize).initialSize(pool.initialSize).maxIdleTime(pool.maxIdleTime)
        return ConnectionPool(builder.build())
    }

    @Bean
    fun anotherDatabaseClient(
        anotherConnectionFactory: ConnectionFactory,
        anotherReactiveDataAccessStrategy: ReactiveDataAccessStrategy,
        anotherExceptionTranslator: R2dbcExceptionTranslator
    ): DatabaseClient {
        return DatabaseClient.builder()
            .connectionFactory(anotherConnectionFactory)
            .dataAccessStrategy(anotherReactiveDataAccessStrategy)
            .exceptionTranslator(anotherExceptionTranslator).build()
    }

    @Bean
    fun anotherR2dbcMappingContext(
        namingStrategy: Optional<NamingStrategy>,
        anotherR2dbcCustomConversions: R2dbcCustomConversions
    ): R2dbcMappingContext {
        Assert.notNull(namingStrategy, "NamingStrategy must not be null!")
        val context = R2dbcMappingContext(namingStrategy.orElse(NamingStrategy.INSTANCE)!!)
        context.setSimpleTypeHolder(anotherR2dbcCustomConversions.simpleTypeHolder)
        return context
    }

    @Bean
    fun anotherReactiveDataAccessStrategy(
        anotherConnectionFactory: ConnectionFactory,
        anotherR2dbcMappingContext: R2dbcMappingContext,
        anotherR2dbcCustomConversions: R2dbcCustomConversions
    ): ReactiveDataAccessStrategy {
        val converter = MappingR2dbcConverter(anotherR2dbcMappingContext, anotherR2dbcCustomConversions)
        return DefaultReactiveDataAccessStrategy(DialectResolver.getDialect(anotherConnectionFactory), converter)
    }

    @Bean
    fun anotherR2dbcCustomConversions(
        anotherConnectionFactory: ConnectionFactory
    ): R2dbcCustomConversions =
        R2dbcCustomConversions(getStoreConversions(anotherConnectionFactory), getCustomConverters())

    @Bean
    fun anotherExceptionTranslator(): R2dbcExceptionTranslator =
        R2dbcExceptionSubclassTranslator()

    @Bean
    fun anotherTransactionManager(anotherConnectionFactory: ConnectionFactory): ReactiveTransactionManager =
        R2dbcTransactionManager(anotherConnectionFactory)

    @Bean
    fun anotherTransactionOperator(
        anotherTransactionManager: ReactiveTransactionManager
    ): TransactionOperator {
        return R2dbcTransactionOperator(anotherTransactionManager)
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.another.liquibase")
    fun anotherLiquibaseProperties(): LiquibaseProperties = LiquibaseProperties()

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    fun anotherDatasourceProperties(): DataSourceProperties = DataSourceProperties()

    @Bean
    fun anotherLiquibase(
        anotherLiquibaseProperties: LiquibaseProperties,
        anotherDatasourceProperties: DataSourceProperties
    ): SpringLiquibase? {
        val liquibase: SpringLiquibase = DataSourceClosingSpringLiquibase()
        liquibase.dataSource = createDataSource(anotherLiquibaseProperties, anotherDatasourceProperties)
        liquibase.changeLog = anotherLiquibaseProperties.changeLog
        liquibase.isClearCheckSums = anotherLiquibaseProperties.isClearChecksums
        liquibase.contexts = anotherLiquibaseProperties.contexts
        liquibase.defaultSchema = anotherLiquibaseProperties.defaultSchema
        liquibase.liquibaseSchema = anotherLiquibaseProperties.liquibaseSchema
        liquibase.liquibaseTablespace = anotherLiquibaseProperties.liquibaseTablespace
        liquibase.databaseChangeLogTable = anotherLiquibaseProperties.databaseChangeLogTable
        liquibase.databaseChangeLogLockTable = anotherLiquibaseProperties.databaseChangeLogLockTable
        liquibase.isDropFirst = anotherLiquibaseProperties.isDropFirst
        liquibase.setShouldRun(anotherLiquibaseProperties.isEnabled)
        liquibase.labels = anotherLiquibaseProperties.labels
        liquibase.setChangeLogParameters(anotherLiquibaseProperties.parameters)
        liquibase.setRollbackFile(anotherLiquibaseProperties.rollbackFile)
        liquibase.isTestRollbackOnUpdate = anotherLiquibaseProperties.isTestRollbackOnUpdate
        liquibase.tag = anotherLiquibaseProperties.tag
        return liquibase
    }

    @Bean
    fun anotherDatabaseService(
        anotherDatabaseClient: DatabaseClient
    ): R2dbcDatabaseService {
        return R2dbcDatabaseService(
            databaseClient = anotherDatabaseClient
        )
    }

    private fun getCustomConverters(): MutableList<Any> =
        mutableListOf(ByteArrayToUuidConverter, UuidToByteArrayConverter)

    private fun getStoreConversions(
        connectionFactory: ConnectionFactory
    ): CustomConversions.StoreConversions {
        val dialect = DialectResolver.getDialect(connectionFactory)
        val converters: MutableList<Any> = ArrayList(dialect.converters)
        converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS)
        return CustomConversions.StoreConversions.of(dialect.simpleTypeHolder, converters)
    }

    private fun createDataSource(
        liquibaseProperties: LiquibaseProperties,
        datasourceProperties: DataSourceProperties
    ): DataSource {
        val url = liquibaseProperties.url
        val user = liquibaseProperties.user
        val password = liquibaseProperties.password
        return DataSourceBuilder.create()
            .type(determineDataSourceType())
            .driverClassName(datasourceProperties.driverClassName)
            .url(url)
            .username(user)
            .password(password)
            .build()!!
    }

    private fun determineDataSourceType(): Class<out DataSource> {
        val type = DataSourceBuilder.findType(null)
        return type ?: SimpleDriverDataSource::class.java
    }
}