package pl.norbit.playermarket.data;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.logs.LogService;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.utils.serializer.BukkitSerializer;

import java.util.List;
import java.util.logging.Logger;

import static pl.norbit.playermarket.utils.TaskUtils.async;

public class DataService {
    private DataService() {
        throw new IllegalStateException("Utility class");
    }

    public static void start(){
        JDBCService.init();
    }

    public static PlayerData getPlayerData(String playerUUID){
        return JDBCService.getPlayerData(playerUUID);
    }

    public static boolean isReady(){
        return JDBCService.isReady();
    }

    public static void buyItem(MarketItemData marketItemData){

        PlayerData playerData = getPlayerData(marketItemData.getOwnerUUID());
        Long itemId = marketItemData.getId();

        if(playerData == null){
            return;
        }

        double price = marketItemData.getPrice();

        //calculate tax
        if(Settings.isTaxEnabled()){
            double taxValue = Settings.getTaxValue();

            if(taxValue < 1){
                double tax = price * taxValue;
                price = price - tax;
            }else {
                LogService.warn("Tax value is higher than 1. Tax value should be in range 0-1");
            }
        }

        //update stats for seller
        playerData.setSoldItems(playerData.getSoldItems() + 1);
        playerData.setEarnedMoney(playerData.getEarnedMoney() + price);
        playerData.setTotalEarnedMoney(playerData.getTotalEarnedMoney() + price);
        playerData.setTotalSoldItems(playerData.getTotalSoldItems() + 1);

        //remove item from seller
        removeMarketItem(itemId);

        updatePlayerData(playerData);
    }

    public static void close(){
        JDBCService.close();
    }

    public static MarketItemData getMarketItemData(Long itemId){
        return JDBCService.getMarketItem(itemId);
    }

    public static ItemStack removeItemFromOffer(Player p,Long itemId){
        PlayerData pData = getPlayerData(p.getUniqueId().toString());

        if(pData == null){
            return null;
        }

        MarketItemData offer = pData.getOffer(itemId);

        if(offer == null){
            return null;
        }

        ItemStack itemStack = offer.getItemStackDeserialize();

        removeMarketItem(itemId);

        updatePlayerData(pData);

        return itemStack;
    }

    public static LocalPlayerData getPlayerLocalData(OfflinePlayer p){
        PlayerData pData = getPlayerDataCreate(p);

        return new LocalPlayerData(pData);
    }


    public static void createPlayerData(PlayerData playerData) {
        JDBCService.createPlayerData(playerData);
    }

    public static void removeMarketItem(Long id) {
        JDBCService.removeMarketItem(id);
    }

    public static List<MarketItemData> getAll(){
        return JDBCService.getAllMarketItems();
    }

    public static void addItemToOffer(OfflinePlayer p, ItemStack is, double price){
        async(() ->{
            PlayerData pData = getPlayerDataCreate(p);

            MarketItemData mItemData = new MarketItemData(p, BukkitSerializer.serializeItems(is), price);

            updatePlayerData(pData);
            updateMarketItem(pData, mItemData);
        });
    }

    public static void clearPlayerData(OfflinePlayer p){
        async(() ->{
            PlayerData pData = getPlayerDataCreate(p);

            pData.setEarnedMoney(0);
            pData.setSoldItems(0);
            pData.setTotalEarnedMoney(0);
            pData.setTotalSoldItems(0);

            pData.getPlayerOffers().forEach(mItem -> removeMarketItem(mItem.getId()));

            updatePlayerData(pData);
        });
    }

    public static void updatePlayerData(PlayerData pData){
        JDBCService.updatePlayerData(pData);
    }

    public static void updateMarketItem(PlayerData pData, MarketItemData mItemData){
        JDBCService.addMarketItemForPlayer(pData, mItemData);
    }

    public static PlayerData getPlayerDataCreate(OfflinePlayer p){
        String playerStringUUID = p.getUniqueId().toString();

        PlayerData pData = getPlayerData(playerStringUUID);

        if(pData != null) return pData;

        pData = new PlayerData();

        pData.setPlayerUUID(playerStringUUID);
        pData.setPlayerName(p.getName());

        createPlayerData(pData);

        return pData;
    }
}
