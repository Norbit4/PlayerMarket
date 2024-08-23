package pl.norbit.playermarket.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.norbit.playermarket.PlayerMarket;

import java.util.Optional;
import java.util.UUID;

public class PlayerUtils {
    private PlayerUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Optional<Player> getPlayer(UUID playerUUID) {
        return Optional.ofNullable(PlayerMarket.getInstance().getServer().getPlayer(playerUUID));
    }

    public static boolean isInventoryFull(Player p) {
        Inventory inventory = p.getInventory();
        return inventory.firstEmpty() == -1;
    }
}
