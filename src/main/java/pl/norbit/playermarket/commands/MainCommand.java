package pl.norbit.playermarket.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.PermUtils;
import pl.norbit.playermarket.utils.PlayerUtils;

import javax.xml.crypto.Data;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length == 1){
            if(!PermUtils.hasPermission(Settings.MAIN_COMMAND_RELOAD_PERMISSION, sender)){
                sender.sendMessage(ChatUtils.format(Settings.MAIN_COMMAND_NO_PERMISSION));
                return true;
            }

            String arg = args[0];

            if(arg.equalsIgnoreCase("reload")){
                Settings.load(true);
                sender.sendMessage(ChatUtils.format(Settings.MAIN_COMMAND_RELOAD_MESSAGE));
                return true;
            }
        }

        if(args.length == 2){
            String arg = args[0];
            String playerName = args[1];
            if(arg.equalsIgnoreCase("cleardata")){
                if(!PermUtils.hasPermission(Settings.getClearPermission(), sender)){
                    sender.sendMessage(ChatUtils.format(Settings.MAIN_COMMAND_NO_PERMISSION));
                    return true;
                }
                OfflinePlayer offlinePlayer = PlayerUtils.getOfflinePlayer(playerName);

                if (!offlinePlayer.hasPlayedBefore()){
                    sender.sendMessage(ChatUtils.format(Settings.getPlayerNotFound()));
                    return true;
                }

                String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "null";

                DataService.clearPlayerData(offlinePlayer);

                String message = Settings.getClearSuccess()
                        .replace("{PLAYER}", name);

                sender.sendMessage(ChatUtils.format(message));
            }
            return true;
        }

        if(!PermUtils.hasPermission(Settings.MAIN_COMMAND_HELP_PERMISSION, sender)){
            sender.sendMessage(ChatUtils.format(Settings.MAIN_COMMAND_NO_PERMISSION));
            return true;
        }

        Settings.MAIN_COMMAND_HELP_MESSAGE.forEach(message -> sender.sendMessage(ChatUtils.format(message)));
        if(PermUtils.hasPermission(Settings.MAIN_COMMAND_RELOAD_PERMISSION, sender)){
            Settings.MAIN_COMMAND_HELP_RELOAD_MESSAGE.forEach(message -> sender.sendMessage(ChatUtils.format(message)));
            return true;
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length == 1){
            return List.of("reload", "cleardata");
        }

        if(strings.length == 2){
            return PlayerUtils.getOfflineNames();
        }

        return List.of("");
    }
}