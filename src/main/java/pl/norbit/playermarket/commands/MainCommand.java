package pl.norbit.playermarket.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.player.PlayerUtils;

import java.util.List;

public class MainCommand {

    @Command("playermarket")
    @Permission("playermarket.command.main.help")
    public void help(CommandSourceStack source) {
        CommandSender sender = source.getSender();

        Settings.getMainCommandHelpMessage().forEach(
                message -> sender.sendMessage(ChatUtils.format(message))
        );

        if (sender.hasPermission("playermarket.command.main.reload")) {
            Settings.getMainCommandReloadInfo().forEach(
                    message -> sender.sendMessage(ChatUtils.format(message))
            );
        }
    }

    @Command("playermarket reload")
    @Permission("playermarket.command.main.reload")
    public void reload(CommandSourceStack source) {
        Settings.load(true);

        source.getSender().sendMessage(ChatUtils.format(Settings.getMainCommandReloadMessage())
        );
    }

    @Suggestions("players")
    public List<String> players() {
        return PlayerUtils.getOnlineNames();
    }

    @Command("playermarket clear <player>")
    @Permission("playermarket.command.main.clear")
    public void clear(CommandSourceStack source,
                      @Argument(value = "player", suggestions = "players") String playerName) {

        OfflinePlayer offlinePlayer = PlayerUtils.getOfflinePlayer(playerName);

        CommandSender commandSender = source.getSender();

        if (!offlinePlayer.hasPlayedBefore()) {
            commandSender.sendMessage(ChatUtils.format(Settings.getPlayerNotFound()));
            return;
        }

        String name = offlinePlayer.getName() != null
                ? offlinePlayer.getName()
                : "null";

        DataService.clearPlayerData(offlinePlayer);

        String message = Settings.getClearSuccess()
                .replace("{PLAYER}", name);

        commandSender.sendMessage(ChatUtils.format(message));
    }
}