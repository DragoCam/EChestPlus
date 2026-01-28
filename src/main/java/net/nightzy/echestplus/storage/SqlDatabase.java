package net.nightzy.echestplus.storage;

import net.nightzy.echestplus.util.ItemSerializer;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.*;
import java.util.UUID;

/**
 * SQL-based implementation of DatabaseProvider.
 * Stores player upgrades and ender chest contents using a JDBC database.
 */
public class SqlDatabase implements DatabaseProvider {

    // JDBC connection to the database
    private final Connection connection;

    /**
     * Initializes the SQL database connection and ensures the players table exists.
     *
     * @param jdbcUri JDBC connection string
     * @throws SQLException if connection or table creation fails
     */
    public SqlDatabase(String jdbcUri) throws SQLException {
        this.connection = DriverManager.getConnection(jdbcUri);
        init();
    }

    /**
     * Creates the players table if it does not exist.
     *
     * Columns:
     * uuid    - primary key for player
     * upgrades - number of upgrades
     * items   - serialized ItemStack array as BLOB
     *
     * @throws SQLException if table creation fails
     */
    private void init() throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, upgrades INTEGER, items BLOB)");
        }
    }

    /**
     * Returns the number of upgrades a player has stored in the database.
     *
     * @param uuid player's UUID
     * @return upgrades count
     */
    @Override
    public int getUpgrades(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT upgrades FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("upgrades");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Sets the number of upgrades for a player in the database.
     * Inserts new record if missing, otherwise updates existing.
     *
     * @param uuid player's UUID
     * @param upgrades number of upgrades
     */
    @Override
    public void setUpgrades(UUID uuid, int upgrades) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO players(uuid, upgrades) VALUES(?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET upgrades = ?")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, upgrades);
            ps.setInt(3, upgrades);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the ender chest size based on the player's upgrades.
     *
     * @param uuid player's UUID
     * @return chest size (27 + upgrades * 9)
     */
    @Override
    public int getSize(UUID uuid) {
        int upgrades = getUpgrades(uuid);
        return 27 + (upgrades * 9);
    }

    /**
     * Updates player's upgrades based on a new chest size.
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
     * Saves player's ender chest items as a serialized BLOB in the database.
     *
     * @param uuid player's UUID
     * @param items ItemStack array to save
     */
    @Override
    public void saveItems(UUID uuid, ItemStack[] items) {
        try {
            byte[] data = ItemSerializer.toBytes(items);
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO players(uuid, items) VALUES(?, ?) " +
                            "ON CONFLICT(uuid) DO UPDATE SET items = ?")) {
                ps.setString(1, uuid.toString());
                ps.setBytes(2, data);
                ps.setBytes(3, data);
                ps.executeUpdate();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a player's ender chest items from the database.
     * Returns an empty array if no items exist or on failure.
     *
     * @param uuid player's UUID
     * @param size expected inventory size
     * @return ItemStack array loaded from database
     */
    @Override
    public ItemStack[] loadItems(UUID uuid, int size) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT items FROM players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] data = rs.getBytes("items");
                    if (data != null) {
                        try {
                            return ItemSerializer.fromBytes(data, size);
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ItemStack[size];
    }
}
