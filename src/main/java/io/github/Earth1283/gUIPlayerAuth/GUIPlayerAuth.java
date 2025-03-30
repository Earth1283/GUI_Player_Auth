package io.github.Earth1283.gUIPlayerAuth;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIPlayerAuth extends JavaPlugin implements Listener {

    private static final Map<UUID, StringBuilder> playerPins = new HashMap<>();
    private static final Map<UUID, Integer> playerAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final int PIN_LENGTH = 4; // Configurable PIN length
    private Connection connection;

    @Override
    public void onEnable() {
        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);

        // Set up the SQLite database
        setupDatabase();

        // Schedule a task to kick unauthenticated players after 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!isAuthenticated(player)) {
                        player.kickPlayer("Authentication required.");
                    }
                }
            }
        }.runTaskLater(this, 600); // 600 ticks = 30 seconds
    }

    @Override
    public void onDisable() {
        // Close the database connection
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupDatabase() {
        File dataFolder = new File(getDataFolder(), "GUIPlayerAuth");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File dbFile = new File(dataFolder, "auth.db");
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS player_pins (uuid TEXT PRIMARY KEY, pin TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isAuthenticated(Player player) {
        return playerPins.containsKey(player.getUniqueId());
    }

    private void openAuthGUI(Player player, boolean isRegistering) {
        Inventory gui = Bukkit.createInventory(null, 9, isRegistering ? "Register" : "Login");

        for (int i = 0; i < 9; i++) {
            ItemStack item = new ItemStack(Material.LIGHT_BLUE_DYE, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(String.valueOf(i + 1));
            item.setItemMeta(meta);
            gui.setItem(i, item);
        }

        ItemStack confirmItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName("Confirm PIN");
        confirmItem.setItemMeta(confirmMeta);
        gui.setItem(8, confirmItem);

        ItemStack clearItem = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta clearMeta = clearItem.getItemMeta();
        clearMeta.setDisplayName("Clear PIN");
        clearItem.setItemMeta(clearMeta);
        gui.setItem(7, clearItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isAuthenticated(player)) {
            openAuthGUI(player, false); // Open login GUI
        } else {
            openAuthGUI(player, true); // Open registration GUI
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals("Register") || event.getView().getTitle().equals("Login")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.LIGHT_BLUE_DYE) {
                String pinDigit = clickedItem.getItemMeta().getDisplayName();
                playerPins.computeIfAbsent(player.getUniqueId(), k -> new StringBuilder()).append(pinDigit);
            } else if (clickedItem != null && clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
                if (playerPins.containsKey(player.getUniqueId()) && playerPins.get(player.getUniqueId()).length() == PIN_LENGTH) {
                    String pin = playerPins.get(player.getUniqueId()).toString();
                    savePinToDatabase(player, pin);
                    player.sendMessage("PIN set successfully.");
                    playerPins.remove(player.getUniqueId());
                } else {
                    player.sendMessage("Please enter a complete PIN.");
                }
            } else if (clickedItem != null && clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                playerPins.remove(player.getUniqueId());
                player.sendMessage("PIN entry cleared.");
            }
        }
    }

    private void savePinToDatabase(Player player, String pin) {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO player_pins (uuid, pin) VALUES (?, ?)");
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, pin);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (!isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerPins.remove(event.getPlayer().getUniqueId());
        playerAttempts.remove(event.getPlayer().getUniqueId());
    }
}
