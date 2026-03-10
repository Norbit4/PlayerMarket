package pl.norbit.playermarket.utils.economy;

import org.bukkit.entity.Player;
import pl.norbit.playermarket.PlayerMarket;

public class CoinsEngineUtils {

//    private CoinsEngineUtils() {
//        throw new IllegalStateException("Utility class");
//    }
//
//    protected static boolean withDrawIfPossible(Player p, String currencyName, double amount){
//        PlayerMarket instance = PlayerMarket.getInstance();
//
//        if(currencyName == null || currencyName.isEmpty()){
//            instance.getLogger().warning("Currency name is null or empty");
//            return false;
//        }
//
//        Currency currency = CoinsEngineAPI.getCurrency(currencyName);
//        if (currency == null){
//            instance.getLogger().warning("Currency not found: " + currencyName);
//            return false;
//        }
//
//        double balance = CoinsEngineAPI.getBalance(p, currency);
//        if (balance < amount) {
//            return false;
//        }
//
//        CoinsEngineAPI.removeBalance(p, currency, amount);
//        return true;
//    }
//
//    protected static void addBalance(Player p, String currencyName, double amount){
//        PlayerMarket instance = PlayerMarket.getInstance();
//
//        if(currencyName == null || currencyName.isEmpty()){
//            instance.getLogger().warning("Currency name is null or empty");
//            return;
//        }
//
//        Currency currency = CoinsEngineAPI.getCurrency(currencyName);
//        if (currency == null){
//            instance.getLogger().warning("Currency not found: " + currencyName);
//            return;
//        }
//
//        CoinsEngineAPI.addBalance(p, currency, amount);
//    }
}
