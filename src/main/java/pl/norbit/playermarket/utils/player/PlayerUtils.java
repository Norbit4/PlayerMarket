package pl.norbit.playermarket.utils.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.norbit.playermarket.PlayerMarket;

import java.util.*;

public class PlayerUtils {
    private PlayerUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Player getPlayer(UUID playerUUID) {
        return PlayerMarket.getInstance().getServer().getPlayer(playerUUID);
    }

    public static OfflinePlayer getOfflinePlayer(String name) {
        return PlayerMarket.getInstance().getServer().getOfflinePlayer(name);
    }

    public static List<String> getOnlineNames() {
        return PlayerMarket.getInstance().getServer().getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .toList();
    }


    public static boolean isInventoryFull(Player p) {
        Inventory inventory = p.getInventory();
        return inventory.firstEmpty() == -1;
    }
}
