package pl.norbit.playermarket.utils.economy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.plugins.PluginHook;
import pl.norbit.playermarket.plugins.PluginService;

import java.util.logging.Logger;

public class EconomyUtils {
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private static PluginHook pluginHook;
    @Setter(AccessLevel.PRIVATE)
    private static String currencyName;

    private EconomyUtils() {}

    public static void load(){
        int count = 0;

        if(PluginService.isEnabled(PluginHook.PLAYER_POINTS)){
            PlayerPointsUtils.load();
            count++;
        }

        if(PluginService.isEnabled(PluginHook.VAULT)){
            VaultUtils.load();
            count++;
        }

        if(PluginService.isEnabled(PluginHook.EXCELLENT_ECONOMY)){
            count++;
        }

        if(count == 0){
            Logger logger = PlayerMarket.getInstance().getLogger();

            logger.severe("ERROR!");
            logger.severe(" ");
            logger.severe("No economy plugin found!");
            logger.severe(" ");
            logger.severe("ERROR!");
        }
    }
    public static void setEconomyType(String type, String currency) {
        if(type == null){
            throw new IllegalArgumentException("Invalid economy type");
        }
        setCurrencyName(currency);

        String economyType = type.toUpperCase();

        if(economyType.equalsIgnoreCase("PLAYER_POINTS") || economyType.equalsIgnoreCase("PLAYERPOINTS")) {
            pluginHook = PluginHook.PLAYER_POINTS;
            PlayerMarket.getInstance().getLogger().info("Economy type set to PLAYER_POINTS.");

        }else if (economyType.equalsIgnoreCase("EXCELLENT_ECONOMY") || economyType.equalsIgnoreCase("EXCELLENTECONOMY")){
                pluginHook = PluginHook.EXCELLENT_ECONOMY;
                PlayerMarket.getInstance().getLogger().info("Economy type set to EXCELLENT_ECONOMY.");

        }else {
            pluginHook = PluginHook.VAULT;
            PlayerMarket.getInstance().getLogger().info("Economy type set to VAULT.");
        }
    }

    public static boolean withDrawIfPossible(Player p, double amount){
        if(pluginHook == PluginHook.PLAYER_POINTS && PluginService.isEnabled(PluginHook.PLAYER_POINTS)){
            return PlayerPointsUtils.withDrawIfPossible(p, (int) amount);
        } else if (pluginHook == PluginHook.VAULT && PluginService.isEnabled(PluginHook.VAULT)) {
            return VaultUtils.withDrawIfPossible(p, amount);
        } else if (pluginHook == PluginHook.EXCELLENT_ECONOMY && PluginService.isEnabled(PluginHook.EXCELLENT_ECONOMY)) {
            return ExcellentEconomyUtils.withDrawIfPossible(p, currencyName, amount);
        }
        PlayerMarket.getInstance().getLogger().warning(
                "Cannot withdraw money. Economy type: " + pluginHook +
                        ", required plugin is not enabled."
        );
        return false;
    }
    public static void deposit(Player p, double amount){
        if(pluginHook == PluginHook.PLAYER_POINTS && PluginService.isEnabled(PluginHook.PLAYER_POINTS)){
            PlayerPointsUtils.addPoints(p, (int) amount);
        } else if (pluginHook == PluginHook.VAULT && PluginService.isEnabled(PluginHook.VAULT)) {
            VaultUtils.deposit(p, amount);
        } else if (pluginHook == PluginHook.EXCELLENT_ECONOMY && PluginService.isEnabled(PluginHook.EXCELLENT_ECONOMY)) {
            ExcellentEconomyUtils.addBalance(p, currencyName, amount);
        } else {
            PlayerMarket.getInstance().getLogger().warning("Cannot deposit money. Economy type: " + pluginHook +
                            ", required plugin is not enabled.");
        }
    }
}
