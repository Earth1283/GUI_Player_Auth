package io.github.earth1283.guiplayerauth.gui

import io.github.earth1283.guiplayerauth.GUIPlayerAuthPlugin
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID

class AuthGUI(
    private val plugin: GUIPlayerAuthPlugin,
    private val player: Player,
    private val isRegistration: Boolean
) {
    private val inventory: Inventory = Bukkit.createInventory(player, 54, getTitle())
    var currentPin: String = ""
        private set

    companion object {
        // Source: https://minecraft-heads.com/custom-heads/alphabet
        // Base64 strings for digits 0-9
        private val digitHeads = mapOf(
            0 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM3Y0M2M1N2E2YTIzOGQ3MWM4ZDQxNDRiNTI1MDIzYjA0NzI3YWQ4M2MyNDE1NzZiZTI3ZjJiOTJhMTJiODRmNiJ9fX0=",
            1 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWIyZGY0Nzc5YTVmZGE4ZWIzYmVmOTRlZTBkNGEyNDE4MTA5N2QxYTU0YTkzMWNjNDFlOTg3NGRlZWZiZWI5YiJ9fX0=",
            2 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGZhYzRlODYxNGE1MWUyOWNkMzFhYTkzNDE0NDk3ZGFiNGMwYzZiNWJiMzZjOTYxYzM5M2JlOWMzYjY4MTM5OCJ9fX0=",
            3 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDg5NTBlNDkyMDcyOWVkYTgzOTc2ZDBkYzM2MGExYmM1ZmNlNjlhODZhNWQwODRkZDJjZmQ0NTc5Mjk5MyJ9fX0=",
            4 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWJmOTFiMjY3MzQzNmVjMzNkNDRlZmNiMmViMjFhZDBjYWNkN2MzYTA2NTg1MTc5NDVlYTE1ODYyNmNlMiJ9fX0=",
            5 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDE2NWI3OWEwMmYzYTlmODNhNWNiMmFkZWUxM2RlZjMyODE1ZmU5OTIyNmIzZmFhODUyMzdhZjhhNWU1M2JhNCJ9fX0=",
            6 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZjOWI3YmM5OGFhYzI0NmNjZDVhNzRkOTdiZjhjMWZmODU4ZjdjODhjYzE3NmY4NWQwYWNjZTNkNDE5MWIxNCJ9fX0=",
            7 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZkODkxMjBiNmZiMzliMWUwZjUzZjI3NmNiZjg2MDY1NjU5MmYwOWU3N2E0MDhjOTc3MzlmZjZkZTY3ZDk3ZiJ9fX0=",
            8 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGZhOGM5MzIyOGYwZDdjZDZjODhhZDAyOWYzYjk2NzI1ZmNmZDBhZjIyNzBlYTZiMWVkYzY5MDUzNzJmYTc4MSJ9fX0=",
            9 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzIzYzdiNDk5NWMwZmExOTJmZmYyOWNiYTY2MDU2NDE5OTkxMzVjZjgzYjhjZWJhMTllODgzZDVmZWY2NDkzOCJ9fX0="
        )

        // Red cross
        val clearHead = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjQ1NTlkNzM0NmI0MzIzNzAxM2FjNWJlMTQ4MTliOTQwMTRhM2RiOTI0ZmFkNDk3OWRiZTc5YmFlMzZhNTJkZSJ9fX0="
        
        // Green checkmark
        val enterHead = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGI4MTE3ZjdlZDM3ZTU1MDM3NjliNWZmMmYwNThmYjhkYTVkNThiZGZkNzdhNzM2MGNkYzVmNmFmNWYxODFlMCJ9fX0="
    }

    init {
        updateInventory()
    }

    private fun getTitle(): Component {
        return if (isRegistration) {
            GUIPlayerAuthPlugin.mm.deserialize("<dark_purple><bold>Register your PIN</bold></dark_purple>")
        } else {
            GUIPlayerAuthPlugin.mm.deserialize("<gold><bold>Enter your PIN</bold></gold>")
        }
    }

    fun appendDigit(digit: Int) {
        val maxLen = plugin.configManager.getMaxPinLength()
        if (currentPin.length < maxLen) {
            currentPin += digit
            updateDisplay()
        }
    }

    fun clearPin() {
        currentPin = ""
        updateDisplay()
    }

    fun open() {
        player.openInventory(inventory)
    }

    private fun updateInventory() {
        val filler = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val fillerMeta = filler.itemMeta
        fillerMeta.displayName(Component.empty())
        filler.itemMeta = fillerMeta

        for (i in 0 until 54) {
            inventory.setItem(i, filler)
        }

        // Layout:
        // Rows 0-1: Display Area
        // Rows 2-4: Numpad (1-9)
        // Row 5: Clear, 0, Enter
        
        // Setup visual buttons
        setNumpadDigit(1, 20)
        setNumpadDigit(2, 21)
        setNumpadDigit(3, 22)
        setNumpadDigit(4, 29)
        setNumpadDigit(5, 30)
        setNumpadDigit(6, 31)
        setNumpadDigit(7, 38)
        setNumpadDigit(8, 39)
        setNumpadDigit(9, 40)
        
        setNumpadDigit(0, 49)

        // Clear button
        val clearItem = HeadBuilder.getHead(clearHead)
        val clearMeta = clearItem.itemMeta
        clearMeta.displayName(GUIPlayerAuthPlugin.mm.deserialize("<red><bold>Clear</bold></red>"))
        clearItem.itemMeta = clearMeta
        inventory.setItem(48, clearItem)

        // Enter button
        val enterItem = HeadBuilder.getHead(enterHead)
        val enterMeta = enterItem.itemMeta
        enterMeta.displayName(GUIPlayerAuthPlugin.mm.deserialize("<green><bold>Enter</bold></green>"))
        enterItem.itemMeta = enterMeta
        inventory.setItem(50, enterItem)

        updateDisplay()
    }

    private fun setNumpadDigit(digit: Int, slot: Int) {
        val b64 = digitHeads[digit] ?: return
        val item = HeadBuilder.getHead(b64)
        val meta = item.itemMeta
        meta.displayName(GUIPlayerAuthPlugin.mm.deserialize("<yellow><bold>$digit</bold></yellow>"))
        item.itemMeta = meta
        inventory.setItem(slot, item)
    }

    private fun updateDisplay() {
        // We will show the display at slot 4
        val displayItem = ItemStack(Material.PAPER)
        val meta = displayItem.itemMeta
        
        val displayStr = if (currentPin.isEmpty()) {
            "<gray><i>Enter Pin...</i></gray>"
        } else {
            "<white><bold>${"*".repeat(currentPin.length)}</bold></white>"
        }
        
        meta.displayName(GUIPlayerAuthPlugin.mm.deserialize(displayStr))
        displayItem.itemMeta = meta
        
        // Show info about constraints
        val lore = mutableListOf<Component>()
        lore.add(Component.empty())
        val minLen = plugin.configManager.getMinPinLength()
        val maxLen = plugin.configManager.getMaxPinLength()
        lore.add(GUIPlayerAuthPlugin.mm.deserialize("<gray>Characters: <white>${currentPin.length}/$maxLen</white></gray>"))
        lore.add(GUIPlayerAuthPlugin.mm.deserialize("<gray>Required Minimum: <white>$minLen</white></gray>"))
        meta.lore(lore)

        displayItem.itemMeta = meta
        inventory.setItem(4, displayItem)
        
        // Visual indicator colors across row 1
        val colorLen = (currentPin.length.toFloat() / maxLen.toFloat() * 9).toInt()
        val cFilled = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
        val cEmpty = ItemStack(Material.RED_STAINED_GLASS_PANE)
        cFilled.itemMeta = cFilled.itemMeta.apply { displayName(Component.empty()) }
        cEmpty.itemMeta = cEmpty.itemMeta.apply { displayName(Component.empty()) }
        
        for (i in 0 until 9) {
            val slot = 9 + i
            if (i < colorLen) {
                inventory.setItem(slot, cFilled)
            } else {
                inventory.setItem(slot, cEmpty)
            }
        }
    }
}
