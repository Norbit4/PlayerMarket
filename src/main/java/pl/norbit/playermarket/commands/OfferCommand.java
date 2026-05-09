package pl.norbit.playermarket.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
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

public class OfferCommand implements CommandExecutor {
    private final Map<UUID, ItemStack> itemsBackup = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!PermUtils.hasPermission(Settings.OFFER_COMMAND_PERMISSION, sender, Settings.OFFER_COMMAND_PERMISSION_ENABLED)){
            sender.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_NO_PERMISSION));
            return true;
        }

        Player p = (Player) sender;

        if(args.length != 1){
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_USAGE));
            return true;
        }

        double price;
        if(EconomyUtils.getEconomyType() == EconomyType.PLAYER_POINTS){
            try{
                price =  Integer.parseInt(args[0]);
            }catch (NumberFormatException e){
                p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_WRONG_PRICE));
                return true;
            }
        }else {
            try{
                price =  Double.parseDouble(args[0]);
            }catch (NumberFormatException e){
                p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_WRONG_PRICE));
                return true;
            }
        }

        //check price
        if(price <= 0){
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_WRONG_PRICE));
            return true;
        }

        if(price > 99999999){
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_WRONG_PRICE));
            return true;
        }

        //check cooldown
        if(CooldownService.isOnCooldown(p.getUniqueId())){
            p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
            return true;
        }
        CooldownService.updateCooldown(p.getUniqueId());

        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();

        //check item is not air
        if(itemInMainHand.getType().isAir()){
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_WRONG_ITEM));
            return true;
        }

        //check item is not blacklisted
        if(BlackListUtils.isBlackListed(itemInMainHand)){
            p.sendMessage(ChatUtils.format(Settings.getBlacklistMessage()));
            return true;
        }

        itemsBackup.put(p.getUniqueId(), itemInMainHand.clone());
        p.getInventory().setItemInMainHand(null);

        DataService.getPlayerData(p.getUniqueId().toString()).thenAccept(playerData -> {
            if(playerData == null){
                return;
            }

            if(Settings.OFFER_COMMAND_LIMIT_ENABLED){
                int amount = PermUtils.getAmount(
                        p,
                        Settings.OFFER_COMMAND_LIMIT_PERMISSION,
                        Settings.OFFER_COMMAND_DEFAULT_LIMIT
                );

                int offersAmount = playerData.getPlayerOffers().size();

                if(offersAmount >= amount){
                    backupItem(p);
                    p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_LIMIT_MESSAGE));
                    return;
                }
            }

            DataService.addItemToOffer(p, itemInMainHand, price);

            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_SUCCESS));

            LogService.log(
                    "Player " + p.getName() +
                            " offer item " + itemInMainHand.getType() +
                            " x" + itemInMainHand.getAmount() +
                            " - " + price);
        });
        return true;
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
