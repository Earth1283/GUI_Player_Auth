package io.github.earth1283.guiplayerauth.config

import io.github.earth1283.guiplayerauth.GUIPlayerAuthPlugin
import io.github.earth1283.guiplayerauth.database.Database
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class AuthManager(private val plugin: GUIPlayerAuthPlugin, val database: Database) {

    // Store players currently authenticating (true = registered, false = not registered)
    private val unauthenticatedPlayers = ConcurrentHashMap<UUID, Boolean>()

    fun isUnauthenticated(player: Player): Boolean {
        return unauthenticatedPlayers.containsKey(player.uniqueId)
    }

    fun isRegistered(player: Player): Boolean {
        return unauthenticatedPlayers[player.uniqueId] ?: false
    }

    fun addUnauthenticated(player: Player, isRegistered: Boolean) {
        unauthenticatedPlayers[player.uniqueId] = isRegistered
    }

    fun removeUnauthenticated(player: Player) {
        unauthenticatedPlayers.remove(player.uniqueId)
    }

    fun checkLogin(player: Player, pin: String): Boolean {
        val hash = database.getPlayerHash(player.uniqueId) ?: return false
        val ip = player.address?.address?.hostAddress ?: "unknown"
        if (PasswordUtils.verifyPin(pin, hash)) {
            removeUnauthenticated(player)
            database.updateLogin(player.uniqueId, ip)
            return true
        }
        return false
    }

    fun register(player: Player, pin: String) {
        val hash = PasswordUtils.hashPin(pin)
        val ip = player.address?.address?.hostAddress ?: "unknown"
        database.registerPlayer(player.uniqueId, hash, ip)
        removeUnauthenticated(player)
    }
}
