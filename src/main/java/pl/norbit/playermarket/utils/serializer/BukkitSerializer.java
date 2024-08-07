package pl.norbit.playermarket.utils.serializer;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import pl.norbit.playermarket.exception.BukkitSerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BukkitSerializer {

    private BukkitSerializer() {
        throw new IllegalStateException("Utility class");
    }

    public static byte[] serializeItems(ItemStack itemStacks) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream objectStream = new BukkitObjectOutputStream(stream)) {
                objectStream.writeObject(itemStacks);
            }
            return stream.toByteArray();
        } catch (IOException e) {
            throw new BukkitSerializationException("Could not serialize itemstacks", e);
        }
    }

    public static ItemStack deserializeItems(byte[] itemStacks) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(itemStacks)) {
            try (BukkitObjectInputStream objectStream = new BukkitObjectInputStream(stream)) {
                return (ItemStack) objectStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new BukkitSerializationException("Could not deserialize itemstacks", e);
        }
    }
}
