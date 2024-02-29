package pl.norbit.playermarket.model;

import lombok.*;


import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlayerData {

    private Long id;
    private String playerName;
    private String playerUUID;
    private int soldItems;
    private int totalSoldItems;
    private double earnedMoney;
    private double totalEarnedMoney;

    @Builder.Default
    private List<MarketItemData> playerOffers = new ArrayList<>();


    public MarketItemData getOffer(Long id){
        return playerOffers.stream().filter(iData -> iData.getId().equals(id)).findFirst().orElse(null);
    }
    public void addOffer(MarketItemData itemData){
        playerOffers.add(itemData);
    }

    public void removeOffer(MarketItemData itemData){
        playerOffers.remove(itemData);
    }

    public void removeOffer(Long id){
        playerOffers.removeIf(iData -> iData.getId().equals(id));
    }
}
