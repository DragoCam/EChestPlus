package net.nightzy.echestplus.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages plugin configuration access.
 * Handles database settings, messages and item definitions.
 */
public class ConfigManager {

    // ============================================================
    // Fields
    // ============================================================

    private final JavaPlugin plugin; // Plugin instance for config access

    // ============================================================
    // Constructor
    // ============================================================

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
    }

    // ============================================================
    // Database & General Settings
    // ============================================================

    public String getBaseType() {
        return plugin.getConfig().getString("baseType", "SQLITE");
    }

    public String getDatabaseConnectionUri() {
        return plugin.getConfig().getString("databaseConnectionUri", "");
    }

    public boolean getMigrateStatus() {
        return plugin.getConfig().getBoolean("migrateStatus", false);
    }

    // ============================================================
    // Ender Chest Settings
    // ============================================================

    /**
     * Returns the display name of the ender chest.
     */
    public Component getEnderChestName() {
        String raw = plugin.getConfig().getString("enderChestName", "Ender Chest");
        return MiniMessage.miniMessage().deserialize(raw);
    }

    public int getEnderChestSize() {
        return plugin.getConfig().getInt("enderChestSize", 27);
    }

    public int getOpenCooldown() {
        return plugin.getConfig().getInt("openCooldown", 3);
    }

    // ============================================================
    // Upgrader Item
    // ============================================================

    /**
     * Builds the upgrader item from config definition.
     * Returns null if configuration is invalid or missing.
     */
    @SuppressWarnings("unchecked")
    public ItemStack getUpgraderItem() {

        // Validate base structure
        if (!plugin.getConfig().isList("upgraderItem")) return null;

        Object first = plugin.getConfig().getList("upgraderItem").get(0);
        if (!(first instanceof Map)) return null;

        Map<String, Object> entry = (Map<String, Object>) first;
        Object itemStackObj = entry.get("itemStack");
        int modelData = entry.containsKey("modelData") ? (int) entry.get("modelData") : 0;

        if (!(itemStackObj instanceof Map)) return null;

        // Resolve material
        Map<String, Object> itemStackMap = (Map<String, Object>) itemStackObj;
        String materialName = (String) itemStackMap.getOrDefault("material", "PAPER");
        Material mat = Material.matchMaterial(materialName);
        if (mat == null) mat = Material.PAPER;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            Object metaObj = itemStackMap.get("meta");
            if (metaObj instanceof Map) {
                Map<String, Object> metaMap = (Map<String, Object>) metaObj;

                // Display name
                String display = (String) metaMap.get("display");
                if (display != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', display));
                }

                // Lore
                Object loreObj = metaMap.get("lore");
                if (loreObj instanceof List) {
                    List<String> lore = new ArrayList<>();
                    for (Object l : (List<?>) loreObj) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', String.valueOf(l)));
                    }
                    meta.setLore(lore);
                }

                // Enchantments
                Object enchObj = metaMap.get("enchantments");
                if (enchObj instanceof Map) {
                    Map<String, Object> enchMap = (Map<String, Object>) enchObj;
                    for (Map.Entry<String, Object> e : enchMap.entrySet()) {
                        String enchName = e.getKey();
                        int lvl = Integer.parseInt(String.valueOf(e.getValue()));
                        Enchantment enchant = Enchantment.getByName(enchName);
                        if (enchant != null) {
                            meta.addEnchant(enchant, lvl, true);
                        }
                    }
                }

                // Item flags
                Object flagsObj = metaMap.get("flags");
                if (flagsObj instanceof List) {
                    for (Object f : (List<?>) flagsObj) {
                        try {
                            ItemFlag flag = ItemFlag.valueOf(String.valueOf(f));
                            meta.addItemFlags(flag);
                        } catch (Exception ignored) {
                            // Ignore invalid flags
                        }
                    }
                }
            }

            // Custom model data
            if (modelData > 0) {
                meta.setCustomModelData(modelData);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    // ============================================================
    // Message Utilities
    // ============================================================

    /**
     * Returns a configured message as a Component.
     */
    public Component getMessageAsComponent(String path, String placeholder, String value) {

        String raw = plugin.getConfig().getString(path, "Message not found");

        if (placeholder != null && value != null) {
            raw = raw.replace("<" + placeholder + ">", value);
        }

        return MiniMessage.miniMessage().deserialize(raw);
    }

    /**
     * Returns the message delivery type for a message section.
     */
    public String getMessageType(String sectionPath) {
        return plugin.getConfig().getString(sectionPath + ".messageType", "CHAT");
    }

    /**
     * Sends a configured message to the player.
     * Supports CHAT, ACTION_BAR and TITLE_SUBTITLE.
     */
    public void sendMessage(org.bukkit.entity.Player player,
                            String sectionPath,
                            String placeholder,
                            String value) {

        String messageType = getMessageType(sectionPath);
        String rawMessage = plugin.getConfig().getString(sectionPath + ".message", "");

        if (placeholder != null && value != null) {
            rawMessage = rawMessage.replace("<" + placeholder + ">", value);
        }

        switch (messageType.toUpperCase()) {

            case "TITLE_SUBTITLE":
                String[] parts = rawMessage.split("\\\\n", 2);
                Component title = MiniMessage.miniMessage()
                        .deserialize(parts.length > 0 ? parts[0] : "");
                Component subtitle = MiniMessage.miniMessage()
                        .deserialize(parts.length > 1 ? parts[1] : "");
                player.showTitle(net.kyori.adventure.title.Title.title(title, subtitle));
                break;

            case "ACTION_BAR":
                Component actionBar = MiniMessage.miniMessage().deserialize(rawMessage);
                player.sendActionBar(actionBar);
                break;

            case "CHAT":
            default:
                Component chat = MiniMessage.miniMessage().deserialize(rawMessage);
                player.sendMessage(chat);
                break;
        }
    }

    /**
     * Returns an admin message as a Component.
     */
    public Component getAdminMessage(String messagePath, String placeholder, String value) {

        String raw = plugin.getConfig()
                .getString("adminMessages." + messagePath, "Message not found");

        if (placeholder != null && value != null) {
            raw = raw.replace("<" + placeholder + ">", value);
        }

        return MiniMessage.miniMessage().deserialize(raw);
    }
}
