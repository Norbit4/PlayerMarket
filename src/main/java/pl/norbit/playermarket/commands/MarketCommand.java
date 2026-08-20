package pl.norbit.playermarket.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.gui.MarketGui;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.utils.format.ChatUtils;

public class MarketCommand {

    public void register(Commands registrar) {
        LiteralCommandNode<CommandSourceStack> commandNode = Commands.literal(Settings.getMarketCommandPrefix())
                .requires(source -> {
                    if (!Settings.isMarketCommandPermissionEnabled()) {
                        return true;
                    }
                    return source.getSender().hasPermission(Settings.getMarketCommandPermission());
                })
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player p)) {
                        return 0;
                    }
                    return execute(p);
                })
                .build();
        registrar.register(commandNode, "Open player market");
    }

    private int execute(Player p) {
        if (CooldownService.isOnCooldown(p.getUniqueId())) {
            p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
            return 0;
        }

        CooldownService.updateCooldown(p.getUniqueId());

        new MarketGui(p, CategoryService.getMain()).open();
        return 1;
    }
}