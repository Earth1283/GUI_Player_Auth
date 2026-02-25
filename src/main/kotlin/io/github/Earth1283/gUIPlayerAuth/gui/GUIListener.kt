package io.github.earth1283.guiplayerauth.gui

import io.github.earth1283.guiplayerauth.GUIPlayerAuthPlugin
import io.github.earth1283.guiplayerauth.config.AuthManager
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import java.util.UUID

class GUIListener(
    private val plugin: GUIPlayerAuthPlugin,
    private val authManager: AuthManager
) : Listener {

    private val activeGUIs = mutableMapOf<UUID, AuthGUI>()
    private val attemptCounts = mutableMapOf<UUID, Int>()

    fun openGUI(player: Player, isRegistration: Boolean) {
        val gui = AuthGUI(plugin, player, isRegistration)
        activeGUIs[player.uniqueId] = gui
        
        // Ensure player is kept in unauthenticated state
        authManager.addUnauthenticated(player, !isRegistration)
        
        // Open safely on next tick to avoid inventory bugs during PlayerJoinEvent
        plugin.server.scheduler.runTask(plugin, Runnable {
            gui.open()
        })
    }

    fun removeGUI(player: Player) {
        activeGUIs.remove(player.uniqueId)
    }

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return
        val gui = activeGUIs[player.uniqueId] ?: return

        // Cancel all clicks for players who have an active auth GUI
        e.isCancelled = true

        val clickedItem = e.currentItem ?: return
        if (clickedItem.type == Material.AIR) return
        
        // We only care about clicks in the top inventory (the GUI itself)
        if (e.clickedInventory != e.view.topInventory) return

        val slot = e.slot

        // Check if it's a digit click
        val digitMatch = mapOf(
            49 to 0, 20 to 1, 21 to 2, 22 to 3,
            29 to 4, 30 to 5, 31 to 6,
            38 to 7, 39 to 8, 40 to 9
        )

        if (digitMatch.containsKey(slot)) {
            gui.appendDigit(digitMatch[slot]!!)
            return
        }

        // Clear button (slot 48)
        if (slot == 48) {
            gui.clearPin()
            return
        }

        // Enter button (slot 50)
        if (slot == 50) {
            val pin = gui.currentPin
            val minLen = plugin.configManager.getMinPinLength()
            
            if (pin.length < minLen) {
                player.sendMessage(plugin.configManager.getMessage("pin-too-short", mapOf("min" to minLen.toString())))
                gui.clearPin()
                return
            }

            val isRegistered = authManager.isRegistered(player)

            if (isRegistered) {
                // Login flow
                if (authManager.checkLogin(player, pin)) {
                    player.sendMessage(plugin.configManager.getMessage("login-success"))
                    player.closeInventory() // which triggers PlayerListener.onInventoryClose -> but player is no longer unauthenticated, so it won't re-open
                    removeGUI(player)
                    attemptCounts.remove(player.uniqueId)
                    return
                } else {
                    val attempts = attemptCounts.getOrDefault(player.uniqueId, 0) + 1
                    attemptCounts[player.uniqueId] = attempts
                    val maxAttempts = plugin.configManager.getMaxAttempts()

                    if (attempts >= maxAttempts) {
                        player.kick(plugin.configManager.getMessage("kick-max-attempts"))
                        removeGUI(player)
                        attemptCounts.remove(player.uniqueId)
                        return
                    } else {
                        player.sendMessage(plugin.configManager.getMessage("wrong-pin", mapOf("attempts" to (maxAttempts - attempts).toString())))
                        gui.clearPin()
                        return
                    }
                }
            } else {
                // Registration flow
                authManager.register(player, pin)
                player.sendMessage(plugin.configManager.getMessage("register-success"))
                player.closeInventory()
                removeGUI(player)
                return
            }
        }
    }
}
