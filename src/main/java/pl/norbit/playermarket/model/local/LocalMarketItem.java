package pl.norbit.playermarket.model.local;

import lombok.Data;
import lombok.NoArgsConstructor;
import mc.obliviate.inventory.Icon;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.gui.shulker.ShulkerContentGui;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.gui.BuyGui;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import pl.norbit.playermarket.utils.gui.LoreBuilder;
import pl.norbit.playermarket.utils.player.ItemsUtils;
import pl.norbit.playermarket.utils.time.ExpireUtils;
import pl.norbit.playermarket.utils.time.TimeUtils;

import java.util.List;

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
    private MarketItemData marketItemData;

    public LocalMarketItem(MarketItemData marketItemData){
        this.marketItemData = marketItemData;
        this.itemStack = marketItemData.getItemStackDeserialize();
        this.id = marketItemData.getId();
        this.ownerName = marketItemData.getOwnerName();
        this.price = marketItemData.getPrice();
        this.offerDate = marketItemData.getOfferDate();
    }

    public Icon getMarketItem(MarketItemType marketItemType) {
        Icon icon = new Icon(getStack(marketItemType));

        icon.onClick(e->{
            Player p = (Player) e.getWhoClicked();

            if (!CooldownService.tryClick(p.getUniqueId())) {
                p.closeInventory();
                p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
                return;
            }

            ClickType click = e.getClick();

            DataService.getMarketItemData(id).thenAccept(mtItemData -> {
                if(mtItemData == null){
                    return;
                }

                if(click == ClickType.RIGHT && ItemsUtils.isShulkerBox(itemStack)){
                    sync(() -> new ShulkerContentGui(p, mtItemData, this).open());
                    return;
                }

                sync(() -> new BuyGui(p, mtItemData, this).open());

            });
        });

        return icon;
    }

    private ItemStack getStack(MarketItemType marketItemType) {
        List<String> lore;

        if (marketItemType == MarketItemType.BUY) {
            lore = Settings.getBuyGui().getIcon("buy-icon").getLore();
        } else if (ItemsUtils.isShulkerBox(itemStack)) {
            lore = Settings.getMarketOfferShulkerLore();
        } else {
            lore = Settings.getMarketOfferItemLore();
        }

        return new LoreBuilder(itemStack)
                .replace("{PRICE}", DoubleFormatter.format(price))
                .replace("{SELLER}", ownerName)
                .replace("{EXPIRE}", ExpireUtils.getRemainingTime(offerDate))
                .replace("{DATE}", TimeUtils.getFormattedDate(offerDate))
                .append(lore);
    }
}
