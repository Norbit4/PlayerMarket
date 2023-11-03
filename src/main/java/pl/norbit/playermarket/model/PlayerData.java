package pl.norbit.playermarket.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
public class PlayerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String playerName;
    private String playerUUID;
    private int soldItems;
    private double totalSoldItems;
    private double earnedMoney;
    private double totalEarnedMoney;

    @OneToMany(cascade = CascadeType.ALL,  orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn()
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
