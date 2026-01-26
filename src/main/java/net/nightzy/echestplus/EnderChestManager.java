package net.nightzy.echestplus;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EnderChestManager {

    public static final int BASE_SIZE = 27;
    public static final int MAX_SIZE = 54;

    private final DatabaseYML database;

    public EnderChestManager(DatabaseYML database) {
        this.database = database;
    }

    /* ================= SIZE ================= */

    public int getSize(Player player) {
        int size = database.getSize(player.getUniqueId());
        if (size <= 0) return BASE_SIZE;
        return normalize(size);
    }

    public void setSize(Player player, int newSize) {
        int size = normalize(newSize);
        size = Math.min(size, MAX_SIZE);
        database.setSize(player.getUniqueId(), size);
    }

    private int normalize(int size) {
        return Math.max(9, Math.min(54, (size / 9) * 9));
    }

    /* ================= INVENTORY ================= */

    public void openEnderChest(Player player) {
        int size = getSize(player);

        Inventory inv = Bukkit.createInventory(
                player,
                size,
                Component.text("Ender Chest")
        );

        ItemStack[] contents = database.loadItems(player.getUniqueId(), size);
        inv.setContents(contents);

        player.openInventory(inv);
    }

    public void saveEnderChest(Player player, Inventory inv) {
        database.saveItems(player.getUniqueId(), inv.getContents());
    }

    public boolean addUpgrade(Player player, int count) {
        int current = getSize(player);
        int newSize = Math.min(current + (count * 9), MAX_SIZE);
        if (newSize == current) return false;
        setSize(player, newSize);
        return true;
    }
}
