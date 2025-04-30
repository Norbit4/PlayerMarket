package pl.norbit.playermarket.model.local;

import lombok.Data;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.utils.item.CustomItem;

import java.util.List;

@Data
public class ConfigIcon {

    private String name;
    private CustomItem customItem;
    private List<String> lore;
    private int slot;

    public Icon getIcon(){
        ItemStack itemStack = customItem.getItemStack();

        if(itemStack == null){
            return new Icon(Material.BARRIER)
                    .setName("&cError")
                    .setLore("&cItem not found");
        }

        Icon icon = new Icon(itemStack);

        icon.setName(name);
        icon.setLore(lore);

        return icon;
    }
}
