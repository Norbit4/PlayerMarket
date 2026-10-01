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
import pl.norbit.playermarket.service.DialogService;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import pl.norbit.playermarket.utils.gui.LoreBuilder;
import pl.norbit.playermarket.utils.player.ItemsUtils;
import pl.norbit.playermarket.utils.player.PermUtils;
import pl.norbit.playermarket.utils.player.PlayerUtils;
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
        ItemStack formatedItemStack = getStack(marketItemType);
        Icon icon = new Icon(formatedItemStack);

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

                if(click.isKeyboardClick() && PermUtils.hasPermission("playermarket.admin", p)){
                    DialogService.openAdminDelete(p, marketItemData);
                }else {
                    if(click == ClickType.RIGHT && ItemsUtils.isShulkerBox(itemStack)){
                        sync(() -> new ShulkerContentGui(p, mtItemData).open());
                        return;
                    }
                    DialogService.openBuy(p, marketItemData);
                }
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
                .replace("{cost}", DoubleFormatter.format(price))
                .replace("{seller}", ownerName)
                .replace("{expire}", ExpireUtils.getRemainingTime(offerDate))
                .replace("{date}", TimeUtils.getFormattedDate(offerDate))
                .append(lore);
    }
}
