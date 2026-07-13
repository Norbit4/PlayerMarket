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
    private int slot;

    public Icon getIcon(){
        ItemStack itemStack = CustomItemsUtils.getItemStack(configId);

        if(itemStack == null){
            return new Icon(Material.BARRIER)
                    .setName(ChatUtils.format("&cError"))
                    .setLore(ChatUtils.format("&cItem not found"));
        }

        Icon icon = new Icon(itemStack);

        icon.setName(name);
        icon.setLore(lore);

        return icon;
    }
}
