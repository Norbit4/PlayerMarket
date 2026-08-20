package pl.norbit.playermarket.utils.format;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.plugins.PluginHook;
import pl.norbit.playermarket.plugins.PluginService;

public class ChatUtils {
    private static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    private ChatUtils() {}

    public static String format(Player p, String text) {
        if(PluginService.isEnabled(PluginHook.PLACEHOLDER_API)) {
            text = PlaceholderAPI.setPlaceholders(p, text);
        }

        if (text.contains("<") && text.contains(">")) {
            Component component = MiniMessage.miniMessage().deserialize(text);
            return LegacyComponentSerializer.legacySection().serialize(component);
        }

        return translateColorCodes(text);
    }

    public static String format(String text) {
        if(PluginService.isEnabled(PluginHook.PLACEHOLDER_API)) {
            text = PlaceholderAPI.setPlaceholders(null, text);
        }
        if (text.contains("<") && text.contains(">")) {
            Component component = MiniMessage.miniMessage().deserialize(text);
            return LegacyComponentSerializer.legacySection().serialize(component);
        }

        return translateColorCodes(text);
    }

    private static String translateColorCodes(String text) {
        String[] texts = text.split(String.format(WITH_DELIMITER, "&"));

        StringBuilder finalText = new StringBuilder();

        for (int i = 0; i < texts.length; i++) {
            if (texts[i].equalsIgnoreCase("&")) {
                if (++i >= texts.length) {
                    finalText.append("&");
                    break;
                }

                if (texts[i].charAt(0) == '#') {
                    if (texts[i].length() >= 7) {
                        String hex = texts[i].substring(0, 7);

                        try {
                            finalText.append(net.md_5.bungee.api.ChatColor.of(hex))
                                    .append(texts[i].substring(7));
                        } catch (IllegalArgumentException ex) {
                            finalText.append("&").append(texts[i]);
                        }
                    } else {
                        finalText.append("&").append(texts[i]);
                    }
                } else {
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]));
                }
            } else {
                finalText.append(texts[i]);
            }
        }
        return finalText.toString();
    }

    public static String format(String message, Player p){
        return PlaceholderAPI.setPlaceholders(p, message);
    }
}
