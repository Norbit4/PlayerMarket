package pl.norbit.playermarket.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

    private final Map<UUID, ItemStack> itemsBackup = new HashMap<>();

    public void register(Commands registrar) {
        String commandName = Settings.getOfferCommandPrefix();
        String argumentName = Settings.getOfferCommandArgumentName();

        LiteralArgumentBuilder<CommandSourceStack> builder =
                Commands.literal(commandName)
                        .requires(source -> {
                            if (!Settings.isOfferCommandPermissionEnabled()) {
                                return true;
                            }
                            return source.getSender().hasPermission(Settings.getOfferCommandPermission());
                        })
                        // /offer
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player p)) {
                                return 0;
                            }
                            p.sendMessage(ChatUtils.format(Settings.getOfferCommandUsage()));
                            return 1;
                        })
                        // /offer <price>
                        .then(Commands.argument(argumentName, DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            if (!(ctx.getSource().getSender() instanceof Player p)) {
                                                return 0;
                                            }

                                            double price = DoubleArgumentType.getDouble(ctx, argumentName);
                                            this.execute(p, price);
                                            return 1;
                                        })
                        );

        registrar.register(builder.build(), "Offer an item");
    }

    private void execute(Player p, double price) {
        if (EconomyUtils.getPluginHook() == PluginHook.PLAYER_POINTS && price != (int) price) {
            p.sendMessage(ChatUtils.format(Settings.getOfferCommandWrongPrice()));
            return;
        }

        if (price <= 0 || price > 99999999) {
            p.sendMessage(ChatUtils.format(Settings.getOfferCommandWrongPrice()));
            return;
        }

        if (CooldownService.isOnCooldown(p.getUniqueId())) {
            p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
            return;
        }

        CooldownService.updateCooldown(p.getUniqueId());

        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();

        if (itemInMainHand.getType().isAir()) {
            p.sendMessage(ChatUtils.format(Settings.getOfferCommandWrongItem()));
            return;
        }

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
            sync(p, () -> {
                if (!p.isOnline()) {
                    return;
                }

                if (Settings.isOfferCommandLimitEnabled()) {
                    int amount = PermUtils.getAmount(p,
                            Settings.getOfferCommandLimitPermission(),
                            Settings.getOfferCommandDefaultLimit());

                    if (playerData.getPlayerOffers().size() >= amount) {
                        backupItem(p);
                        p.sendMessage(ChatUtils.format(Settings.getOfferCommandLimitMessage()));
                        return;
                    }
                }

                DataService.addItemToOffer(p, itemInMainHand, price);

                p.sendMessage(ChatUtils.format(Settings.getOfferCommandSuccess()));

                LogService.log("Player " + p.getName()
                                + " offer item "
                                + itemInMainHand.getType()
                                + " x" + itemInMainHand.getAmount()
                                + " - " + price
                );
            });
        });
    }

    private void backupItem(Player p) {
        if (!p.isOnline()) {
            return;
        }

        ItemStack itemStack = itemsBackup.get(p.getUniqueId());

        if (itemStack != null) {
            p.getInventory().setItemInMainHand(itemStack);
        }
    }
}