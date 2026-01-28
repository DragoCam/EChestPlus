package net.nightzy.echestplus.listeners;

import net.nightzy.echestplus.config.ConfigManager;
import net.nightzy.echestplus.manager.EnderChestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles usage of the ender chest upgrade item.
 */
public class UpgradeItemListener implements Listener {

    // ============================================================
    // Fields
    // ============================================================

    private final EnderChestManager manager; // Ender chest size manager
    private final ConfigManager config;      // Configuration & messages provider

    // ============================================================
    // Constructor
    // ============================================================

    public UpgradeItemListener(EnderChestManager manager, ConfigManager config) {
        this.manager = manager;
        this.config = config;
    }

    // ============================================================
    // Event Handlers
    // ============================================================

    /**
     * Detects usage of the configured upgrade item
     * and increases the player's ender chest size.
     */
    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        // Validate interaction type
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // Validate item
        if (event.getItem() == null) return;

        ItemStack item = event.getItem();
        if (!item.hasItemMeta()) return;

        // Resolve used item's custom model data
        int itemModel = -1;
        if (item.getItemMeta().hasCustomModelData()) {
            itemModel = item.getItemMeta().getCustomModelData();
        }

        // Resolve configured upgrader item's model data
        int configuredModel = 999;
        try {
            ItemStack cfg = config.getUpgraderItem();
            if (cfg != null &&
                cfg.hasItemMeta() &&
                cfg.getItemMeta().hasCustomModelData()) {

                configuredModel = cfg.getItemMeta().getCustomModelData();
            }
        } catch (Exception ignored) {
            // Ignore malformed config
        }

        // Item does not match upgrader
        if (itemModel != configuredModel) return;

        Player player = event.getPlayer();

        // Check max ender chest size
        int current = manager.getSize(player);
        if (current >= EnderChestManager.MAX_SIZE) {
            config.sendMessage(player, "fullEnderChest", null, null);
            return;
        }

        // Increase ender chest size
        manager.setSize(player, current + 9);
        String sizeStr = String.valueOf(current + 9);
        config.sendMessage(player, "upgraded", "size", sizeStr);

        // Consume item and cancel interaction
        item.setAmount(item.getAmount() - 1);
        event.setCancelled(true);
    }
}
