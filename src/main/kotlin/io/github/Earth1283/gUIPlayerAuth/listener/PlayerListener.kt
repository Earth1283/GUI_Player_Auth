package io.github.earth1283.guiplayerauth.listener

import io.github.earth1283.guiplayerauth.GUIPlayerAuthPlugin
import io.github.earth1283.guiplayerauth.config.AuthManager
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class PlayerListener(
    private val plugin: GUIPlayerAuthPlugin,
    private val authManager: AuthManager
) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onMove(e: PlayerMoveEvent) {
        if (authManager.isUnauthenticated(e.player)) {
            val from = e.from
            val to = e.to
            if (to != null && (from.x != to.x || from.y != to.y || from.z != to.z)) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(e: AsyncChatEvent) {
        if (authManager.isUnauthenticated(e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (authManager.isUnauthenticated(e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteract(e: PlayerInteractEvent) {
        if (authManager.isUnauthenticated(e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockBreak(e: BlockBreakEvent) {
        if (authManager.isUnauthenticated(e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (authManager.isUnauthenticated(e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDamage(e: EntityDamageEvent) {
        val entity = e.entity
        if (entity is org.bukkit.entity.Player && authManager.isUnauthenticated(entity)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onTarget(e: EntityTargetEvent) {
        val target = e.target
        if (target is org.bukkit.entity.Player && authManager.isUnauthenticated(target)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDrop(e: PlayerDropItemEvent) {
        if (authManager.isUnauthenticated(e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPickup(e: EntityPickupItemEvent) {
        val entity = e.entity
        if (entity is org.bukkit.entity.Player && authManager.isUnauthenticated(entity)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClose(e: InventoryCloseEvent) {
        val player = e.player as org.bukkit.entity.Player
        if (authManager.isUnauthenticated(player)) {
            // Must delay opening GUI slightly because Bukkit throws a fit
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                // TODO: Re-open the GUI here
            }, 1L)
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        authManager.removeUnauthenticated(e.player)
    }
}
