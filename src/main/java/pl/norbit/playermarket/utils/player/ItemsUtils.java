package pl.norbit.playermarket.utils.player;

import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemsUtils {

    private ItemsUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<ItemStack> getShulkerBoxInv(ItemStack itemStack) {
        if(itemStack == null) {
            return List.of();
        }

        if(itemStack.getItemMeta() == null) {
            return List.of();
        }

        BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();

        if(meta instanceof ShulkerBox) {
            return List.of();
        }

        ShulkerBox shulkerBox = (ShulkerBox) meta.getBlockState();
        Inventory inventory = shulkerBox.getInventory();

        return Stream.of(inventory.getContents())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static boolean isShulkerBox(ItemStack itemStack) {
        return itemStack.getItemMeta() instanceof BlockStateMeta && ((BlockStateMeta) itemStack.getItemMeta()).getBlockState() instanceof ShulkerBox;
    }
}
