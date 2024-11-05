package pl.norbit.playermarket.model.local;

import lombok.Data;
import lombok.NoArgsConstructor;
import mc.obliviate.inventory.Icon;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.gui.GuiType;
import pl.norbit.playermarket.gui.shulker.ShulkerContentGui;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.gui.BuyGui;
import pl.norbit.playermarket.utils.TaskUtils;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import pl.norbit.playermarket.utils.player.ItemsUtils;
import pl.norbit.playermarket.utils.time.ExpireUtils;
import pl.norbit.playermarket.utils.time.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import static pl.norbit.playermarket.utils.TaskUtils.async;
import static pl.norbit.playermarket.utils.TaskUtils.sync;

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
    private MarketItemData marketItemData;

    public LocalMarketItem(MarketItemData marketItemData){
        this.marketItemData = marketItemData;
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

            ClickType click = e.getClick();

            async(()->{
                MarketItemData mtItemData = DataService.getMarketItemData(id);

                if(mtItemData == null){
                    return;
                }
                ItemStack currentItem = e.getCurrentItem();

                if(click == ClickType.RIGHT && ItemsUtils.isShulkerBox(itemStack)){
                    sync(()-> new ShulkerContentGui(p, mtItemData, currentItem).open());
                    return;
                }

                sync(()-> new BuyGui(p, mtItemData, currentItem).open());
            });
        });
        this.icon = icon;
    }

    private ItemStack addPrice(){
        ItemMeta iMeta = itemStack.getItemMeta();
        List<String> lore = iMeta.getLore();

        if(lore == null){
            lore = new ArrayList<>();
        }

        List<String> loreToFormat;

        if(ItemsUtils.isShulkerBox(itemStack)){
             loreToFormat = Settings.MARKET_OFFER_SHULKER_LORE;
        }else {
            loreToFormat = Settings.MARKET_OFFER_ITEM_LORE;
        }

        for (String line : loreToFormat){
            lore.add(formatLine(line));
        }

        iMeta.setLore(lore);
        itemStack.setItemMeta(iMeta);

        return itemStack;
    }
    private String formatLine(String line){
        return ChatUtils.format(
                line
                        .replace("{EXPIRE}", ExpireUtils.getRemainingTime(offerDate))
                        .replace("{PRICE}", DoubleFormatter.format(price))
                        .replace("{SELLER}", ownerName)
                        .replace("{DATE}", TimeUtils.getFormattedDate(offerDate))
        );
    }

}
