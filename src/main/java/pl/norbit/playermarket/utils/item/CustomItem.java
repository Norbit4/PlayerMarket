package pl.norbit.playermarket.utils.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Data
@AllArgsConstructor
public class CustomItem {
    private String materialId;

    public ItemStack getItemStack() {
        String[] split = materialId.split(":");

        if (split.length < 2) {
            Material material = Material.getMaterial(materialId.toUpperCase());

            if (material == null) {
                return null;
            }

            return new ItemStack(material);
        }

        String namespace = split[0];
        String materialName = split[1];

        ItemType type = getType(namespace);

        if(type == ItemType.ITEMS_ADDER) {
            return ItemsAdderUtils.getItem(materialName).orElse(null);
        } else {
            Material material = Material.getMaterial(materialName.toUpperCase());

            if (material == null) {
                return null;
            }

            return new ItemStack(material);
        }
    }

    public boolean isEqual(ItemStack itemStack) {
        String[] split = materialId.split(":");

        if (split.length < 2) {
            Material material = Material.getMaterial(materialId.toUpperCase());

            if (material == null) {
                return false;
            }

            return itemStack.getType() == material;
        }

        String namespace = split[0];
        String materialName = split[1];

        ItemType type = getType(namespace);

        if(type == ItemType.ITEMS_ADDER) {
            return ItemsAdderUtils.isEqual(itemStack, materialName);
        } else {
            Material material = Material.getMaterial(materialName.toUpperCase());

            if (material == null) {
                return false;
            }

            return itemStack.getType() == material;
        }
    }

    private ItemType getType(String nameSpace) {
        if (nameSpace.equalsIgnoreCase("ia")) {
            return ItemType.ITEMS_ADDER;
        }
        return ItemType.MINECRAFT;
    }

}
