package pl.norbit.playermarket.utils.economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import pl.norbit.playermarket.PlayerMarket;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;

public class ExcellentEconomyUtils {
    private static ExcellentEconomyAPI api;

    private ExcellentEconomyUtils() {}

    private static ExcellentEconomyAPI getApi(){
        if(api != null){
            return api;
        }

        RegisteredServiceProvider<ExcellentEconomyAPI> provider = Bukkit.getServer()
                .getServicesManager().getRegistration(ExcellentEconomyAPI.class);
        if (provider != null) {
            api = provider.getProvider();
        }
        return api;
    }

    protected static boolean withDrawIfPossible(Player p, String currencyName, double amount){
        if(currencyName == null || currencyName.isEmpty()){
            PlayerMarket.getInstance().getLogger().warning("Currency name is null or empty");
            return false;
        }

        ExcellentEconomyAPI excellentEconomyAPI = getApi();

        if(!excellentEconomyAPI.hasCurrency(currencyName)){
            PlayerMarket.getInstance().getLogger().warning("Currency not exist");
            return false;
        }

        double balance = excellentEconomyAPI.getBalance(p, currencyName);

        if (balance < amount) {
            return false;
        }

        excellentEconomyAPI.withdraw(p, currencyName, amount);
        return true;
    }

    protected static void addBalance(Player p, String currencyName, double amount){
        if(currencyName == null || currencyName.isEmpty()){
            PlayerMarket.getInstance().getLogger().warning("Currency name is null or empty");
            return;
        }
        ExcellentEconomyAPI excellentEconomyAPI = getApi();

        if(!excellentEconomyAPI.hasCurrency(currencyName)){
            PlayerMarket.getInstance().getLogger().warning("Currency not exist");
            return;
        }

        excellentEconomyAPI.deposit(p, currencyName, amount);
    }
}
