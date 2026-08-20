package pl.norbit.playermarket.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.gui.MarketGui;
import pl.norbit.playermarket.service.CategoryService;
import pl.norbit.playermarket.utils.format.ChatUtils;

public class MarketCommand {
    private final PaperCommandManager<CommandSourceStack> commandManager;

    public MarketCommand(PaperCommandManager<CommandSourceStack> commandManager) {
        this.commandManager = commandManager;
    }

    public void register() {
        Command.Builder<CommandSourceStack> commandBuilder = commandManager.commandBuilder(Settings.getMarketCommandPrefix()
        ).handler(context -> {
            CommandSourceStack source = context.sender();

            if (!(source.getSender() instanceof Player p)) {
                return;
            }
            execute(p);
        });

        if(Settings.isMarketCommandPermissionEnabled()){
            commandBuilder = commandBuilder.permission(Settings.getMarketCommandPermission());
        }

        commandManager.command(commandBuilder);
    }

    private void execute(Player p) {
        if (CooldownService.isOnCooldown(p.getUniqueId())) {
            p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
            return;
        }

        CooldownService.updateCooldown(p.getUniqueId());

        new MarketGui(p, CategoryService.getMain()).open();
    }
}
