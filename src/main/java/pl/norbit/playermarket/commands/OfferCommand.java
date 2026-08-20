package pl.norbit.playermarket.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.DoubleParser;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.logs.LogService;
import pl.norbit.playermarket.plugins.PluginHook;
import pl.norbit.playermarket.utils.BlackListUtils;
import pl.norbit.playermarket.utils.economy.EconomyUtils;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.player.PermUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static pl.norbit.playermarket.utils.TaskUtils.sync;

public class OfferCommand {
    private final PaperCommandManager<CommandSourceStack> commandManager;
    private final Map<UUID, ItemStack> itemsBackup = new HashMap<>();

    public OfferCommand(PaperCommandManager<CommandSourceStack> commandManager) {
        this.commandManager = commandManager;
    }

    public void register() {
        String commandName = Settings.getOfferCommandPrefix();
        String argumentName = Settings.getOfferCommandArgumentName();

        // /offer
        Command.Builder<CommandSourceStack> builderNoArg = commandManager.commandBuilder(commandName)
                .handler(context -> {
                    CommandSourceStack source = context.sender();

                    if (!(source.getSender() instanceof Player p)) {
                        return;
                    }
                    p.sendMessage(ChatUtils.format(Settings.getOfferCommandUsage()));
                });

        if(Settings.isOfferCommandPermissionEnabled()){
            builderNoArg = builderNoArg.permission(Settings.getOfferCommandPermission());
        }

        commandManager.command(builderNoArg);

        // /offer <price>
        Command.Builder<CommandSourceStack> builderArg = commandManager.commandBuilder(commandName)
                .required(argumentName, DoubleParser.doubleParser())
                .handler(context -> {
                    if (!(context.sender().getSender() instanceof Player p)) {
                        return;
                    }
                    double price = context.get(argumentName);

                    execute(p, price);
                });

        if(Settings.isOfferCommandPermissionEnabled()){
            builderArg = builderArg.permission(Settings.getOfferCommandPermission());
        }

        commandManager.command(builderArg);
    }

    private void execute(Player p, double price) {
        if (EconomyUtils.getPluginHook() == PluginHook.PLAYER_POINTS && price != (int) price) {
            p.sendMessage(ChatUtils.format(Settings.getOfferCommandWrongPrice()));
            return;
        }

        //check price
        if (price <= 0 || price > 99999999) {
            p.sendMessage(ChatUtils.format(Settings.getOfferCommandWrongPrice()));
            return;
        }

        //check cooldown
        if (CooldownService.isOnCooldown(p.getUniqueId())) {
            p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
            return;
        }
        CooldownService.updateCooldown(p.getUniqueId());

        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();

        //check item is not air
        if (itemInMainHand.getType().isAir()) {
            p.sendMessage(ChatUtils.format(Settings.getOfferCommandWrongItem()));
            return;
        }

        //check item is not blacklisted
        if (BlackListUtils.isBlackListed(itemInMainHand)) {
            p.sendMessage(ChatUtils.format(Settings.getBlacklistMessage()));
            return;
        }

        itemsBackup.put(p.getUniqueId(), itemInMainHand.clone());
        p.getInventory().setItemInMainHand(null);

        DataService.getPlayerData(p.getUniqueId().toString()).thenAccept(playerData -> {
            if (playerData == null) {
                return;
            }

            if (Settings.isOfferCommandLimitEnabled()) {
                int amount = PermUtils.getAmount(
                        p,
                        Settings.getOfferCommandLimitPermission(),
                        Settings.getOfferCommandDefaultLimit()
                );

                if (playerData.getPlayerOffers().size() >= amount) {
                    backupItem(p);
                    p.sendMessage(ChatUtils.format(Settings.getOfferCommandLimitMessage()));
                    return;
                }
            }

            DataService.addItemToOffer(p, itemInMainHand, price);

            p.sendMessage(ChatUtils.format(Settings.getOfferCommandSuccess()));

            LogService.log("Player " + p.getName() +
                            " offer item " + itemInMainHand.getType() +
                            " x" + itemInMainHand.getAmount() +
                            " - " + price
            );
        });
    }

    private void backupItem(Player p){
        if(!p.isOnline()){
            return;
        }
        ItemStack itemStack = itemsBackup.get(p.getUniqueId());

        if(itemStack != null){
            sync(() -> p.getInventory().setItemInMainHand(itemStack));
        }
    }
}
