package com.rollback.r2dbc.synchronizaions.dbconfig

import com.rollback.r2dbc.synchronizaions.dbconfig.converter.ByteArrayToUuidConverter
import com.rollback.r2dbc.synchronizaions.dbconfig.converter.UuidToByteArrayConverter
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactory
import liquibase.integration.spring.SpringLiquibase
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
import org.springframework.context.annotation.Primary
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

@Configuration
class R2dbcConfiguration {

    @Bean
    fun connectionFactory(
        r2dbcProperties: R2dbcProperties,
        resourceLoader: ResourceLoader
    ): ConnectionFactory {
        val connectionFactory = ConnectionFactoryBuilder.of(r2dbcProperties) {
            EmbeddedDatabaseConnection.get(resourceLoader.classLoader)
        }.build()

        val pool: R2dbcProperties.Pool = r2dbcProperties.pool
        val builder = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxSize(pool.maxSize).initialSize(pool.initialSize).maxIdleTime(pool.maxIdleTime)
        return ConnectionPool(builder.build())
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.r2dbc")
    fun r2dbcProperties(): R2dbcProperties = R2dbcProperties()

    @Bean
    fun databaseClient(
        connectionFactory: ConnectionFactory,
        reactiveDataAccessStrategy: ReactiveDataAccessStrategy,
        exceptionTranslator: R2dbcExceptionTranslator
    ): DatabaseClient {
        return DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .dataAccessStrategy(reactiveDataAccessStrategy)
            .exceptionTranslator(exceptionTranslator).build()
    }

    @Bean
    fun r2dbcMappingContext(
        namingStrategy: Optional<NamingStrategy>,
        r2dbcCustomConversions: R2dbcCustomConversions
    ): R2dbcMappingContext {
        Assert.notNull(namingStrategy, "NamingStrategy must not be null!")
        val context = R2dbcMappingContext(namingStrategy.orElse(NamingStrategy.INSTANCE)!!)
        context.setSimpleTypeHolder(r2dbcCustomConversions.simpleTypeHolder)
        return context
    }

    @Bean
    fun reactiveDataAccessStrategy(
        connectionFactory: ConnectionFactory,
        r2dbcMappingContext: R2dbcMappingContext,
        r2dbcCustomConversions: R2dbcCustomConversions
    ): ReactiveDataAccessStrategy {
        val converter = MappingR2dbcConverter(r2dbcMappingContext, r2dbcCustomConversions)
        return DefaultReactiveDataAccessStrategy(DialectResolver.getDialect(connectionFactory), converter)
    }

    @Bean
    fun r2dbcCustomConversions(
        connectionFactory: ConnectionFactory
    ): R2dbcCustomConversions =
        R2dbcCustomConversions(getStoreConversions(connectionFactory), getCustomConverters())

    @Bean
    fun exceptionTranslator(): R2dbcExceptionTranslator =
        R2dbcExceptionSubclassTranslator()

    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager =
        R2dbcTransactionManager(connectionFactory)

    @Bean
    fun transactionOperator(transactionManager: ReactiveTransactionManager): TransactionOperator {
        return R2dbcTransactionOperator(transactionManager)
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.liquibase")
    fun liquibaseProperties(): LiquibaseProperties = LiquibaseProperties()

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    fun datasourceProperties(): DataSourceProperties = DataSourceProperties()

    @Bean
    fun liquibase(
        liquibaseProperties: LiquibaseProperties,
        datasourceProperties: DataSourceProperties
    ): SpringLiquibase? {
        val liquibase: SpringLiquibase = DataSourceClosingSpringLiquibase()
        liquibase.dataSource = createDataSource(liquibaseProperties, datasourceProperties)
        liquibase.changeLog = liquibaseProperties.changeLog
        liquibase.isClearCheckSums = liquibaseProperties.isClearChecksums
        liquibase.contexts = liquibaseProperties.contexts
        liquibase.defaultSchema = liquibaseProperties.defaultSchema
        liquibase.liquibaseSchema = liquibaseProperties.liquibaseSchema
        liquibase.liquibaseTablespace = liquibaseProperties.liquibaseTablespace
        liquibase.databaseChangeLogTable = liquibaseProperties.databaseChangeLogTable
        liquibase.databaseChangeLogLockTable = liquibaseProperties.databaseChangeLogLockTable
        liquibase.isDropFirst = liquibaseProperties.isDropFirst
        liquibase.setShouldRun(liquibaseProperties.isEnabled)
        liquibase.labels = liquibaseProperties.labels
        liquibase.setChangeLogParameters(liquibaseProperties.parameters)
        liquibase.setRollbackFile(liquibaseProperties.rollbackFile)
        liquibase.isTestRollbackOnUpdate = liquibaseProperties.isTestRollbackOnUpdate
        liquibase.tag = liquibaseProperties.tag
        return liquibase
    }

    @Bean
    fun databaseService(
        databaseClient: DatabaseClient
    ): R2dbcDatabaseService {
        return R2dbcDatabaseService(
            databaseClient = databaseClient
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