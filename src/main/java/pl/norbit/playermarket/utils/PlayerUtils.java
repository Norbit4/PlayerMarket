package pl.norbit.playermarket.utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.norbit.playermarket.PlayerMarket;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerUtils {
    private PlayerUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Optional<Player> getPlayer(UUID playerUUID) {
        return Optional.ofNullable(PlayerMarket.getInstance().getServer().getPlayer(playerUUID));
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        return PlayerMarket.getInstance().getServer().getOfflinePlayer(name);
    }

    public static List<String> getOfflineNames() {
        return Arrays.stream(PlayerMarket.getInstance().getServer().getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .collect(Collectors.toList());
    }


    public static boolean isInventoryFull(Player p) {
        Inventory inventory = p.getInventory();
        return inventory.firstEmpty() == -1;
    }
}
