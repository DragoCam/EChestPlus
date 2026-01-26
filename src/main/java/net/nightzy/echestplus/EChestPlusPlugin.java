package net.nightzy.echestplus;

import net.nightzy.echestplus.commands.UpgradeCommand;
import net.nightzy.echestplus.listeners.EnderChestListener;
import net.nightzy.echestplus.listeners.UpgradeItemListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class EChestPlusPlugin extends JavaPlugin {

    private DatabaseYML database;
    private EnderChestManager enderChestManager;

    @Override
    public void onEnable() {
        try {
            if (!getDataFolder().exists()) getDataFolder().mkdirs();

            database = new DatabaseYML(getDataFolder());
            enderChestManager = new EnderChestManager(database);

            getServer().getPluginManager().registerEvents(
                    new EnderChestListener(enderChestManager), this
            );
            
            getServer().getPluginManager().registerEvents(
                    new UpgradeItemListener(enderChestManager), this
            );

            getCommand("giveupgrade")
                    .setExecutor(new UpgradeCommand());

            getLogger().info("EChestPlus enabled.");
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
