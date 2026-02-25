package io.github.earth1283.guiplayerauth.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.earth1283.guiplayerauth.GUIPlayerAuthPlugin
import java.sql.Connection
import java.util.UUID

class MySQLManager(private val plugin: GUIPlayerAuthPlugin) : Database {
    private var dataSource: HikariDataSource? = null

    override fun connect() {
        val host = plugin.config.getString("database.mysql.host") ?: "localhost"
        val port = plugin.config.getInt("database.mysql.port", 3306)
        val schema = plugin.config.getString("database.mysql.database") ?: "playerauth"
        val username = plugin.config.getString("database.mysql.username") ?: "root"
        val password = plugin.config.getString("database.mysql.password") ?: ""
        val useSsl = plugin.config.getBoolean("database.mysql.use-ssl", false)

        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://$host:$port/$schema"
        config.username = username
        config.password = password
        config.addDataSourceProperty("useSSL", useSsl)

        // Recommended HikariCP settings for MySQL
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.addDataSourceProperty("useServerPrepStmts", "true")
        config.addDataSourceProperty("useLocalSessionState", "true")
        config.addDataSourceProperty("rewriteBatchedStatements", "true")
        config.addDataSourceProperty("cacheResultSetMetadata", "true")
        config.addDataSourceProperty("cacheServerConfiguration", "true")
        config.addDataSourceProperty("elideSetAutoCommits", "true")
        config.addDataSourceProperty("maintainTimeStats", "false")

        dataSource = HikariDataSource(config)
        createTables()
    }

    private fun createTables() {
        getConnection().use { conn ->
            conn.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    pin_hash VARCHAR(255) NOT NULL,
                    last_ip VARCHAR(45) NOT NULL,
                    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                );
                """.trimIndent()
            ).execute()
        }
    }

    override fun close() {
        dataSource?.close()
    }

    private fun getConnection(): Connection {
        return dataSource?.connection ?: throw IllegalStateException("Database is not connected!")
    }

    override fun isRegistered(uuid: UUID): Boolean {
        getConnection().use { conn ->
            val stmt = conn.prepareStatement("SELECT 1 FROM players WHERE uuid = ?;")
            stmt.setString(1, uuid.toString())
            val rs = stmt.executeQuery()
            return rs.next()
        }
    }

    override fun getPlayerHash(uuid: UUID): String? {
        getConnection().use { conn ->
            val stmt = conn.prepareStatement("SELECT pin_hash FROM players WHERE uuid = ?;")
            stmt.setString(1, uuid.toString())
            val rs = stmt.executeQuery()
            if (rs.next()) {
                return rs.getString("pin_hash")
            }
            return null
        }
    }

    override fun registerPlayer(uuid: UUID, hash: String, ip: String) {
        getConnection().use { conn ->
            val stmt = conn.prepareStatement(
                "INSERT INTO players (uuid, pin_hash, last_ip) VALUES (?, ?, ?);"
            )
            stmt.setString(1, uuid.toString())
            stmt.setString(2, hash)
            stmt.setString(3, ip)
            stmt.executeUpdate()
        }
    }

    override fun updateLogin(uuid: UUID, ip: String) {
        getConnection().use { conn ->
            val stmt = conn.prepareStatement(
                "UPDATE players SET last_ip = ?, last_login = CURRENT_TIMESTAMP WHERE uuid = ?;"
            )
            stmt.setString(1, ip)
            stmt.setString(2, uuid.toString())
            stmt.executeUpdate()
        }
    }
}
