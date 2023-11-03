package pl.norbit.playermarket.data;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.model.PlayerData;

import java.util.List;

public class DataService {

    public static void start(){
        HibernateService.init();
    }

    public static PlayerData getPlayerData(String playerUUID){
        return HibernateService.getPlayerData(playerUUID);
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
        playerData.removeOffer(id);

        updatePlayerData(playerData);
    }

    public static void close(){
        HibernateService.close();
    }

    public static MarketItemData getMarketItemData(Long itemId){
        return HibernateService.getMarketItem(itemId);
    }

    public static ItemStack removeItemFromOffer(Player p,Long itemId){
        PlayerData pData = getPlayerData(p.getUniqueId().toString());

        if(pData == null) return null;

        MarketItemData offer = pData.getOffer(itemId);

        if(offer == null) return null;

        ItemStack itemStack = offer.getItemStack();

        pData.removeOffer(itemId);

        updatePlayerData(pData);

        return itemStack;
    }

    public static LocalPlayerData getPlayerLocalData(Player p){
        PlayerData pData = getPlayerDataCreate(p);

        return new LocalPlayerData(pData);
    }
    public static List<MarketItemData> getAll(){
        return HibernateService.getAllMarketItems();
    }

    public static void addItemToOffer(Player p, ItemStack is, double price){
        PlayerData pData = getPlayerDataCreate(p);

        MarketItemData mItemData = new MarketItemData(p, is, price);

        pData.getPlayerOffers().add(mItemData);
        updatePlayerData(pData);
    }

    public static void updatePlayerData(PlayerData pData){
        HibernateService.updatePlayerData(pData);
    }

    public static PlayerData getPlayerDataCreate(Player p){
        String playerStringUUID = p.getUniqueId().toString();

        PlayerData pData = getPlayerData(playerStringUUID);

        if(pData != null) return pData;

        pData = new PlayerData();

        pData.setPlayerUUID(playerStringUUID);
        pData.setPlayerName(p.getName());

        updatePlayerData(pData);

        return pData;
    }
}
