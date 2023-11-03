package pl.norbit.playermarket.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.TaskUtils;

public class OfferItemCMD implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player p = (Player)sender;

        if(args.length != 1){
            p.sendMessage(ChatUtils.format("&cPoprawne uzycie: /wystaw <cena>"));
            return true;
        }
        double price;
        try{
            price =  Double.parseDouble(args[0]);
        }catch (NumberFormatException e){
            p.sendMessage(ChatUtils.format("&cCena musi byc liczba!"));
            return true;
        }

        if(price <= 0){
            p.sendMessage(ChatUtils.format("&cCena musi byc wieksza od 0!"));
            return true;
        }
        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();

        if(itemInMainHand.getType().isAir()){
            p.sendMessage(ChatUtils.format("&cMusisz trzymac przedmiot w rece!"));
            return true;
        }
        p.getInventory().setItemInMainHand(null);

        TaskUtils.runTaskLaterAsynchronously(() -> DataService.addItemToOffer(p,itemInMainHand, price), 0L);
        p.sendMessage(ChatUtils.format("&aWystawiles przedmiot na sprzedaz!"));

        return true;
    }
}
