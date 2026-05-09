package pl.norbit.playermarket.utils.economy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import pl.norbit.playermarket.PlayerMarket;

import java.util.logging.Logger;

public class EconomyUtils {
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private static EconomyType economyType;
    @Setter(AccessLevel.PRIVATE)
    private static String currencyName;
    @Setter(AccessLevel.PRIVATE)
    private static PlayerMarket instance;

    private EconomyUtils() {}

    private static boolean checkPlugin(String pluginName) {
        Server server = instance.getServer();

        PluginManager pM = server.getPluginManager();
        Plugin plugin = pM.getPlugin(pluginName);

        if(plugin != null && plugin.isEnabled()){
            Logger logger = server.getLogger();
            logger.info("Hooked to: " + pluginName);
            return true;
        }
        return false;
    }

    public static void load(){
        int count = 0;

        if(checkPlugin("PlayerPoints")){
            PlayerPointsUtils.load();
            count++;
        }

        if(checkPlugin("Vault")){
            VaultUtils.load();
            count++;
        }

        if(checkPlugin("CoinsEngine")){
            count++;
        }

        if(count == 0){
            instance.getLogger().severe("No economy plugin found!");
        }
    }
    public static void setEconomyType(String type, String currency) {
        setInstance(PlayerMarket.getInstance());
        if(type == null){
            throw new IllegalArgumentException("Invalid economy type");
        }
        setCurrencyName(currency);

        try {
            setEconomyType(EconomyType.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid economy type");
        }

        PluginManager pluginManager = instance.getServer().getPluginManager();

        String pluginName = economyType.getName();

        if(!pluginManager.isPluginEnabled(pluginName)){
            throw new IllegalArgumentException(pluginName + " not found");
        }else {
            instance.getLogger().info("Using " + pluginName + " as economy plugin");
        }
    }

    public static boolean withDrawIfPossible(Player p, double amount){
        if(economyType == EconomyType.PLAYER_POINTS){
            return PlayerPointsUtils.withDrawIfPossible(p, (int) amount);
        } else if (economyType == EconomyType.VAULT) {
            return VaultUtils.withDrawIfPossible(p, amount);
//        } else if (economyType == EconomyType.COINS_ENGINE) {
//            return CoinsEngineUtils.withDrawIfPossible(p, currencyName, amount);
        } else {
            throw new IllegalArgumentException("Unsupported economy type: " + economyType);
        }
    }
    public static void deposit(Player p, double amount){
        if(economyType == EconomyType.PLAYER_POINTS){
            PlayerPointsUtils.addPoints(p, (int) amount);
        } else if (economyType == EconomyType.VAULT) {
            VaultUtils.deposit(p, amount);
//        } else if (economyType == EconomyType.COINS_ENGINE) {
//            CoinsEngineUtils.addBalance(p, currencyName, amount);
        } else {
            throw new IllegalArgumentException("Unsupported economy type: " + economyType);
        }
    }
}
