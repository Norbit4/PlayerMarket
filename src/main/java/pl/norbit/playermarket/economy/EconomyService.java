package pl.norbit.playermarket.economy;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.logs.LogService;

public class EconomyService {
    private static Economy economy;
    private static PlayerPointsAPI playerPointsAPI;

    private static EconomyType economyType;

    private EconomyService() {
        throw new IllegalStateException("Utility class");
    }

    public static void setEconomyType(String type) {
        if(type == null){
            throw new IllegalArgumentException("Invalid economy type");
        }

        try {
            economyType = EconomyType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid economy type");
        }

        PlayerMarket instance = PlayerMarket.getInstance();
        PluginManager pluginManager = instance.getServer().getPluginManager();

        String pluginName = economyType.getName();

        if(!pluginManager.isPluginEnabled(pluginName)){
            throw new IllegalArgumentException(pluginName + " not found");
        }else {
            instance.getLogger().info("Using " + pluginName + " as economy plugin");
        }
    }

    public static void load(){
        Server server = PlayerMarket.getInstance().getServer();

        if(economyType == EconomyType.VAULT) {
            RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);

            if (rsp == null){
                return;
            }

            economy = rsp.getProvider();
        } else if (EconomyType.PLAYERPOINTS == economyType) {
            playerPointsAPI = PlayerPoints.getInstance().getAPI();
        }
    }

    public static boolean withDrawIfPossible(Player p, double amount){
        if(economyType == EconomyType.VAULT && economy.getBalance(p) < amount) {
            return false;
        } else if (economyType == EconomyType.PLAYERPOINTS && playerPointsAPI.look(p.getUniqueId()) < amount)
            return false;

        if(economyType == EconomyType.VAULT) {
            economy.withdrawPlayer(p, amount);
        } else if (EconomyType.PLAYERPOINTS == economyType) {
            playerPointsAPI.take(p.getUniqueId(), (int) amount);
        }

        LogService.log("Player " + p.getName() + " eco remove: " + amount + " $");
        return true;
    }

    public static void deposit(Player p, double amount){
        if (EconomyType.PLAYERPOINTS == economyType) {
            playerPointsAPI.give(p.getUniqueId(), (int) amount);
        } else if (EconomyType.VAULT == economyType) {
            economy.depositPlayer(p, amount);
        }
        LogService.log("Player " + p.getName() + " eco add: " + amount + " $");
    }
}
