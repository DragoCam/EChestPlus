package net.nightzy.echestplus.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility class for serializing and deserializing ItemStack arrays.
 * Used for storing ender chest contents in SQL or YML storage.
 */
public final class ItemSerializer {

    // Prevent instantiation
    private ItemSerializer() {}

    /**
     * Serializes an array of ItemStacks into a byte array.
     *
     * @param items array of ItemStacks to serialize
     * @return byte array representation of the items
     * @throws IOException if serialization fails
     */
    public static byte[] toBytes(ItemStack[] items) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos)) {

            // Write the length first
            boos.writeInt(items.length);

            // Write each ItemStack object
            for (ItemStack is : items) {
                boos.writeObject(is);
            }

            boos.flush();
            return bos.toByteArray();
        }
    }

    /**
     * Deserializes a byte array into an array of ItemStacks.
     * Returns an array with the specified size, filling only available items.
     *
     * @param data serialized byte array
     * @param size expected size of the resulting ItemStack array
     * @return deserialized ItemStack array
     * @throws IOException if deserialization fails
     * @throws ClassNotFoundException if an object in the stream is not an ItemStack
     */
    public static ItemStack[] fromBytes(byte[] data, int size) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bis)) {

            int len = bois.readInt();
            ItemStack[] items = new ItemStack[size];

            // Read only up to the expected size
            for (int i = 0; i < Math.min(len, size); i++) {
                Object o = bois.readObject();
                if (o instanceof ItemStack) items[i] = (ItemStack) o;
            }

            return items;
        }
    }
}
