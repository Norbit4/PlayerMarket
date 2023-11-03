package pl.norbit.playermarket.utils;

import org.bukkit.ChatColor;

public class ChatUtils {
    public static String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
