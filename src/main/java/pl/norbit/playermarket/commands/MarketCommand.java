package pl.norbit.playermarket.commands;

import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.gui.MarketGui;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.player.PermUtils;

public class MarketCommand {

    public static void register(Commands registrar) {
        MarketCommand command = new MarketCommand();

        registrar.register(
                Commands.literal(Settings.getMarketCommandPrefix())
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                return 0;
                            }

                            return command.execute(player);
                        })
                        .build(),
                "Open player market"
        );
    }

    private int execute(Player p) {
        if (!PermUtils.hasPermission(Settings.getMarketCommandPermission(), p, Settings.isMarketCommandPermissionEnabled())) {
            p.sendMessage(ChatUtils.format(Settings.getMarketCommandNoPermission()));
            return 0;
        }

        if (CooldownService.isOnCooldown(p.getUniqueId())) {
            p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
            return 0;
        }

        CooldownService.updateCooldown(p.getUniqueId());

        new MarketGui(p, CategoryService.getMain()).open();

        return 0;
    }
}
