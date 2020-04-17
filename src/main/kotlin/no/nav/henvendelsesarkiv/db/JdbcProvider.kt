package no.nav.henvendelsesarkiv.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.henvendelsesarkiv.ApplicationProperties
import no.nav.henvendelsesarkiv.SingletonHolder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import javax.sql.DataSource

class ConnectionPool private constructor(application: ApplicationProperties) {
    var dataSource: HikariDataSource
    init {
        val config = HikariConfig()
        config.jdbcUrl = application.dbUrl
        config.username = application.dbUsername
        config.password = application.dbPassword
        config.maximumPoolSize = 100
        config.minimumIdle = 2
        config.connectionTimeout = 1000
        dataSource = HikariDataSource(config)
    }

    companion object : SingletonHolder<ConnectionPool, ApplicationProperties>(::ConnectionPool)
}

private val hikariDatasource = ConnectionPool.getInstance(ApplicationProperties()).dataSource

class CoroutineAwareJdbcTemplate(val dataSource: DataSource) {
    private val dataSourceTransactionManager = DataSourceTransactionManager(dataSource)
    private val transactionTemplate = TransactionTemplate(dataSourceTransactionManager)
    internal val jdbcTemplate = JdbcTemplate(dataSource)

    suspend fun <T> use(block: JdbcTemplate.() -> T): T =
            withContext(Dispatchers.IO) {
                block(jdbcTemplate)
            }

    suspend fun <T> inTransaction(block: JdbcTemplate.() -> T): T =
            withContext(Dispatchers.IO) {
                transactionTemplate.execute {
                    block(jdbcTemplate)
                }!!
            }
}

val coroutineAwareJdbcTemplate = CoroutineAwareJdbcTemplate(hikariDatasource)
