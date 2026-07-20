package pl.norbit.playermarket.model.local;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mc.obliviate.inventory.Icon;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.cooldown.CooldownService;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.gui.PlayerItemsGui;
import pl.norbit.playermarket.gui.shulker.ShulkerContentGui;
import pl.norbit.playermarket.logs.LogService;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import pl.norbit.playermarket.utils.gui.LoreBuilder;
import pl.norbit.playermarket.utils.player.ItemsUtils;
import pl.norbit.playermarket.utils.time.ExpireUtils;
import pl.norbit.playermarket.utils.player.PlayerUtils;
import pl.norbit.playermarket.utils.time.TimeUtils;

import java.util.List;

import static pl.norbit.playermarket.utils.TaskUtils.sync;

@Data
@NoArgsConstructor
public class LocalPlayerItem {

    private Long id;
    private String ownerUUID;
    private double price;
    private ItemStack itemStack;
    private long offerDate;
    private boolean removeProgress;

    private static final ConfigGui configGui = Settings.getOffersGui();

    @Getter
    private Icon icon;

    public LocalPlayerItem(Long itemID, ItemStack is, double price, long offerDate){
        this.itemStack = is;
        this.id = itemID;
        this.price = price;
        this.offerDate = offerDate;
        this.removeProgress = false;

        generateIcon();
    }

    private void generateIcon(){
        Icon icon = new Icon(getStack());

        icon.onClick(e->{
            e.setCancelled(true);

            Player p = (Player) e.getWhoClicked();

            if(CooldownService.isOnCooldown(p.getUniqueId())){
                p.sendMessage(ChatUtils.format(Settings.getCooldownMessage()));
                return;
            }
            CooldownService.updateCooldown(p.getUniqueId());

            if(PlayerUtils.isInventoryFull(p)){
                p.sendMessage(ChatUtils.format(configGui.getMessage("inventory-full-message")));
                return;
            }

            if(removeProgress){
                return;
            }
            removeProgress = true;

            ClickType click = e.getClick();

            if(click == ClickType.RIGHT && ItemsUtils.isShulkerBox(itemStack)){
                new ShulkerContentGui(p, itemStack).open();
                return;
            }

            DataService.removeItemFromOffer(p, id).thenAccept(item -> {
                sync(() -> {

                    p.sendMessage(ChatUtils.format(Settings.getOffersGui().getMessage("remove-offer-message")));

                    if (item != null) {
                        p.getInventory().addItem(item);
                        LogService.log("Player " + p.getName() + " remove offer item " + item.getType() + " x" + item.getAmount());
                    } else {
                        LogService.log("Player " + p.getName() + " remove offer item failed (null)");
                    }

                    DataService.getPlayerLocalData(p).thenAccept(localData -> {
                        sync(() -> new PlayerItemsGui(p, localData, 0).open());
                    });

                });

            });

        });
        this.icon = icon;
    }

    private ItemStack getStack() {
        List<String> lore = ItemsUtils.isShulkerBox(itemStack)
                ? Settings.getPlayerOfferShulkerLore()
                : Settings.getPlayerOfferItemLore();

        return new LoreBuilder(itemStack)
                .replace("{PRICE}", DoubleFormatter.format(price))
                .replace("{EXPIRE}", ExpireUtils.getRemainingTime(offerDate))
                .replace("{DATE}", TimeUtils.getFormattedDate(offerDate))
                .append(lore);
    }
}
