package pl.norbit.playermarket.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.player.PlayerUtils;

public class MainCommand {

    public void register(Commands registrar) {
        LiteralArgumentBuilder<CommandSourceStack> builder =
                Commands.literal("playermarket")
                        .requires(source ->
                                source.getSender()
                                        .hasPermission("playermarket.command.main.help")
                        )
                        .executes(ctx -> this.help(ctx.getSource()))
                        .then(Commands.literal("reload")
                                    .requires(source ->
                                            source.getSender()
                                                    .hasPermission("playermarket.command.main.reload"))
                                    .executes(ctx -> this.reload(ctx.getSource())))
                        .then(Commands.literal("clear")
                                    .requires(source -> source.getSender()
                                            .hasPermission("playermarket.command.main.clear"))
                                    .then(Commands.argument("player", StringArgumentType.word())
                                            .suggests((ctx, suggestions) -> {
                                                for (String name : PlayerUtils.getOnlineNames()) {
                                                    suggestions.suggest(name);
                                                }
                                                return suggestions.buildFuture();
                                            })
                                            .executes(ctx -> {
                                                String playerName = StringArgumentType.getString(ctx, "player");

                                                return this.clear(
                                                        ctx.getSource(),
                                                        playerName
                                                );
                                            })
                                    )
                        );

        registrar.register(builder.build(), "PlayerMarket main command");
    }

    private int help(CommandSourceStack source) {
        CommandSender sender = source.getSender();

        Settings.getMainCommandHelpMessage().forEach(
                message -> sender.sendMessage(ChatUtils.format(message))
        );

        if (sender.hasPermission("playermarket.command.main.reload")) {
            Settings.getMainCommandReloadInfo().forEach(
                    message -> sender.sendMessage(ChatUtils.format(message))
            );
        }

        return 1;
    }

    private int reload(CommandSourceStack source) {
        Settings.load(true);

        source.getSender().sendMessage(ChatUtils.format(Settings.getMainCommandReloadMessage()));
        return 1;
    }

    private int clear(CommandSourceStack source, String playerName) {
        OfflinePlayer offlinePlayer = PlayerUtils.getOfflinePlayer(playerName);
        CommandSender sender = source.getSender();

        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(ChatUtils.format(Settings.getPlayerNotFound()));
            return 0;
        }

        String name = offlinePlayer.getName() != null
                ? offlinePlayer.getName()
                : "null";

        DataService.clearPlayerData(offlinePlayer);

        String message = Settings.getClearSuccess()
                .replace("{player}", name);

        sender.sendMessage(ChatUtils.format(message));
        return 1;
    }
}