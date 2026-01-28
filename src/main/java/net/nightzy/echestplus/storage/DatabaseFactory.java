package net.nightzy.echestplus.storage;

import net.nightzy.echestplus.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Factory class responsible for creating the appropriate
 * DatabaseProvider implementation based on configuration.
 */
public class DatabaseFactory {

    /**
     * Creates a DatabaseProvider instance depending on the configured
     * storage backend type.
     *
     * @param plugin plugin instance (used for logging and data folder access)
     * @param config configuration manager
     * @return initialized DatabaseProvider implementation
     */
    public static DatabaseProvider create(JavaPlugin plugin, ConfigManager config) {

        // Database backend type defined in config (e.g. YML, SQLITE, MYSQL)
        String baseType = config.getBaseType();

        // Connection URI used by SQL-based providers
        String uri = config.getDatabaseConnectionUri();

        // Fallback to YML storage if no base type is defined
        if (baseType == null) baseType = "YML";

        // Select backend implementation based on configured type
        switch (baseType.toUpperCase()) {

            case "SQLITE":
            case "MYSQL":
                try {
                    // SQLite and MySQL are handled by the same JDBC-backed provider
                    return new SqlDatabase(uri);
                } catch (Exception e) {
                    // Log initialization failure and fall back to YML storage
                    plugin.getLogger().warning("Failed to initialize SQL backend: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }

            case "MONGODB":
                // MongoDB support is not implemented yet
                plugin.getLogger().warning("MongoDB backend is not implemented. Falling back to YML.");
                break;

            default:
                // Any unknown or unsupported type is treated as YML
                break;
        }

        // Default fallback: file-based YML storage
        return new DatabaseYML(plugin.getDataFolder());
    }
}
