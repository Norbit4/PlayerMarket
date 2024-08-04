package pl.norbit.playermarket.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.DoubleFormatter;

import static pl.norbit.playermarket.utils.TaskUtils.async;

public class OnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        async(() -> {
            PlayerData playerData = DataService.getPlayerDataCreate(p);

            int soldItems = playerData.getSoldItems();

            if(soldItems > 0){
                String joinMessage = Settings.JOIN_MESSAGE
                        .replace("{MONEY}", DoubleFormatter.format(playerData.getEarnedMoney()))
                        .replace("{SOLD}", String.valueOf(playerData.getSoldItems()));

                p.sendMessage(ChatUtils.format(p, joinMessage));
            }
        });
    }
}
