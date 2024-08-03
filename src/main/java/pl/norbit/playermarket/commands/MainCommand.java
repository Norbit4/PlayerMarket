package pl.norbit.playermarket.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.PermUtils;

import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player p = (Player) sender;

        if(args.length == 1){
            if(!PermUtils.hasPermission(Settings.MAIN_COMMAND_RELOAD_PERMISSION, sender)){
                sender.sendMessage(ChatUtils.format(Settings.MAIN_COMMAND_NO_PERMISSION));
                return true;
            }

            String arg = args[0];

            if(arg.equalsIgnoreCase("reload")){
                Settings.load(true);
                p.sendMessage(ChatUtils.format(Settings.MAIN_COMMAND_RELOAD_MESSAGE));
                return true;
            }
        }

        if(!PermUtils.hasPermission(Settings.MAIN_COMMAND_HELP_PERMISSION, sender)){
            sender.sendMessage(ChatUtils.format(Settings.MAIN_COMMAND_NO_PERMISSION));
            return true;
        }

        Settings.MAIN_COMMAND_HELP_MESSAGE.forEach(message -> p.sendMessage(ChatUtils.format(message)));
        if(PermUtils.hasPermission(Settings.MAIN_COMMAND_RELOAD_PERMISSION, sender)){
            Settings.MAIN_COMMAND_HELP_RELOAD_MESSAGE.forEach(message -> p.sendMessage(ChatUtils.format(message)));
            return true;
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of("reload");
    }
}