package pl.norbit.playermarket.utils.custom;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class MythicUtils {

    private MythicUtils() {}

    protected static ItemStack getItem(String id){
        Optional<MythicItem> item = MythicBukkit.inst().getItemManager().getItem(id);

        return item
                .map(mythicItem -> BukkitAdapter.adapt(mythicItem.generateItemStack(1)))
                .orElse(null);
    }

    protected static boolean isEqual(ItemStack stack1, String id) {
        String mythicTypeFromItem = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(stack1);

        return id.equals(mythicTypeFromItem);
    }
}
