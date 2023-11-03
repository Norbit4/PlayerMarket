package pl.norbit.playermarket.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import pl.norbit.playermarket.PlayerMarket;

import java.util.UUID;

public class EconomyService {

    private static Economy economy;

    public static void load(){
        Server server = PlayerMarket.getInstance().getServer();

        RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);

        if (rsp == null) return;

        economy = rsp.getProvider();
    }
    public static Economy getEconomy() {
        return economy;
    }

    public static boolean withDrawIfPossible(Player p, double amount){
        if(economy.getBalance(p) < amount) return false;

        economy.withdrawPlayer(p, amount);
        return true;
    }

    public static void deposit(Player p, double amount){
        economy.depositPlayer(p, amount);
    }
    public static boolean hasEnoughMoney(Player p, double amount){
        return economy.getBalance(p) >= amount;
    }

    public static double getBalance(Player p){
        return economy.getBalance(p);
    }
    public static double getBalance(UUID playerUUID){
        OfflinePlayer offlinePlayer = PlayerMarket.getInstance().getServer().getOfflinePlayer(playerUUID);

        return economy.getBalance(offlinePlayer);
    }
}
