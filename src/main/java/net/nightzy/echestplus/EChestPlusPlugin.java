package net.nightzy.echestplus;

import net.nightzy.echestplus.commands.AdminCommand;
import net.nightzy.echestplus.commands.AdminCommandTabCompleter;
import net.nightzy.echestplus.config.ConfigManager;
import net.nightzy.echestplus.listeners.EnderChestListener;
import net.nightzy.echestplus.listeners.UpgradeItemListener;
import net.nightzy.echestplus.manager.EnderChestManager;
import net.nightzy.echestplus.storage.DatabaseFactory;
import net.nightzy.echestplus.storage.DatabaseProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for EChestPlus.
 * Handles initialization of configuration, database, managers, listeners, and commands.
 */
public class EChestPlusPlugin extends JavaPlugin {

    // ============================================================
    // Fields
    // ============================================================

    private DatabaseProvider database;          // Database provider for persistent storage
    private EnderChestManager enderChestManager; // Manager for player ender chests
    private ConfigManager configManager;         // Configuration manager

    // ============================================================
    // Plugin Lifecycle
    // ============================================================

    @Override
    public void onEnable() {
        try {
            // Ensure data folder exists
            if (!getDataFolder().exists()) getDataFolder().mkdirs();

            // Initialize configuration
            configManager = new ConfigManager(this);

            // Initialize database provider
            database = DatabaseFactory.create(this, configManager);

            // Initialize ender chest manager
            enderChestManager = new EnderChestManager(database, configManager);

            // Register listeners
            getServer().getPluginManager().registerEvents(
                new EnderChestListener(enderChestManager, configManager), this
            );

            getServer().getPluginManager().registerEvents(
                new UpgradeItemListener(enderChestManager, configManager), this
            );

            // Register admin command and tab completer
            AdminCommand adminCommand = new AdminCommand(enderChestManager, configManager);
            getCommand("adminec").setExecutor(adminCommand);
            getCommand("adminec").setTabCompleter(new AdminCommandTabCompleter());

            getLogger().info("EChestPlus enabled.");
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
