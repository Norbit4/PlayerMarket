package pl.norbit.playermarket.utils.custom;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.plugins.PluginService;

public class CustomItemsUtils {
    private record CustomItem(String id, ItemType type){}

    public static ItemStack getItemStack(String configId){
        return getItemStack(configId, 1);
    }

    public static String getName(String configId){
        ItemStack itemStack = getItemStack(configId);

        if(itemStack == null){
            return configId + " (null)";
        }

        return PlainTextComponentSerializer.plainText()
                .serialize(itemStack.displayName())
                .replace("[", "")
                .replace("]", "");
    }

    private static void sendUnsupportedWarning(String configId, ItemType itemType) {
        PlayerMarket.getInstance().getLogger().warning(
                "Cannot use item '" + configId + "' because the required plugin '" +
                        itemType.getRequiredPlugin().getPluginName() +
                        "' is not installed or enabled."
        );
    }

    public static ItemStack getItemStack(String configId, int amount){
        CustomItem customItem = getCustomItem(configId);

        if(customItem == null){
            return null;
        }

        if (!PluginService.supports(customItem.type())) {
            sendUnsupportedWarning(configId, customItem.type());
            return null;
        }

        String id = customItem.id();

        return switch (customItem.type()) {
            case MATERIAL -> {
                Material material = Material.getMaterial(id);
                yield material == null ? null : new ItemStack(material, amount);
            }
            case ITEMS_ADDER -> updateStack(ItemsAdderUtils.getItem(id), amount);
            case NEXO -> updateStack(NexoUtils.getItem(id), amount);
            case MMO_ITEMS -> updateStack(MmoItemsUtils.getItem(id), amount);
            case ORAXEN -> updateStack(OraxenUtils.getItem(id), amount);
            case CRAFT_ENGINE -> updateStack(CraftEngineUtils.getItem(id), amount);
            case MYTHIC_MOBS -> updateStack(MythicUtils.getItem(id), amount);
        };
    }

    public static boolean isEqual(String configId, ItemStack itemStack){
        return isEqual(configId, itemStack, -1);
    }

    public static boolean isEqual(String configId, ItemStack itemStack, int amount) {
        if (itemStack == null) {
            return false;
        }

        CustomItem customItem = getCustomItem(configId);

        if(customItem == null){
            return false;
        }

        if (!PluginService.supports(customItem.type())) {
            sendUnsupportedWarning(configId, customItem.type());
            return false;
        }

        String id = customItem.id();

        boolean matches = switch (customItem.type()) {
            case MATERIAL -> itemStack.getType() == Material.getMaterial(id);
            case ITEMS_ADDER -> ItemsAdderUtils.isEqual(itemStack, id);
            case NEXO -> NexoUtils.isEqual(itemStack, id);
            case MMO_ITEMS -> MmoItemsUtils.isEqual(itemStack, id);
            case ORAXEN -> OraxenUtils.isEqual(itemStack, id);
            case CRAFT_ENGINE -> CraftEngineUtils.isEqual(itemStack, id);
            case MYTHIC_MOBS -> MythicUtils.isEqual(itemStack, id);
        };

        if(amount == -1){
            return matches;
        }

        return matches && itemStack.getAmount() >= amount;
    }

    private static ItemStack updateStack(ItemStack itemStack, int amount){
        if(itemStack == null){
            return null;
        }
        itemStack.setAmount(amount);
        return itemStack;
    }

    private static CustomItem getCustomItem(String id){
        String[] split = id.split(":");

        String type = split[0];

        if(split.length < 2){
            return new CustomItem(type, ItemType.MATERIAL);
        }
        String customId = split[1];

        if(type.equalsIgnoreCase("IA") || type.equalsIgnoreCase("ITEMSADDER")){
            return new CustomItem(customId, ItemType.ITEMS_ADDER);
        }
        else if(type.equalsIgnoreCase("NEXO")){
            return new CustomItem(customId, ItemType.NEXO);
        }
        else if(type.equalsIgnoreCase("MMOITEMS")){
            if(split.length < 3){
                return null;
            }
            return new CustomItem(split[1] + ":" + split[2], ItemType.MMO_ITEMS);
        }
        else if(type.equalsIgnoreCase("ORAXEN") || type.equalsIgnoreCase("OX")){
            return new CustomItem(customId, ItemType.ORAXEN);
        }
        else if(type.equalsIgnoreCase("CRAFTENGINE") || type.equalsIgnoreCase("CE")){
            return new CustomItem(customId, ItemType.CRAFT_ENGINE);
        }
        else if(type.equalsIgnoreCase("MYTHICMOBS") || type.equalsIgnoreCase("MM")){
            return new CustomItem(customId, ItemType.MYTHIC_MOBS);
        }
        else{
            return new CustomItem(type, ItemType.MATERIAL);
        }
    }
}
