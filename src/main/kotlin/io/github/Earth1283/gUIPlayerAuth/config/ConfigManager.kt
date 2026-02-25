package io.github.earth1283.guiplayerauth.config

import org.bukkit.Location
import org.bukkit.Bukkit
import io.github.earth1283.guiplayerauth.GUIPlayerAuthPlugin

class ConfigManager(private val plugin: GUIPlayerAuthPlugin) {

    fun getTempSpawn(): Location? {
        if (!plugin.config.getBoolean("auth.temp-spawn.enabled")) {
            return null
        }
        val worldName = plugin.config.getString("auth.temp-spawn.world") ?: "world"
        val world = Bukkit.getWorld(worldName) ?: return null
        
        val x = plugin.config.getDouble("auth.temp-spawn.x")
        val y = plugin.config.getDouble("auth.temp-spawn.y")
        val z = plugin.config.getDouble("auth.temp-spawn.z")
        val yaw = plugin.config.getDouble("auth.temp-spawn.yaw").toFloat()
        val pitch = plugin.config.getDouble("auth.temp-spawn.pitch").toFloat()
        
        return Location(world, x, y, z, yaw, pitch)
    }

    fun getMinPinLength(): Int {
        return plugin.config.getInt("auth.min-pin-length", 4)
    }

    fun getMaxPinLength(): Int {
        return plugin.config.getInt("auth.max-pin-length", 12)
    }
    
    fun getSessionTimeoutMinutes(): Int {
        return plugin.config.getInt("auth.session-timeout-minutes", 60)
    }

    fun getMaxAttempts(): Int {
        return plugin.config.getInt("auth.max-attempts", 3)
    }

    fun getMessage(key: String, placeholders: Map<String, String> = emptyMap()): net.kyori.adventure.text.Component {
        val prefixRaw = plugin.config.getString("messages.prefix", "")
        var msgRaw = plugin.config.getString("messages.$key", "") ?: ""
        
        placeholders.forEach { (k, v) ->
            msgRaw = msgRaw.replace("<$k>", v)
        }
        
        return GUIPlayerAuthPlugin.mm.deserialize(prefixRaw + msgRaw)
    }
}
