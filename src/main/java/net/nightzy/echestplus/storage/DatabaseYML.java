package net.nightzy.echestplus.storage;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * YML-based implementation of DatabaseProvider.
 * Stores player data in individual YAML files within the plugin data folder.
 */
public class DatabaseYML implements DatabaseProvider {

    // Folder where all player data files are stored
    private final File dataFolder;

    /**
     * Constructs the YML database provider and ensures the data folder exists.
     *
     * @param dataFolder base folder for plugin data
     */
    public DatabaseYML(File dataFolder) {
        this.dataFolder = new File(dataFolder, "playerdata");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs(); // create directory if missing
        }
    }

    /**
     * Returns the file corresponding to a player's UUID.
     *
     * @param uuid unique identifier of the player
     * @return File object pointing to the player's YAML file
     */
    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid + ".yml");
    }

    /**
     * Loads the YAML configuration for a player.
     * Creates a new configuration if the file does not exist.
     *
     * @param uuid player's UUID
     * @return loaded YamlConfiguration
     */
    private YamlConfiguration loadConfig(UUID uuid) {
        File file = getPlayerFile(uuid);
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }

    /**
     * Saves the YAML configuration for a player to disk.
     *
     * @param uuid player's UUID
     * @param config configuration to save
     */
    private void saveConfig(UUID uuid, YamlConfiguration config) {
        try {
            config.save(getPlayerFile(uuid));
        } catch (IOException e) {
            e.printStackTrace(); // log failure to save
        }
    }

    /**
     * Returns the number of upgrades a player has.
     *
     * @param uuid player's UUID
     * @return number of upgrades
     */
    @Override
    public int getUpgrades(UUID uuid) {
        return loadConfig(uuid).getInt("upgrades", 0);
    }

    /**
     * Sets the number of upgrades a player has.
     *
     * @param uuid player's UUID
     * @param upgrades new upgrade count
     */
    @Override
    public void setUpgrades(UUID uuid, int upgrades) {
        YamlConfiguration config = loadConfig(uuid);
        config.set("upgrades", upgrades);
        saveConfig(uuid, config);
    }

    /**
     * Returns the ender chest size based on the player's upgrades.
     *
     * @param uuid player's UUID
     * @return calculated size (base 27 + upgrades * 9)
     */
    @Override
    public int getSize(UUID uuid) {
        int upgrades = getUpgrades(uuid);
        return 27 + (upgrades * 9);
    }

    /**
     * Updates the player's upgrade count based on a new chest size.
     *
     * @param uuid player's UUID
     * @param size new chest size
     */
    @Override
    public void setSize(UUID uuid, int size) {
        int upgrades = (size - 27) / 9;
        setUpgrades(uuid, Math.max(0, upgrades));
    }

    /**
     * Saves the player's ender chest items to the YAML file.
     *
     * @param uuid player's UUID
     * @param items array of ItemStack representing inventory contents
     */
    @Override
    public void saveItems(UUID uuid, ItemStack[] items) {
        YamlConfiguration config = loadConfig(uuid);

        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                config.set("items." + i, items[i]);
            } else {
                config.set("items." + i, null);
            }
        }

        saveConfig(uuid, config);
    }

    /**
     * Loads the player's ender chest items from the YAML file.
     *
     * @param uuid player's UUID
     * @param size expected inventory size
     * @return array of ItemStack containing loaded items
     */
    @Override
    public ItemStack[] loadItems(UUID uuid, int size) {
        ItemStack[] items = new ItemStack[size];
        YamlConfiguration config = loadConfig(uuid);

        if (config.contains("items")) {
            for (int i = 0; i < size; i++) {
                if (config.contains("items." + i)) {
                    items[i] = config.getItemStack("items." + i);
                }
            }
        }

        return items;
    }
}
