package pl.norbit.playermarket.model.local;

import lombok.Data;
import lombok.NoArgsConstructor;
import mc.obliviate.inventory.Icon;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pl.norbit.playermarket.PlayerMarket;
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

    private static final String priceTemplate = "&7Cena: &a{PRICE}&7";
    private static final String removeTemplate = "&eKliknij aby usunąć!";

    private static NamespacedKey idKey = new NamespacedKey(PlayerMarket.getInstance(), "ID");
    private Long id;
    private String ownerUUID;
    private double price;
    private ItemStack itemStack;
    private Icon icon;

    public LocalPlayerItem(Long itemID, ItemStack is, double price){
        ItemMeta itemMeta = is.getItemMeta();

        PersistentDataContainer pDataContainer = itemMeta.getPersistentDataContainer();

        pDataContainer.set(idKey, PersistentDataType.LONG, itemID);

        is.setItemMeta(itemMeta);
        this.itemStack = is;
        this.id = itemID;
        this.price = price;
        updateMarketItem();
    }
    public Icon getIcon() {
        return this.icon;
    }

    public void updateMarketItem(){
        Icon icon = new Icon(addPrice(itemStack, price));

        icon.onClick(e->{
            e.setCancelled(true);

            TaskUtils.runTaskLaterAsynchronously(() -> {
                Player p = (Player) e.getWhoClicked();

                ItemStack currentItem = e.getCurrentItem();

                ItemMeta itemMeta = currentItem.getItemMeta();

                PersistentDataContainer pDataContainer = itemMeta.getPersistentDataContainer();

                Long ID = pDataContainer.get(idKey, PersistentDataType.LONG);

                ItemStack itemStack1 = DataService.removeItemFromOffer(p, ID);

                p.sendMessage(ChatUtils.format("&aUsunięto przedmiot!"));

                if(itemStack1 != null)  p.getInventory().addItem(itemStack1);

                LocalPlayerData pLocalData = DataService.getPlayerLocalData(p);

                TaskUtils.runTaskLater(() -> new PlayerItemsGui(p, pLocalData).open(), 0L);
            }, 0L);
        });
        this.icon = icon;
    }

    private static ItemStack addPrice(ItemStack is, double price){
        ItemMeta iMeta = is.getItemMeta();
        List<String> lore = iMeta.getLore();

        if(lore == null) lore = new ArrayList<>();

        lore.add("");
        lore.add(ChatUtils.format(priceTemplate.replace("{PRICE}", DoubleFormatter.format(price))));
        lore.add("");
        lore.add(ChatUtils.format(removeTemplate));

        iMeta.setLore(lore);
        is.setItemMeta(iMeta);

        return is;
    }
}
