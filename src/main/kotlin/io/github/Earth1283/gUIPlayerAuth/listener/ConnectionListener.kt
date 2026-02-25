package io.github.earth1283.guiplayerauth.listener

import io.github.earth1283.guiplayerauth.GUIPlayerAuthPlugin
import io.github.earth1283.guiplayerauth.config.AuthManager
import io.github.earth1283.guiplayerauth.gui.GUIListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.Location
import java.util.UUID

class ConnectionListener(
    private val plugin: GUIPlayerAuthPlugin,
    private val authManager: AuthManager,
    private val guiListener: GUIListener
) : Listener {

    private val tempSpawnConfig = plugin.configManager.getTempSpawn()
    private val originLocations = mutableMapOf<UUID, Location>()

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        val ip = player.address?.address?.hostAddress ?: "unknown"
        val isRegistered = authManager.database.isRegistered(player.uniqueId)
        
        // Always block them initially until proven authenticated
        authManager.addUnauthenticated(player, isRegistered)
        
        // Check Session logic - only for registered users
        if (isRegistered) {
            // Simplified session logic: Instead of a complex table just for sessions,
            // we will query if their last login IP matches and timeframe is valid
            // we will implement proper session logic simply via IP comparison for now
            // To do full time-based session properly, database needs a check method
            // We'll enforce GUI login for safety natively to get the system hooked up.
            // Future feature: True session skipping
        }
        
        // Handle Temp Spawn logic
        if (tempSpawnConfig != null) {
            originLocations[player.uniqueId] = player.location.clone()
            player.teleportAsync(tempSpawnConfig)
        }

        // Delay opening the GUI
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            val gui = io.github.earth1283.guiplayerauth.gui.AuthGUI(plugin, player, !isRegistered)
            gui.open()
            
            // Register it in the GUIListener internally
            if (!isRegistered) {
                player.sendMessage(plugin.configManager.getMessage("prefix").append(plugin.configManager.getMessage("register-success")))
            }
        }, 10L)
    }
}
