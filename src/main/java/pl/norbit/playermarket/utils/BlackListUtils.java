package pl.norbit.playermarket.utils;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;

public class BlackListUtils {

    private BlackListUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isBlackListed(ItemStack itemStack) {
        if(!Settings.isBlacklistEnabled()){
            return false;
        }

        if (itemStack == null) {
            return false;
        }

        String itemType = itemStack.getType().name();

        for (String blacklistItem : Settings.getBlacklistItems()) {
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
