package pl.norbit.playermarket.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.config.Settings;

public class ChatUtils {

    private ChatUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String format(Player p, String text) {
        if(Settings.PLACEHOLDERAPI_IS_ENABLED) {
            text = PlaceholderAPI.setPlaceholders(p, text);
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String format(String text) {
        if(Settings.PLACEHOLDERAPI_IS_ENABLED) {
            text = PlaceholderAPI.setPlaceholders(null, text);
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String format(String message, Player p){
        return PlaceholderAPI.setPlaceholders(p, message);
    }
}
