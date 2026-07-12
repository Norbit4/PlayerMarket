package pl.norbit.playermarket.utils;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.utils.custom.CustomItemsUtils;

public class BlackListUtils {

    private BlackListUtils() {}

    public static boolean isBlackListed(ItemStack itemStack) {
        if(!Settings.isBlacklistEnabled()){
            return false;
        }

        if (itemStack == null) {
            return false;
        }

        String itemType = itemStack.getType().name();

        for (String blacklistItem : Settings.getBlacklistItems()) {
            if(CustomItemsUtils.isEqual(blacklistItem, itemStack)){
                return true;
            }

            if (blacklistItem.equalsIgnoreCase(itemType)) {
                return true;
            }

            String displayName = itemStack.getItemMeta().getDisplayName();

            if(displayName.isBlank()){
                continue;
            }

            String strippedName = ChatColor.stripColor(displayName);

            if (blacklistItem.equalsIgnoreCase(strippedName)) {
                return true;
            }
        }

        return false;
    }
}
