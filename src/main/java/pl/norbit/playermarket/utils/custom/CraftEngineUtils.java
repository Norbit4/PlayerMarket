package pl.norbit.playermarket.utils.custom;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import org.bukkit.inventory.ItemStack;

public class CraftEngineUtils {

    private CraftEngineUtils() {}

    protected static ItemStack getItem(String id){
        BukkitItemDefinition bukkitItemDefinition = CraftEngineItems.byId(id);

        if(bukkitItemDefinition == null) return null;

        return bukkitItemDefinition.buildBukkitItem();
    }

    protected static boolean isEqual(ItemStack stack1, String id) {
        BukkitItemDefinition bukkitItemDefinition = CraftEngineItems.byId(id);

        if(bukkitItemDefinition == null) return false;

        BukkitItemDefinition bukkitItemDefinition1 = CraftEngineItems.byItemStack(stack1);

        if(bukkitItemDefinition1 == null) return false;

        return bukkitItemDefinition.equals(bukkitItemDefinition1);
    }
}
