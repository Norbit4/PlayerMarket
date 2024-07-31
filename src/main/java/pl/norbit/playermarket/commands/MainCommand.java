package pl.norbit.playermarket.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.gui.anvil.ItemTypeSearchGui;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.PermUtils;

import java.util.List;

public class MainCommand extends BukkitCommand {

    public MainCommand() {
        super("playermarket");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!PermUtils.hasPermission(Settings.MAIN_COMMAND_PERMISSION, sender)){
            sender.sendMessage(ChatUtils.format(Settings.MAIN_COMMAND_NO_PERMISSION));
            return true;
        }

        Player p = (Player) sender;

        if(args.length == 1){
            String arg = args[0];

            if(arg.equalsIgnoreCase("reload")){
                Settings.load(true);
                p.sendMessage(ChatUtils.format(Settings.MAIN_COMMAND_RELOAD_MESSAGE));
                return true;
            }
        }

        Settings.MAIN_COMMAND_HELP_MESSAGE.forEach(message -> p.sendMessage(format(message)));
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return List.of("reload", "help");
    }

    private String format(String message){
        return ChatUtils.format(message)
                .replace("{CMD_MARKET}",Settings.MARKET_COMMAND_NAME)
                .replace("{CMD_OFFER}",Settings.OFFER_COMMAND_NAME);

    }
    public void register(){
        PlayerMarket.getInstance().getServer().getCommandMap().register("", this);
    }
}