package net.nightzy.echestplus.commands;

import net.nightzy.echestplus.config.ConfigManager;
import net.nightzy.echestplus.manager.EnderChestManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Handles administrative commands for EchestPlus.
 * Provides access to ender chest management, resizing and configuration reload.
 */
public class AdminCommand implements CommandExecutor {

    // ============================================================
    // Fields
    // ============================================================

    private final EnderChestManager manager; // Ender chest logic handler
    private final ConfigManager config;      // Configuration & messages provider

    // ============================================================
    // Constructor
    // ============================================================

    public AdminCommand(EnderChestManager manager, ConfigManager config) {
        this.manager = manager;
        this.config = config;
    }

    // ============================================================
    // Command Entry Point
    // ============================================================

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Command available only for players
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCommand available only for players!");
            return true;
        }

        Player admin = (Player) sender;

        // Permission check
        if (!admin.hasPermission("echestplus.admin")) {
            admin.sendMessage(config.getAdminMessage("noPermission", null, null));
            return true;
        }

        // No arguments -> show help
        if (args.length == 0) {
            sendHelp(admin);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        // Dispatch subcommands
        switch (subcommand) {
            case "getitem":
                return handleGetItem(admin, args);
            case "open":
                return handleOpen(admin, args);
            case "size":
                return handleSize(admin, args);
            case "reload":
                return handleReload(admin);
            default:
                sendHelp(admin);
                return true;
        }
    }

    // ============================================================
    // Subcommand Handlers
    // ============================================================

    /**
     * Gives the ender chest upgrade item to a target player.
     */
    private boolean handleGetItem(Player admin, String[] args) {

        // Validate arguments
        if (args.length < 2) {
            admin.sendMessage(config.getAdminMessage("usage", null, null));
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        // Target must be online
        if (target == null) {
            admin.sendMessage(config.getAdminMessage("playerNotOnline", "player", playerName));
            return true;
        }

        // Get upgrader item (fallback if missing)
        ItemStack upgrade = config.getUpgraderItem();
        if (upgrade == null) {
            upgrade = new ItemStack(org.bukkit.Material.NETHER_STAR);
        }

        // Give item
        target.getInventory().addItem(upgrade);
        admin.sendMessage(config.getAdminMessage("itemGiven", "player", target.getName()));
        target.sendMessage(config.getAdminMessage("targetItemGiven", null, null));

        return true;
    }

    /**
     * Opens another player's ender chest for the admin.
     */
    private boolean handleOpen(Player admin, String[] args) {

        // Validate arguments
        if (args.length < 2) {
            admin.sendMessage(config.getAdminMessage("usage", null, null));
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        UUID targetUUID;

        // Resolve UUID (online or offline)
        if (target != null) {
            targetUUID = target.getUniqueId();
        } else {
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            targetUUID = offlinePlayer.getUniqueId();
        }

        // Open ender chest
        manager.openForAdmin(admin, targetUUID);
        admin.sendMessage(config.getAdminMessage("opened", "player", playerName));

        return true;
    }

    /**
     * Changes the size of a player's ender chest.
     */
    private boolean handleSize(Player admin, String[] args) {

        // Validate arguments
        if (args.length < 3) {
            admin.sendMessage(config.getAdminMessage("usage", null, null));
            return true;
        }

        String playerName = args[1];
        int lines;

        // Parse and validate line count
        try {
            lines = Integer.parseInt(args[2]);
            if (lines < 1 || lines > 6) {
                admin.sendMessage(config.getAdminMessage("invalidLines", null, null));
                return true;
            }
        } catch (NumberFormatException e) {
            admin.sendMessage(config.getAdminMessage("invalidNumber", null, null));
            return true;
        }

        Player target = Bukkit.getPlayer(playerName);
        UUID targetUUID;

        // Resolve UUID (online or offline)
        if (target != null) {
            targetUUID = target.getUniqueId();
        } else {
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            targetUUID = offlinePlayer.getUniqueId();
        }

        // Convert lines to inventory size
        int size = lines * 9;
        manager.setSizeForUUID(targetUUID, size);
        admin.sendMessage(config.getAdminMessage("sizeChanged", "player", playerName));

        return true;
    }

    /**
     * Reloads the plugin by disabling and enabling it again.
     */
    private boolean handleReload(Player admin) {

        try {
            Bukkit.getPluginManager().disablePlugin(
                    Bukkit.getPluginManager().getPlugin("EchestPlus")
            );
            Bukkit.getPluginManager().enablePlugin(
                    Bukkit.getPluginManager().getPlugin("EchestPlus")
            );
            admin.sendMessage(config.getAdminMessage("reloaded", null, null));
        } catch (Exception e) {
            admin.sendMessage(config.getAdminMessage("reloadError", "error", e.getMessage()));
            e.printStackTrace();
        }

        return true;
    }

    // ============================================================
    // Help Messages
    // ============================================================

    /**
     * Sends help message to the admin.
     */
    private void sendHelp(Player admin) {
        admin.sendMessage(config.getAdminMessage("helpTitle", null, null));
        admin.sendMessage(config.getAdminMessage("helpGetItem", null, null));
        admin.sendMessage(config.getAdminMessage("helpOpen", null, null));
        admin.sendMessage(config.getAdminMessage("helpSize", null, null));
        admin.sendMessage(config.getAdminMessage("helpReload", null, null));
    }
}
