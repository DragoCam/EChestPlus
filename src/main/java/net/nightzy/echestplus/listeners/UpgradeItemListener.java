package net.nightzy.echestplus.listeners;

import net.nightzy.echestplus.EnderChestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class UpgradeItemListener implements Listener {

    private final EnderChestManager manager;

    public UpgradeItemListener(EnderChestManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null) return;

        ItemStack item = event.getItem();
        if (!item.hasItemMeta()) return;
        if (!item.getItemMeta().hasCustomModelData()) return;
        if (item.getItemMeta().getCustomModelData() != 999) return;

        Player player = event.getPlayer();

        int current = manager.getSize(player);
        if (current >= EnderChestManager.MAX_SIZE) {
            player.sendMessage("§cEnder Chest jest juz na maksimum.");
            return;
        }

        manager.setSize(player, current + 9);
        player.sendMessage("§aEnder Chest ulepszony! (+9 slotów)");

        item.setAmount(item.getAmount() - 1);
        event.setCancelled(true);
    }
}
