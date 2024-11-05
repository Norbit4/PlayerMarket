package pl.norbit.playermarket.utils.format;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.config.Settings;

public class ChatUtils {
    private static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    private ChatUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String format(Player p, String text) {
        if(Settings.PLACEHOLDERAPI_IS_ENABLED) {
            text = PlaceholderAPI.setPlaceholders(p, text);
        }
        return translateColorCodes(text);
    }

    public static String format(String text) {
        if(Settings.PLACEHOLDERAPI_IS_ENABLED) {
            text = PlaceholderAPI.setPlaceholders(null, text);
        }
        return translateColorCodes(text);
    }

    /**
     * @param text The string of text to apply color/effects to
     * @return Returns a string of text with color/effects applied
     */
    private static String translateColorCodes(String text){
        String[] texts = text.split(String.format(WITH_DELIMITER, "&"));

        StringBuilder finalText = new StringBuilder();

        for (int i = 0; i < texts.length; i++){
            if (texts[i].equalsIgnoreCase("&")){
                //get the next string
                i++;
                if (texts[i].charAt(0) == '#'){
                    finalText
                            .append(net.md_5.bungee.api.ChatColor.of(texts[i].substring(0, 7)))
                            .append(texts[i].substring(7));
                }else{
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]));
                }
            }else{
                finalText.append(texts[i]);
            }
        }

        return finalText.toString();
    }

    public static String format(String message, Player p){
        return PlaceholderAPI.setPlaceholders(p, message);
    }
}