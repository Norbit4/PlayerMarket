package pl.norbit.playermarket.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.PermissionUtils;
import pl.norbit.playermarket.utils.TaskUtils;
public class OfferCommand extends BukkitCommand {

    public OfferCommand() {
        super(Settings.OFFER_COMMAND_NAME);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

        if(!PermissionUtils.hasPermission(Settings.OFFER_COMMAND_PERMISSION, sender)){
            sender.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_NO_PERMISSION));
            return true;
        }

        Player p = (Player)sender;

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

        if(price <= 0){
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_WRONG_PRICE));
            return true;
        }
        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();

        if(itemInMainHand.getType().isAir()){
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_WRONG_ITEM));
            return true;
        }

        p.getInventory().setItemInMainHand(null);

        TaskUtils.runTaskLaterAsynchronously(() -> DataService.addItemToOffer(p,itemInMainHand, price), 0L);
        p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_SUCCESS));

        return true;
    }
    public void register(){
        PlayerMarket.getInstance().getServer().getCommandMap().register("", this);
    }
}
