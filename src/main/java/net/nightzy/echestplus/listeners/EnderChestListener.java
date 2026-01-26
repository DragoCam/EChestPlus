package net.nightzy.echestplus.listeners;

import net.nightzy.echestplus.EnderChestManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EnderChestListener implements Listener {

    private final EnderChestManager manager;

    public EnderChestListener(EnderChestManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onEnderChestOpen(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.ENDER_CHEST) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        event.setCancelled(true);
        manager.openEnderChest(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (!event.getView().getTitle().startsWith("Ender Chest")) return;

        manager.saveEnderChest(player, event.getInventory());
    }
}
