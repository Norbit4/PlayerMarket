package pl.norbit.playermarket.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.norbit.playermarket.cache.PlayerDataCache;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import static pl.norbit.playermarket.utils.TaskUtils.asyncLater;
import static pl.norbit.playermarket.utils.TaskUtils.sync;

public class OnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        join(p);
    }

    private void join(Player p){
        asyncLater(() -> {
            if(!p.isOnline()){
                return;
            }
            if(!DataService.isReady()){
                join(p);
                return;
            }
            DataService.getPlayerDataCreate(p).thenAccept(playerData -> {
                PlayerDataCache.loadCache(playerData);

                int soldItems = playerData.getSoldItems();

                if(soldItems > 0){
                    if(!p.isOnline()){
                        return;
                    }

                    String joinMessage = Settings.JOIN_MESSAGE
                            .replace("{MONEY}", DoubleFormatter.format(playerData.getEarnedMoney()))
                            .replace("{SOLD}", String.valueOf(playerData.getSoldItems()));

                    p.sendMessage(ChatUtils.format(p, joinMessage));
                }
            });
        }, 30L);
    }
}
