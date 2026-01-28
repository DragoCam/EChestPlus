package net.nightzy.echestplus.manager;

import net.kyori.adventure.text.Component;
import net.nightzy.echestplus.config.ConfigManager;
import net.nightzy.echestplus.storage.DatabaseProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages ender chest size, inventory access and persistence.
 * Handles both player and admin interactions.
 */
public class EnderChestManager {

    // ============================================================
    // Constants
    // ============================================================

    public static final int MAX_SIZE = 54; // Maximum allowed ender chest size

    // ============================================================
    // Fields
    // ============================================================

    private final DatabaseProvider database; // Storage provider
    private final ConfigManager config;      // Configuration access

    /**
     * Maps admin UUID -> target player UUID
     * Used to track admin inventory views.
     */
    private final Map<UUID, UUID> adminViewMap = new HashMap<>();

    // ============================================================
    // Constructor
    // ============================================================

    public EnderChestManager(DatabaseProvider database, ConfigManager config) {
        this.database = database;
        this.config = config;
    }

    // ============================================================
    // Size Management
    // ============================================================

    /**
     * Returns the normalized ender chest size for a player.
     */
    public int getSize(Player player) {

        int size = database.getSize(player.getUniqueId());
        if (size <= 0) {
            return config.getEnderChestSize();
        }

        return normalize(size);
    }

    /**
     * Sets the ender chest size for a player.
     */
    public void setSize(Player player, int newSize) {

        int size = normalize(newSize);
        size = Math.min(size, MAX_SIZE);
        database.setSize(player.getUniqueId(), size);
    }

    /**
     * Normalizes inventory size to valid multiples of 9.
     */
    private int normalize(int size) {
        return Math.max(9, Math.min(54, (size / 9) * 9));
    }

    // ============================================================
    // Inventory Handling
    // ============================================================

    /**
     * Opens the player's own ender chest.
     */
    public void openEnderChest(Player player) {

        int size = getSize(player);

        Inventory inv = Bukkit.createInventory(
                player,
                size,
                config.getEnderChestName()
        );

        ItemStack[] contents = database.loadItems(player.getUniqueId(), size);
        inv.setContents(contents);

        player.openInventory(inv);
    }

    /**
     * Saves the contents of the player's ender chest.
     */
    public void saveEnderChest(Player player, Inventory inv) {
        database.saveItems(player.getUniqueId(), inv.getContents());
    }

    /**
     * Opens another player's ender chest for an admin.
     */
    public void openForAdmin(Player admin, UUID targetUUID) {

        int size = getSize(targetUUID);

        Inventory inv = Bukkit.createInventory(
                admin,
                size,
                Component.text("EnderChest: " + targetUUID)
        );

        ItemStack[] contents = database.loadItems(targetUUID, size);
        inv.setContents(contents);

        adminViewMap.put(admin.getUniqueId(), targetUUID);
        admin.openInventory(inv);
    }

    /**
     * Sets the ender chest size for a player UUID.
     */
    public void setSizeForUUID(UUID uuid, int newSize) {

        int size = normalize(newSize);
        size = Math.min(size, MAX_SIZE);
        database.setSize(uuid, size);
    }

    /**
     * Returns normalized size for a UUID.
     */
    private int getSize(UUID uuid) {

        int size = database.getSize(uuid);
        if (size <= 0) {
            return config.getEnderChestSize();
        }

        return normalize(size);
    }

    /**
     * Saves admin-viewed inventory and clears admin view state.
     */
    public void saveAdminInventory(Player admin, Inventory inv) {

        UUID targetUUID = adminViewMap.get(admin.getUniqueId());
        if (targetUUID != null) {
            database.saveItems(targetUUID, inv.getContents());
            adminViewMap.remove(admin.getUniqueId());
        }
    }

    /**
     * Returns the UUID of the player currently viewed by the admin.
     */
    public UUID getAdminViewTarget(UUID adminUUID) {
        return adminViewMap.get(adminUUID);
    }

    // ============================================================
    // Upgrade Utilities
    // ============================================================

    /**
     * Adds ender chest upgrades to a player.
     *
     * @return true if size changed, false if already at max
     */
    public boolean addUpgrade(Player player, int count) {

        int current = getSize(player);
        int newSize = Math.min(current + (count * 9), MAX_SIZE);

        if (newSize == current) {
            return false;
        }

        setSize(player, newSize);
        return true;
    }
}
