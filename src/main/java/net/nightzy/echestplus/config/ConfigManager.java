package net.nightzy.echestplus.config;

import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages plugin configuration access.
 * Handles database settings, messages, item definitions, and visual effects.
 */
public class ConfigManager {

    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
    }

    public boolean isUseItemsAdder() {
        return plugin.getConfig().getBoolean("useItemsAdder", false);
    }

    public String getBaseType() {
        return plugin.getConfig().getString("baseType", "YML");
    }

    public String getDatabaseConnectionUri() {
        return plugin.getConfig().getString("databaseConnectionUri");
    }

    public Component getEnderChestName() {
        String raw = plugin.getConfig().getString("enderChestName", "<gradient:#086BFB:#4385FF>Your EnderChest</gradient>");
        return MiniMessage.miniMessage().deserialize(raw);
    }

    public int getEnderChestSize() {
        return plugin.getConfig().getInt("enderChestSize", 27);
    }

    @SuppressWarnings("unchecked")
    public ItemStack getUpgraderItem() {
        if (!plugin.getConfig().isList("upgraderItem")) return null;

        List<?> list = plugin.getConfig().getList("upgraderItem");
        if (list == null || list.isEmpty()) return null;

        Object first = list.get(0);
        if (!(first instanceof Map)) return null;

        Map<String, Object> entry = (Map<String, Object>) first;
        Object itemStackObj = entry.get("itemStack");
        int modelData = entry.containsKey("modelData") ? (int) entry.get("modelData") : 0;

        if (!(itemStackObj instanceof Map)) return null;
        Map<String, Object> itemStackMap = (Map<String, Object>) itemStackObj;

        ItemStack item;

        if (isUseItemsAdder()) {
            CustomStack cs = CustomStack.getInstance("echestplus:upgrader");
            if (cs != null) {
                item = cs.getItemStack();
            } else {
                String materialName = (String) itemStackMap.getOrDefault("material", "NETHER_STAR");
                item = new ItemStack(Material.valueOf(materialName));
            }
        } else {
            String materialName = (String) itemStackMap.getOrDefault("material", "NETHER_STAR");
            Material mat = Material.matchMaterial(materialName);
            item = new ItemStack(mat != null ? mat : Material.NETHER_STAR);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Object metaObj = itemStackMap.get("meta");
            if (metaObj instanceof Map) {
                Map<String, Object> metaMap = (Map<String, Object>) metaObj;

                String display = (String) metaMap.get("display");
                if (display != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', display));
                }

                Object loreObj = metaMap.get("lore");
                if (loreObj instanceof List) {
                    List<String> loreLines = new ArrayList<>();
                    for (Object line : (List<?>) loreObj) {
                        loreLines.add(ChatColor.translateAlternateColorCodes('&', String.valueOf(line)));
                    }
                    meta.setLore(loreLines);
                }

                Object enchObj = metaMap.get("enchantments");
                if (enchObj instanceof Map) {
                    Map<String, Object> enchMap = (Map<String, Object>) enchObj;
                    for (Map.Entry<String, Object> e : enchMap.entrySet()) {
                        Enchantment enchant = Enchantment.getByName(e.getKey().toUpperCase());
                        if (enchant != null) {
                            int lvl = Integer.parseInt(String.valueOf(e.getValue()));
                            meta.addEnchant(enchant, lvl, true);
                        }
                    }
                }

                Object flagsObj = metaMap.get("flags");
                if (flagsObj instanceof List) {
                    for (Object f : (List<?>) flagsObj) {
                        try {
                            meta.addItemFlags(ItemFlag.valueOf(String.valueOf(f).toUpperCase()));
                        } catch (Exception ignored) {}
                    }
                }
            }

            if (!isUseItemsAdder() && modelData > 0) {
                meta.setCustomModelData(modelData);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public void sendMessage(Player player, String sectionPath, Map<String, String> placeholders) {
        String rawMessage = plugin.getConfig().getString(sectionPath + ".message", "Message not found");
        String type = getMessageType(sectionPath);

        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                rawMessage = rawMessage.replace("<" + e.getKey() + ">", e.getValue());
            }
        }

        MiniMessage mm = MiniMessage.miniMessage();

        switch (type.toUpperCase()) {
            case "TITLE_SUBTITLE":
                String[] split = rawMessage.split("\n", 2);
                Component title = mm.deserialize(split[0]);
                Component subtitle = (split.length > 1) ? mm.deserialize(split[1]) : Component.empty();
                player.showTitle(Title.title(title, subtitle));
                break;
            case "ACTIONBAR":
                player.sendActionBar(mm.deserialize(rawMessage));
                break;
            case "CHAT":
            default:
                player.sendMessage(mm.deserialize(rawMessage));
                break;
        }
    }

    public String getMessageType(String sectionPath) {
        return plugin.getConfig().getString(sectionPath + ".messageType", "CHAT");
    }

    public Component getMessageAsComponent(String path, Map<String, String> placeholders) {
        String raw = plugin.getConfig().getString(path, "Message not found");
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                raw = raw.replace("<" + e.getKey() + ">", e.getValue());
            }
        }
        return MiniMessage.miniMessage().deserialize(raw);
    }

    public Component getAdminMessage(String messagePath, String placeholder, String value) {
        String raw = plugin.getConfig().getString("adminMessages." + messagePath, "Message not found");
        if (placeholder != null && value != null) {
            raw = raw.replace("<" + placeholder + ">", value);
        }
        return MiniMessage.miniMessage().deserialize(raw);
    }
}