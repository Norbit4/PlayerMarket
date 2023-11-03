package pl.norbit.playermarket.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.utils.TaskUtils;

public class OnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        TaskUtils.runTaskLaterAsynchronously(() -> DataService.getPlayerDataCreate(p),0L);
    }
}
