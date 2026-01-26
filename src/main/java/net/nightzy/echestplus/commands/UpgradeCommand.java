package net.nightzy.echestplus.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UpgradeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        ItemStack upgrade = new ItemStack(Material.NETHER_STAR, 1);
        ItemMeta meta = upgrade.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bEnder Chest Upgrade");
            meta.setCustomModelData(999);
            upgrade.setItemMeta(meta);
        }

        player.getInventory().addItem(upgrade);
        player.sendMessage("§aOtrzymałeś upgrade Ender Chest!");
        return true;
    }
}
