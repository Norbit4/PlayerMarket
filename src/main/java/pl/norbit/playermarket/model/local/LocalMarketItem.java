package pl.norbit.playermarket.model.local;

import lombok.Data;
import lombok.NoArgsConstructor;
import mc.obliviate.inventory.Icon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.gui.BuyGui;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.DoubleFormatter;
import pl.norbit.playermarket.utils.time.TimeUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class LocalMarketItem {

    private Long id;
    private String ownerUUID;
    private String ownerName;
    private double price;
    private long offerDate;

    private ItemStack itemStack;
    private Icon icon;

    public LocalMarketItem(MarketItemData marketItemData){
        this.itemStack = marketItemData.getItemStackDeserialize();
        this.id = marketItemData.getId();
        this.ownerName = marketItemData.getOwnerName();
        this.price = marketItemData.getPrice();
        this.offerDate = marketItemData.getOfferDate();

        updateMarketItem();
    }
    public Icon getMarketItem() {
        return this.icon;
    }

    public void updateMarketItem(){
        Icon icon = new Icon(addPrice());

        icon.onClick(e->{
            e.setCancelled(true);

            Player p = (Player) e.getWhoClicked();

            ItemStack currentItem = e.getCurrentItem();

            MarketItemData mtItemData = DataService.getMarketItemData(id);

            if(mtItemData == null){
                return;
            }

            new BuyGui(p, mtItemData, currentItem).open();
        });
        this.icon = icon;
    }

    private ItemStack addPrice(){
        ItemMeta iMeta = itemStack.getItemMeta();
        List<String> lore = iMeta.getLore();

        if(lore == null) lore = new ArrayList<>();

        for (String line : Settings.MARKET_OFFER_ITEM_LORE) lore.add(formatLine(line));

        iMeta.setLore(lore);
        itemStack.setItemMeta(iMeta);

        return itemStack;
    }
    private String formatLine(String line){
        return ChatUtils.format(
                line
                        .replace("{PRICE}", DoubleFormatter.format(price))
                        .replace("{SELLER}", ownerName)
                        .replace("{DATE}", TimeUtils.getFormattedDate(offerDate))
        );
    }

}
