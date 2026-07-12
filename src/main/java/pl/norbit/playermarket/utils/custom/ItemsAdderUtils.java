package pl.norbit.playermarket.utils.custom;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderUtils {

    private ItemsAdderUtils() {}

    protected static ItemStack getItem(String id){
        CustomStack stack = CustomStack.getInstance(id);

        if(stack == null) return null;

        return stack.getItemStack();
    }

    protected static boolean isEqual(ItemStack stack1, String id) {
        CustomStack stack = CustomStack.getInstance(id);

        if(stack == null) return false;

        CustomStack customStack = CustomStack.byItemStack(stack1);

        if(customStack == null) return false;

        return customStack.matchNamespacedID(stack);
    }
}
