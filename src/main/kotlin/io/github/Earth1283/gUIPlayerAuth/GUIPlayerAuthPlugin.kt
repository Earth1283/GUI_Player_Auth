package io.github.earth1283.guiplayerauth

import org.bukkit.plugin.java.JavaPlugin
import net.kyori.adventure.text.minimessage.MiniMessage
import io.github.earth1283.guiplayerauth.config.ConfigManager
import io.github.earth1283.guiplayerauth.config.AuthManager
import io.github.earth1283.guiplayerauth.database.Database
import io.github.earth1283.guiplayerauth.database.SQLiteManager
import io.github.earth1283.guiplayerauth.database.MySQLManager
import io.github.earth1283.guiplayerauth.listener.PlayerListener
import io.github.earth1283.guiplayerauth.listener.ConnectionListener
import io.github.earth1283.guiplayerauth.gui.GUIListener

class GUIPlayerAuthPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: GUIPlayerAuthPlugin
            private set
            
        val mm = MiniMessage.miniMessage()
    }

    lateinit var configManager: ConfigManager
        private set
    lateinit var database: Database
        private set
    lateinit var authManager: AuthManager
        private set
    lateinit var guiListener: GUIListener
        private set

    override fun onEnable() {
        instance = this
        
        // Save default config
        saveDefaultConfig()
        configManager = ConfigManager(this)
        
        // Initialize Database
        val dbType = config.getString("database.type", "SQLITE")
        database = if (dbType.equals("MYSQL", true)) {
            MySQLManager(this)
        } else {
            SQLiteManager(this)
        }
        database.connect()
        
        // Initialize Managers
        authManager = AuthManager(this, database)
        guiListener = GUIListener(this, authManager)
        
        // Register Listeners
        server.pluginManager.registerEvents(PlayerListener(this, authManager), this)
        server.pluginManager.registerEvents(ConnectionListener(this, authManager, guiListener), this)
        server.pluginManager.registerEvents(guiListener, this)
        
        logger.info("GUIPlayerAuth enabled!")
    }

    override fun onDisable() {
        database.close()
        logger.info("GUIPlayerAuth disabled!")
    }
}
