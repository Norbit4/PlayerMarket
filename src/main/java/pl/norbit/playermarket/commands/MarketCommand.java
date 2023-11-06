package pl.norbit.playermarket.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.gui.MarketGui;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.PermissionUtils;

public class MarketCommand extends BukkitCommand {

    public MarketCommand() {
        super(Settings.MARKET_COMMAND_NAME);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

        if(!PermissionUtils.hasPermission(Settings.MARKET_COMMAND_PERMISSION, sender)){
            sender.sendMessage(ChatUtils.format(Settings.MARKET_COMMAND_NO_PERMISSION));
            return true;
        }

        new MarketGui((Player) sender, CategoryService.getMain()).open();

        return true;
    }
    public void register(){
        PlayerMarket.getInstance().getServer().getCommandMap().register("", this);
    }
}
