package pl.norbit.playermarket.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.gui.MarketGui;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.player.PermUtils;

public class MarketCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!PermUtils.hasPermission(Settings.MARKET_COMMAND_PERMISSION, sender)){
            sender.sendMessage(ChatUtils.format(Settings.MARKET_COMMAND_NO_PERMISSION));
            return true;
        }
        new MarketGui((Player) sender, CategoryService.getMain()).open();

        return true;
    }
}
