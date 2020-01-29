package no.nav.henvendelsesarkiv.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.henvendelsesarkiv.ApplicationProperties
import no.nav.henvendelsesarkiv.SingletonHolder

class ConnectionPool private constructor(application: ApplicationProperties) {
    var dataSource: HikariDataSource
    init {
        val config = HikariConfig()
        config.jdbcUrl = application.dbUrl
        config.username = application.dbUsername
        config.password = application.dbPassword
        config.maximumPoolSize = 10
        config.minimumIdle = 2
        config.connectionTimeout = 1000
        dataSource = HikariDataSource(config)
    }

    companion object : SingletonHolder<ConnectionPool, ApplicationProperties>(::ConnectionPool)
}

val hikariDatasource = ConnectionPool.getInstance(ApplicationProperties()).dataSource