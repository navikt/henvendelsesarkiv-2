package no.nav.henvendelsesarkiv

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.jdbc.core.JdbcTemplate

class ConnectionPool private constructor(fasit: FasitProperties) {
    var dataSource: HikariDataSource
    init {
        val config = HikariConfig()
        config.jdbcUrl = fasit.dbUrl
        config.username = fasit.dbUsername
        config.password = fasit.dbPassword
        config.maximumPoolSize = 10
        config.minimumIdle = 2
        config.connectionTimeout = 1000
        dataSource = HikariDataSource(config)
    }

    companion object : SingletonHolder<ConnectionPool, FasitProperties>(::ConnectionPool)
}

val jdbcTemplate = JdbcTemplate(ConnectionPool.getInstance(FasitProperties()).dataSource)