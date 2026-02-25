package io.github.earth1283.guiplayerauth.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.earth1283.guiplayerauth.GUIPlayerAuthPlugin
import java.io.File
import java.sql.Connection
import java.util.UUID

class SQLiteManager(private val plugin: GUIPlayerAuthPlugin) : Database {
    private var dataSource: HikariDataSource? = null

    override fun connect() {
        val dbFile = File(plugin.dataFolder, plugin.config.getString("database.sqlite.file") ?: "database.db")
        if (!dbFile.exists()) {
            dbFile.parentFile.mkdirs()
            dbFile.createNewFile()
        }

        val config = HikariConfig()
        config.jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"
        config.driverClassName = "org.sqlite.JDBC"
        config.poolName = "GUIPlayerAuth-SQLitePool"
        config.maximumPoolSize = 10
        config.minimumIdle = 2
        config.idleTimeout = 30000
        config.maxLifetime = 60000
        config.connectionTimeout = 10000

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
                    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
