package pl.norbit.playermarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.utils.serializer.BukkitSerializer;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MarketItemData {

    private Long id;
    private String ownerName;
    private String ownerUUID;

    private double price;
    private long offerDate;

    private byte[] itemStack;
    private Long playerId;

    public MarketItemData(OfflinePlayer p, byte[] is, double price){
        this.ownerName = p.getName();
        this.ownerUUID = p.getUniqueId().toString();

        this.price = price;
        this.itemStack = is;
        this.offerDate = System.currentTimeMillis();
    }

    public ItemStack getItemStackDeserialize(){
        return BukkitSerializer.deserializeItems(itemStack);
    }

    public String getItemName(){
        return getItemStackDeserialize().getType().toString();
    }
}
