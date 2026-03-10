package pl.norbit.playermarket.utils.economy;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import pl.norbit.playermarket.PlayerMarket;

public class VaultUtils {

    private VaultUtils() {}

    @Getter
    private static Economy economy;

    protected static void load(){
        Server server = PlayerMarket.getInstance().getServer();

        RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);

        if (rsp == null){
            return;
        }

        economy = rsp.getProvider();
    }

    protected static boolean withDrawIfPossible(Player p, double amount){
        if(economy.getBalance(p) < amount){
            return false;
        }

        economy.withdrawPlayer(p, amount);
        return true;
    }

    protected static void deposit(Player p, double amount){
        economy.depositPlayer(p, amount);
    }
}
