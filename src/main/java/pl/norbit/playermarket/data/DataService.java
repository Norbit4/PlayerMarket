package pl.norbit.playermarket.data;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.logs.DiscordLogs;
import pl.norbit.playermarket.logs.LogService;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.utils.serializer.BukkitSerializer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataService {

    private DataService() {
        throw new IllegalStateException("Utility class");
    }

    public static void start(){
        JDBCService.init();
    }

    public static boolean isReady(){
        return JDBCService.isReady();
    }

    public static CompletableFuture<PlayerData> getPlayerData(String playerUUID){
        return JDBCService.getPlayerData(playerUUID);
    }

    public static CompletableFuture<MarketItemData> getMarketItemData(Long itemId){
        return JDBCService.getMarketItem(itemId);
    }

    public static CompletableFuture<List<MarketItemData>> getAll(){
        return JDBCService.getAllMarketItems();
    }

    public static CompletableFuture<Double> buyItem(MarketItemData marketItemData){

        return getPlayerData(marketItemData.getOwnerUUID()).thenApply(playerData -> {

            if(playerData == null){
                return 0.0;
            }

            Long itemId = marketItemData.getId();
            double price = marketItemData.getPrice();

            double tax = 0;

            if(Settings.isTaxEnabled()){
                double taxValue = Settings.getTaxValue();

                if(taxValue < 1){
                    tax = price * taxValue;
                    price -= tax;
                } else {
                    LogService.warn("Tax value is higher than 1. Tax value should be in range 0-1");
                }
            }

            playerData.setSoldItems(playerData.getSoldItems() + 1);
            playerData.setEarnedMoney(playerData.getEarnedMoney() + price);
            playerData.setTotalEarnedMoney(playerData.getTotalEarnedMoney() + price);
            playerData.setTotalSoldItems(playerData.getTotalSoldItems() + 1);

            removeMarketItem(itemId);
            updatePlayerData(playerData);

            return tax;
        });
    }

    public static void close(){
        JDBCService.close();
    }

    public static CompletableFuture<ItemStack> removeItemFromOffer(Player p, Long itemId){

        return getPlayerData(p.getUniqueId().toString()).thenApply(pData -> {

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
        });
    }

    public static CompletableFuture<LocalPlayerData> getPlayerLocalData(OfflinePlayer p){
        return getPlayerDataCreate(p).thenApply(LocalPlayerData::new);
    }

    public static void createPlayerData(PlayerData playerData) {
        JDBCService.createPlayerData(playerData);
    }

    public static void removeMarketItem(Long id) {
        JDBCService.removeMarketItem(id);
    }

    public static void addItemToOffer(OfflinePlayer p, ItemStack is, double price){
        getPlayerDataCreate(p).thenAccept(pData -> {

            MarketItemData mItemData = new MarketItemData(p, BukkitSerializer.serializeItems(is), price);

            updatePlayerData(pData);
            updateMarketItem(pData, mItemData);

            DiscordLogs.offerItem(mItemData);

        });
    }

    public static void clearPlayerData(OfflinePlayer p){

        getPlayerDataCreate(p).thenAccept(pData -> {

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

    public static CompletableFuture<PlayerData> getPlayerDataCreate(OfflinePlayer p){
        String playerUUID = p.getUniqueId().toString();

        return getPlayerData(playerUUID).thenApply(pData -> {

            if(pData != null) return pData;

            PlayerData newData = new PlayerData();

            newData.setPlayerUUID(playerUUID);
            newData.setPlayerName(p.getName());

            createPlayerData(newData);

            return newData;
        });
    }
}