package pl.norbit.playermarket.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.logs.LogService;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.utils.BlackListUtils;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.player.PermUtils;

public class OfferCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!PermUtils.hasPermission(Settings.OFFER_COMMAND_PERMISSION, sender)){
            sender.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_NO_PERMISSION));
            return true;
        }

        Player p = (Player) sender;

        if(args.length != 1){
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_USAGE));
            return true;
        }


        double price;
        try{
            price =  Double.parseDouble(args[0]);
        }catch (NumberFormatException e){
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_WRONG_PRICE));
            return true;
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

        PlayerData playerData = DataService.getPlayerData(p.getUniqueId().toString());

        //check offers limit
        if(Settings.OFFER_COMMAND_LIMIT_ENABLED){
            int amount = PermUtils.getAmount(p, Settings.OFFER_COMMAND_LIMIT_PERMISSION, Settings.OFFER_COMMAND_DEFAULT_LIMIT);

            int offersAmount = playerData.getPlayerOffers().size();

            if(offersAmount >= amount){
                p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_LIMIT_MESSAGE));
                return true;
            }
        }

        if(itemInMainHand.getType().isAir()){
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_WRONG_ITEM));
            return true;
        }

        p.getInventory().setItemInMainHand(null);

        DataService.addItemToOffer(p, itemInMainHand, price);
        p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_SUCCESS));

        LogService.log("Player " + p.getName() + " offer item " + itemInMainHand.getType() + " x" + itemInMainHand.getAmount() + " - " + price);

        return true;
    }
}
