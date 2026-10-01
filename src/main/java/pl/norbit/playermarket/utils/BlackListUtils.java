package pl.norbit.playermarket.utils;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.utils.custom.CustomItemsUtils;

public class BlackListUtils {
    private BlackListUtils() {}

    public static boolean isBlackListed(ItemStack itemStack) {
        if (!Settings.isBlacklistEnabled()) {
            return false;
        }

        if (itemStack == null) {
            return false;
        }

        String itemType = itemStack.getType().name();
        boolean matchesList = false;

        for (String blacklistItem : Settings.getBlacklistItems()) {
            if (CustomItemsUtils.isEqual(blacklistItem, itemStack)) {
                matchesList = true;
                break;
            }
            if (blacklistItem.equalsIgnoreCase(itemType)) {
                matchesList = true;
                break;
            }
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
                String displayName = itemStack.getItemMeta().getDisplayName();

                if (!displayName.isBlank()) {
                    String strippedName = ChatColor.stripColor(displayName);

                    if (blacklistItem.equalsIgnoreCase(strippedName)) {
                        matchesList = true;
                        break;
                    }
                }
            }
        }
        return Settings.isWhitelistMode() != matchesList;
    }
}
