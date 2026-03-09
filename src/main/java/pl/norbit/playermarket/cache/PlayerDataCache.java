package pl.norbit.playermarket.cache;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.utils.TaskUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataCache {
    private static Map<UUID, LocalPlayerData> localDataCache = new ConcurrentHashMap<>();

    private PlayerDataCache() {}

    public static void start(){
        TaskUtils.asyncTimer(() ->{
            Map<UUID, LocalPlayerData> newLocalDataCache = new ConcurrentHashMap<>();
            for (Player onlinePlayer : PlayerMarket.getInstance().getServer().getOnlinePlayers()) {
                DataService.getPlayerLocalData(onlinePlayer)
                        .thenAccept(localPlayerData
                                -> newLocalDataCache.put(onlinePlayer.getUniqueId(), localPlayerData));
            }
            localDataCache = newLocalDataCache;
        }, 20L, 20 * 60 * 3L);
    }

    public static void loadCache(OfflinePlayer p){
       DataService.getPlayerLocalData(p)
               .thenAccept(localPlayerData -> localDataCache.put(p.getUniqueId(), localPlayerData));
    }

    public static void loadCache(PlayerData playerData){
        localDataCache.put(UUID.fromString(playerData.getPlayerUUID()), new LocalPlayerData(playerData));
    }

    public static LocalPlayerData getPlayerData(OfflinePlayer p){
        return localDataCache.get(p.getUniqueId());
    }
}
