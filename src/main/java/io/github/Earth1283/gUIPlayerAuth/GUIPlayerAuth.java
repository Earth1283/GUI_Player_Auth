package io.github.Earth1283.gUIPlayerAuth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import org.bukkit.plugin.java.JavaPlugin;

public class GUIPlayerAuth extends JavaPlugin implements Listener {

    private static Connection connection;
    private final Map<Player, List<Integer>> pinInputs = new HashMap<>();
    private final Set<Player> unauthenticatedPlayers = new HashSet<>();
    private final Map<Player, BukkitRunnable> kickTimers = new HashMap<>();

    @Override
    public void onEnable() {
        createDatabase();
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();

        // Register /resetpin command
        getCommand("resetpin").setExecutor(this);
    }

    @Override
    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            getLogger().warning("Failed to close the database connection.");
            e.printStackTrace();
        }
    }

    private void createDatabase() {
        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }

        File dbFile = new File(pluginFolder, "playerauth.db");
        try {
            if (!dbFile.exists()) {
                dbFile.createNewFile();
                getLogger().info("Database file created: " + dbFile.getAbsolutePath());
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            PreparedStatement stmt = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, pin TEXT)");
            stmt.executeUpdate();
            getLogger().info("Database initialized successfully.");
        } catch (SQLException | IOException e) {
            getLogger().severe("Error creating or connecting to the database.");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String username = player.getName();
        getLogger().info("Player " + username + " joined the game.");

        unauthenticatedPlayers.add(player);
        applyBlindnessEffect(player);

        if (connection == null) {
            getLogger().warning("Database connection is null! Attempting to reconnect...");
            createDatabase();
        }

        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT pin FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                openRegisterGUI(player);
            } else {
                openLoginGUI(player);
            }

        } catch (SQLException e) {
            getLogger().severe("Error checking registration status for player " + username);
            e.printStackTrace();
        }

        // Start the kick timer (30 seconds)
        startKickTimer(player);
    }

    private void startKickTimer(Player player) {
        BukkitRunnable kickTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (unauthenticatedPlayers.contains(player)) {
                    player.kickPlayer(ChatColor.RED + "You failed to authenticate within 30 seconds.");
                    unauthenticatedPlayers.remove(player);
                }
            }
        };
        kickTask.runTaskLater(this, 600L); // 600 ticks = 30 seconds
        kickTimers.put(player, kickTask);
    }

    private void cancelKickTimer(Player player) {
        if (kickTimers.containsKey(player)) {
            kickTimers.get(player).cancel();
            kickTimers.remove(player);
        }
    }

    private void openRegisterGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "Register");

        for (int i = 1; i <= 9; i++) {
            inv.setItem(i - 1, createNumberItem(i));
        }

        inv.setItem(18, createButton(Material.LIME_STAINED_GLASS_PANE, "Confirm PIN"));
        inv.setItem(26, createButton(Material.RED_STAINED_GLASS_PANE, "Clear PIN"));

        player.openInventory(inv);
        pinInputs.put(player, new ArrayList<>());
    }

    private void openLoginGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "Login");

        for (int i = 1; i <= 9; i++) {
            inv.setItem(i - 1, createNumberItem(i));
        }

        inv.setItem(18, createButton(Material.LIME_STAINED_GLASS_PANE, "Confirm PIN"));
        inv.setItem(26, createButton(Material.RED_STAINED_GLASS_PANE, "Clear PIN"));

        player.openInventory(inv);
        pinInputs.put(player, new ArrayList<>());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelKickTimer(event.getPlayer());
        unauthenticatedPlayers.remove(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String inventoryTitle = event.getView().getTitle();

        if (!inventoryTitle.equals("Register") && !inventoryTitle.equals("Login")) {
            return;
        }

        event.setCancelled(true); // Prevent item movement

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();

        // Handle PIN digit selection
        if (itemName.startsWith(ChatColor.AQUA + "PIN: ")) {
            int digit = Integer.parseInt(itemName.replace(ChatColor.AQUA + "PIN: ", ""));
            List<Integer> pin = pinInputs.get(player);

            if (pin.size() < getConfig().getInt("pin_length", 4)) {
                pin.add(digit);
                player.sendMessage(ChatColor.AQUA + "Digit " + digit + " added. Current PIN: " + pin);
            } else {
                player.sendMessage(ChatColor.RED + "You have already entered the maximum number of digits.");
            }
            return;
        }

        // Handle Confirm PIN button
        if (itemName.equals(ChatColor.BOLD + "Confirm PIN")) {
            List<Integer> pin = pinInputs.get(player);

            // Ensure a valid PIN length
            if (pin.size() != getConfig().getInt("pin_length", 4)) {
                player.sendMessage(ChatColor.RED + "Your PIN must be exactly " + getConfig().getInt("pin_length", 4) + " digits.");
                return;
            }

            String pinString = pin.toString().replaceAll("[\\[\\], ]", ""); // Convert PIN list to a string

            try {
                if (inventoryTitle.equals("Register")) {
                    // Register the player by saving the PIN in the database
                    PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO users (username, pin) VALUES (?, ?)");
                    stmt.setString(1, player.getName());
                    stmt.setString(2, pinString);
                    stmt.executeUpdate();

                    player.sendMessage(ChatColor.GREEN + "Registration successful! You are now logged in.");
                } else if (inventoryTitle.equals("Login")) {
                    // Verify PIN for an existing user
                    PreparedStatement stmt = connection.prepareStatement("SELECT pin FROM users WHERE username = ?");
                    stmt.setString(1, player.getName());
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next() && rs.getString("pin").equals(pinString)) {
                        player.sendMessage(ChatColor.GREEN + "Login successful!");
                    } else {
                        player.kickPlayer(ChatColor.RED + "Incorrect PIN. You have been kicked from the server.");
                        return;
                    }
                }

                // If login or registration is successful, remove player from restrictions
                unauthenticatedPlayers.remove(player);
                removeBlindnessEffect(player);
                cancelKickTimer(player);
                player.closeInventory();

            } catch (SQLException e) {
                player.sendMessage(ChatColor.RED + "An error occurred. Please contact an admin.");
                e.printStackTrace();
            }
            return;
        }

        // Handle Clear PIN button
        if (itemName.equals(ChatColor.BOLD + "Clear PIN")) {
            pinInputs.get(player).clear();
            player.sendMessage(ChatColor.YELLOW + "Your PIN has been cleared.");
        }
    }

    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                pinInputs.put(player, new ArrayList<>());
                openRegisterGUI(player);
                player.sendMessage(ChatColor.YELLOW + "Your PIN has been reset. Please register a new PIN.");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /resetpin");
            }
        }
        return false;
    }

    private void applyBlindnessEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false));
    }

    private void removeBlindnessEffect(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }

    private ItemStack createNumberItem(int digit) {
        ItemStack item = new ItemStack(Material.LIGHT_BLUE_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "PIN: " + digit);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BOLD + name);
        item.setItemMeta(meta);
        return item;
    }
}
