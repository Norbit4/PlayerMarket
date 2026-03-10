package pl.norbit.playermarket.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.norbit.playermarket.cooldown.CooldownService;

public class OnPlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        CooldownService.clearCooldown(p.getUniqueId());
    }
}
