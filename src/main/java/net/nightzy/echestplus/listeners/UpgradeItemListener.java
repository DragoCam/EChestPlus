package net.nightzy.echestplus.listeners;

import dev.lone.itemsadder.api.CustomStack;
import net.nightzy.echestplus.config.ConfigManager;
import net.nightzy.echestplus.manager.EnderChestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeItemListener implements Listener {

    private final EnderChestManager manager;
    private final ConfigManager config;

    public UpgradeItemListener(EnderChestManager manager, ConfigManager config) {
        this.manager = manager;
        this.config = config;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        // Obsługa tylko kliknięć prawym przyciskiem
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack itemInHand = event.getItem();
        if (itemInHand == null || !itemInHand.hasItemMeta()) return;

        // Pobieramy wzorcowy przedmiot z konfiguracji
        ItemStack cfgItem = config.getUpgraderItem();
        if (cfgItem == null || !cfgItem.hasItemMeta()) return;

        boolean validItem = false;

        // 1. Sprawdzenie lore (Wspólne dla obu wersji)
        // Pobieramy lore z ręki i lore z configu
        ItemMeta handMeta = itemInHand.getItemMeta();
        ItemMeta cfgMeta = cfgItem.getItemMeta();

        List<String> handLore = handMeta.getLore();
        List<String> cfgLore = cfgMeta.getLore();

        // Porównujemy Lore - jeśli się zgadzają, traktujemy jako ten sam przedmiot
        if (handLore != null && handLore.equals(cfgLore)) {
            validItem = true;
        }

        // 2. Dodatkowa weryfikacja ItemsAdder (jeśli włączone i lore nie wystarczyło)
        if (!validItem && config.isUseItemsAdder()) {
            CustomStack cs = CustomStack.byItemStack(itemInHand);
            if (cs != null && cs.getNamespacedID().equals("echestplus:upgrader")) {
                validItem = true;
            }
        }

        // 3. Dodatkowa weryfikacja ModelData (jeśli lore nie wystarczyło)
        if (!validItem) {
            int cfgModel = cfgMeta.hasCustomModelData() ? cfgMeta.getCustomModelData() : -1;
            int itemModel = handMeta.hasCustomModelData() ? handMeta.getCustomModelData() : -1;
            if (cfgModel != -1 && cfgModel == itemModel) {
                validItem = true;
            }
        }

        if (!validItem) return;

        // --- Logika ulepszania (bez zmian) ---
        Player player = event.getPlayer();
        int current = manager.getSize(player);
        
        if (current >= EnderChestManager.MAX_SIZE) {
            config.sendMessage(player, "fullEnderChest", null);
            event.setCancelled(true);
            return;
        }

        manager.setSize(player, current + 9);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("size", String.valueOf(current + 9));
        placeholders.put("lines", String.valueOf((current + 9) / 9));

        config.sendMessage(player, "upgraded", placeholders);

        // Zabranie przedmiotu
        itemInHand.setAmount(itemInHand.getAmount() - 1);
        event.setCancelled(true);
    }
}