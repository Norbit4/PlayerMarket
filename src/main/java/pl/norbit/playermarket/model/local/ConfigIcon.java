package pl.norbit.playermarket.model.local;

import lombok.Data;
import mc.obliviate.inventory.Icon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.utils.custom.CustomItemsUtils;
import pl.norbit.playermarket.utils.format.ChatUtils;

import java.util.List;

@Data
public class ConfigIcon {

    private String name;
    private String configId;
    private List<String> lore;
    boolean enabled;
    private int slot;

    public ItemStack getItem() {
        ItemStack itemStack = CustomItemsUtils.getItemStack(configId);

        if(itemStack == null){
            return new ItemStack(Material.BARRIER);
        }
        return itemStack;
    }

    public Icon getIcon(){
        if (configId == null) {
            return new Icon(Material.BARRIER)
                    .setName(ChatUtils.formatLegacy("&cError"))
                    .setLore(ChatUtils.formatLegacy("&cItem not found"));
        }

        ItemStack itemStack = CustomItemsUtils.getItemStack(configId);

        if(itemStack == null){
            return new Icon(Material.BARRIER)
                    .setName(ChatUtils.formatLegacy("&cError"))
                    .setLore(ChatUtils.formatLegacy("&cItem not found"));
        }

        Icon icon = new Icon(itemStack);

        if(name != null){
            icon.setName(name);
        }

        icon.setLore(lore);

        return icon;
    }
}
