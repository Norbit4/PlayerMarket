package pl.norbit.playermarket.model.local;

import lombok.Data;
import lombok.NoArgsConstructor;
import mc.obliviate.inventory.Icon;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.gui.PlayerItemsGui;
import pl.norbit.playermarket.utils.ChatUtils;
import pl.norbit.playermarket.utils.DoubleFormatter;
import pl.norbit.playermarket.utils.TaskUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class LocalPlayerItem {

    private Long id;
    private String ownerUUID;
    private double price;
    private ItemStack itemStack;
    private Icon icon;

    public LocalPlayerItem(Long itemID, ItemStack is, double price){
        this.itemStack = is;
        this.id = itemID;
        this.price = price;

        updateMarketItem();
    }
    public Icon getIcon() {
        return this.icon;
    }

    public void updateMarketItem(){
        Icon icon = new Icon(addPrice());

        icon.onClick(e->{
            e.setCancelled(true);

            TaskUtils.runTaskLaterAsynchronously(() -> {
                Player p = (Player) e.getWhoClicked();

                ItemStack itemStack1 = DataService.removeItemFromOffer(p, id);

                p.sendMessage(ChatUtils.format(Settings.OFFERS_GUI.getMessage("remove-offer-message")));

                if(itemStack1 != null)  p.getInventory().addItem(itemStack1);

                LocalPlayerData pLocalData = DataService.getPlayerLocalData(p);

                TaskUtils.runTaskLater(() -> new PlayerItemsGui(p, pLocalData, 0).open(), 0L);
            }, 0L);
        });
        this.icon = icon;
    }

    private  ItemStack addPrice(){
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
                line
                        .replace("{PRICE}", DoubleFormatter.format(price))
                        .replace("{DATE}", "brak")
        );
    }
}
