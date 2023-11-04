package pl.norbit.playermarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.utils.serializer.BukkitSerializer;

import jakarta.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MarketItemData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ownerName;
    private String ownerUUID;

    private double price;
//    private long date;

    @Lob
    @Column(name = "itemStack", columnDefinition="BLOB")
    private byte[] itemStack;

    public MarketItemData(Player p, ItemStack is, double price){
        this.ownerName = p.getName();
        this.ownerUUID = p.getUniqueId().toString();

        this.price = price;
        this.itemStack = BukkitSerializer.serializeItems(is);
    }

    public ItemStack getItemStack(){
        return BukkitSerializer.deserializeItems(itemStack);
    }
}
