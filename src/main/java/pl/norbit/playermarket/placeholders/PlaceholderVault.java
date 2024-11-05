package pl.norbit.playermarket.placeholders;

import org.bukkit.OfflinePlayer;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.local.LocalPlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static pl.norbit.playermarket.utils.TaskUtils.asyncTimer;

public class PlaceholderVault {
    private static final Map<UUID, LocalPlayerData> PLAYER_DATA = new HashMap<>();

    private PlaceholderVault() {
        throw new IllegalStateException("Utility class");
    }
    public static void start(){
        asyncTimer(PLAYER_DATA::clear, 0L, 60L);
    }

    public static LocalPlayerData getLocalPlayerData(OfflinePlayer player) {
        LocalPlayerData localPlayerData = PLAYER_DATA.get(player.getUniqueId());

        if (localPlayerData == null) {
            LocalPlayerData pLocalData = DataService.getPlayerLocalData(player);

            PLAYER_DATA.put(player.getUniqueId(), pLocalData);
            return pLocalData;
        }

        return localPlayerData;
    }
}
