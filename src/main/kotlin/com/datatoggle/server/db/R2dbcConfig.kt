package com.datatoggle.server.db

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

// cf. https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/cloud-sql/r2dbc/src/main/java/com/example/cloudsql/r2dbcsample/R2dbcSampleApplication.java
// and https://github.com/GoogleCloudPlatform/spring-cloud-gcp/issues/205
// and https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory/blob/main/r2dbc/postgres/src/test/java/com/google/cloud/sql/core/R2dbcPostgresIntegrationTests.java

@Configuration
@EnableR2dbcRepositories
class R2dbcConfig(
    @Value("\${datatoggle.db_connection}") private val dbConnection: String,
) : AbstractR2dbcConfiguration() {

    @Bean
    override fun connectionFactory(): ConnectionFactory {

        val connectionFactory: ConnectionFactory = ConnectionFactories.get(dbConnection)

        val configuration = ConnectionPoolConfiguration
            .builder(connectionFactory)
            .build()

        return ConnectionPool(configuration)
    }
}
