package net.nightzy.echestplus.storage;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

/**
 * Interface defining the contract for all database storage providers.
 * Handles ender chest sizes, upgrades, and inventory persistence.
 */
public interface DatabaseProvider {

    /**
     * Retrieves the number of upgrades a player has purchased.
     *
     * @param uuid unique identifier of the player
     * @return number of upgrades
     */
    int getUpgrades(UUID uuid);

    /**
     * Sets the number of upgrades a player has.
     *
     * @param uuid unique identifier of the player
     * @param upgrades new upgrade count
     */
    void setUpgrades(UUID uuid, int upgrades);

    /**
     * Gets the saved ender chest size for a player.
     *
     * @param uuid unique identifier of the player
     * @return ender chest size
     */
    int getSize(UUID uuid);

    /**
     * Updates the saved ender chest size for a player.
     *
     * @param uuid unique identifier of the player
     * @param size new ender chest size
     */
    void setSize(UUID uuid, int size);

    /**
     * Persists the items of a player's ender chest.
     *
     * @param uuid unique identifier of the player
     * @param items array of ItemStack representing the chest contents
     */
    void saveItems(UUID uuid, ItemStack[] items);

    /**
     * Loads the items of a player's ender chest.
     *
     * @param uuid unique identifier of the player
     * @param size expected size of the inventory
     * @return array of ItemStack containing the player's items
     */
    ItemStack[] loadItems(UUID uuid, int size);
}
