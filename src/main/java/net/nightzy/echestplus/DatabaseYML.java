package net.nightzy.echestplus;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DatabaseYML {

    private final File dataFolder;

    public DatabaseYML(File dataFolder) {
        this.dataFolder = new File(dataFolder, "playerdata");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid + ".yml");
    }

    private YamlConfiguration loadConfig(UUID uuid) {
        File file = getPlayerFile(uuid);
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        return new YamlConfiguration();
    }

    private void saveConfig(UUID uuid, YamlConfiguration config) {
        try {
            config.save(getPlayerFile(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getUpgrades(UUID uuid) {
        return loadConfig(uuid).getInt("upgrades", 0);
    }

    public void setUpgrades(UUID uuid, int upgrades) {
        YamlConfiguration config = loadConfig(uuid);
        config.set("upgrades", upgrades);
        saveConfig(uuid, config);
    }

    public int getSize(UUID uuid) {
        int upgrades = getUpgrades(uuid);
        return 27 + (upgrades * 9);
    }

    public void setSize(UUID uuid, int size) {
        int upgrades = (size - 27) / 9;
        setUpgrades(uuid, Math.max(0, upgrades));
    }

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
