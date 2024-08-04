package pl.norbit.playermarket.model.local;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mc.obliviate.inventory.Icon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.gui.PlayerItemsGui;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.DoubleFormatter;
import pl.norbit.playermarket.utils.time.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import static pl.norbit.playermarket.utils.TaskUtils.async;
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

    @Getter
    private Icon icon;

    public LocalPlayerItem(Long itemID, ItemStack is, double price, long offerDate){
        this.itemStack = is;
        this.id = itemID;
        this.price = price;
        this.offerDate = offerDate;
        this.removeProgress = false;

        updateMarketItem();
    }

    public void updateMarketItem(){
        Icon icon = new Icon(addPrice());

        icon.onClick(e->{
            e.setCancelled(true);

            if(removeProgress){
                return;
            }
            removeProgress = true;
            async(() -> {
                Player p = (Player) e.getWhoClicked();

                ItemStack item = DataService.removeItemFromOffer(p, id);

                p.sendMessage(ChatUtils.format(Settings.OFFERS_GUI.getMessage("remove-offer-message")));

                if(item != null){
                    p.getInventory().addItem(item);
                }

                LocalPlayerData pLocalData = DataService.getPlayerLocalData(p);

                sync(() -> new PlayerItemsGui(p, pLocalData, 0).open());
            });
        });
        this.icon = icon;
    }

    private ItemStack addPrice(){
        ItemMeta iMeta = itemStack.getItemMeta();
        List<String> lore = iMeta.getLore();

        if(lore == null) lore = new ArrayList<>();

        for (String line : Settings.PLAYER_OFFER_ITEM_LORE) lore.add(formatLine(line));

        iMeta.setLore(lore);
        itemStack.setItemMeta(iMeta);

        return itemStack;
    }
    private String formatLine(String line){
        return ChatUtils.format(
                line.replace("{PRICE}", DoubleFormatter.format(price))
                        .replace("{DATE}", TimeUtils.getFormattedDate(offerDate))
        );
    }
}
