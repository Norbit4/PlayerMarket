package pl.norbit.playermarket.data;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.utils.serializer.BukkitSerializer;

import java.util.List;

public class DataService {

    public static void start(){
        JDBCService.init();
    }

    public static PlayerData getPlayerData(String playerUUID){
        return JDBCService.getPlayerData(playerUUID);
    }

    public static void buyItem(MarketItemData marketItemData){

        PlayerData playerData = getPlayerData(marketItemData.getOwnerUUID());
        Long id = marketItemData.getId();

        if(playerData == null) return;

        //update stats for seller
        playerData.setSoldItems(playerData.getSoldItems() + 1);
        playerData.setEarnedMoney(playerData.getEarnedMoney() + marketItemData.getPrice());
        playerData.setTotalEarnedMoney(playerData.getTotalEarnedMoney() + marketItemData.getPrice());
        playerData.setTotalSoldItems(playerData.getTotalSoldItems() + 1);

        //remove item from seller
//        playerData.removeOffer(id);
        removeMarketItem(id);

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

        if(pData == null) return null;

        MarketItemData offer = pData.getOffer(itemId);

        if(offer == null) return null;

        ItemStack itemStack = offer.getItemStackDeserialize();

//        pData.removeOffer(itemId);

        removeMarketItem(itemId);

        updatePlayerData(pData);

        return itemStack;
    }

    public static LocalPlayerData getPlayerLocalData(Player p){
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

    public static void addItemToOffer(Player p, ItemStack is, double price){
        PlayerData pData = getPlayerDataCreate(p);

        MarketItemData mItemData = new MarketItemData(p, BukkitSerializer.serializeItems(is), price);

        updatePlayerData(pData);
        updateMarketItem(pData, mItemData);
    }

    public static void updatePlayerData(PlayerData pData){
        JDBCService.updatePlayerData(pData);
    }

    public static void updateMarketItem(PlayerData pData, MarketItemData mItemData){
        JDBCService.addMarketItemForPlayer(pData, mItemData);
    }

    public static PlayerData getPlayerDataCreate(Player p){
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
