package pl.norbit.playermarket.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.logs.LogService;
import pl.norbit.playermarket.utils.BlackListUtils;
import pl.norbit.playermarket.utils.economy.EconomyType;
import pl.norbit.playermarket.utils.economy.EconomyUtils;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.player.PermUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static pl.norbit.playermarket.utils.TaskUtils.sync;

public class OfferCommand {
    private final Map<UUID, ItemStack> itemsBackup = new HashMap<>();

    public static void register(Commands registrar) {
        OfferCommand command = new OfferCommand();

        registrar.register(
                Commands.literal(Settings.getOfferCommandPrefix())
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                return 0;
                            }

                            player.sendMessage(ChatUtils.format(Settings.getOfferCommandUsage()));
                            return 0;
                        })
                        .then(Commands.argument(Settings.getOfferCommandArgumentName(), DoubleArgumentType.doubleArg(0.01))
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getSender() instanceof Player player)) {
                                        return 0;
                                    }

                                    return command.execute(
                                            player,
                                            DoubleArgumentType.getDouble(ctx, Settings.getOfferCommandArgumentName())
                                    );
                                }))
                        .build(),
                "Offer an item"
        );
    }
    private int execute(Player p, double price) {
        if (!PermUtils.hasPermission(Settings.getOfferCommandPermission(), p, Settings.isOfferCommandPermissionEnabled())) {
            p.sendMessage(ChatUtils.format(Settings.getOfferCommandNoPermission()));
            return 0;
        }

        if (EconomyUtils.getEconomyType() == EconomyType.PLAYER_POINTS && price != (int) price) {
            p.sendMessage(ChatUtils.format(Settings.getOfferCommandWrongPrice()));
            return 0;
        }

        //check price
        if (price <= 0 || price > 99999999) {
            p.sendMessage(ChatUtils.format(Settings.getOfferCommandWrongPrice()));
            return 0;
        }

        //check cooldown
        if (CooldownService.isOnCooldown(p.getUniqueId())) {
            p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
            return 0;
        }
        CooldownService.updateCooldown(p.getUniqueId());

        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();

        //check item is not air
        if (itemInMainHand.getType().isAir()) {
            p.sendMessage(ChatUtils.format(Settings.getOfferCommandWrongItem()));
            return 0;
        }

        //check item is not blacklisted
        if (BlackListUtils.isBlackListed(itemInMainHand)) {
            p.sendMessage(ChatUtils.format(Settings.getBlacklistMessage()));
            return 0;
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

            LogService.log(
                    "Player " + p.getName() +
                            " offer item " + itemInMainHand.getType() +
                            " x" + itemInMainHand.getAmount() +
                            " - " + price
            );
        });

        return 0;
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
