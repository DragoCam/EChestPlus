package net.nightzy.echestplus.listeners;

import net.kyori.adventure.text.Component;
import net.nightzy.echestplus.config.ConfigManager;
import net.nightzy.echestplus.manager.EnderChestManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles ender chest interaction and inventory persistence.
 */
public class EnderChestListener implements Listener {

    // ============================================================
    // Fields
    // ============================================================

    private final EnderChestManager manager; // Ender chest logic handler
    private final ConfigManager config;      // Configuration & messages provider

    // ============================================================
    // Constructor
    // ============================================================

    public EnderChestListener(EnderChestManager manager, ConfigManager config) {
        this.manager = manager;
        this.config = config;
    }

    // ============================================================
    // Event Handlers
    // ============================================================

    /**
     * Intercepts default ender chest interaction
     * and opens the custom ender chest instead.
     */
    @EventHandler
    public void onEnderChestOpen(PlayerInteractEvent event) {

        // Validate interaction
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.ENDER_CHEST) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // Cancel vanilla behavior
        event.setCancelled(true);
        manager.openEnderChest(event.getPlayer());
    }

    /**
     * Saves ender chest contents when inventory is closed.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        // Only players are relevant
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Component actualTitle = event.getView().title();

        // Check if admin is viewing another player's ender chest
        if (manager.getAdminViewTarget(player.getUniqueId()) != null) {
            manager.saveAdminInventory(player, event.getInventory());
            return;
        }

        // Check if this is the player's own ender chest
        Component expectedTitle = config.getEnderChestName();
        if (!actualTitle.equals(expectedTitle)) return;

        manager.saveEnderChest(player, event.getInventory());
    }
}
