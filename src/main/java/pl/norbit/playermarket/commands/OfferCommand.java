package pl.norbit.playermarket.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.PermUtils;
import pl.norbit.playermarket.utils.TaskUtils;
public class OfferCommand extends BukkitCommand {

    public OfferCommand() {
        super(Settings.OFFER_COMMAND_NAME);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

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
        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();


        //check item is not air
        if(itemInMainHand.getType().isAir()){
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_WRONG_ITEM));
            return true;
        }

        TaskUtils.runTaskLaterAsynchronously(() ->{
            PlayerData playerData = DataService.getPlayerData(p.getUniqueId().toString());

            //check offers limit
            if(Settings.OFFER_COMMAND_LIMIT_ENABLED){
                int amount = PermUtils.getAmount(p, Settings.OFFER_COMMAND_LIMIT_PERMISSION, Settings.OFFER_COMMAND_DEFAULT_LIMIT);

                int offersAmount = playerData.getPlayerOffers().size();

                if(offersAmount >= amount){
                    p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_LIMIT_MESSAGE));
                    return;
                }
            }

            TaskUtils.runTaskLater(() -> p.getInventory().setItemInMainHand(null),0L);

            DataService.addItemToOffer(p,itemInMainHand, price);
            p.sendMessage(ChatUtils.format(Settings.OFFER_COMMAND_SUCCESS));
        }, 0L);


        return true;
    }

    public void register(){
        PlayerMarket.getInstance().getServer().getCommandMap().register("", this);
    }
}
