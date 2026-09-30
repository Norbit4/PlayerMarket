package pl.norbit.playermarket.utils.format;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.plugins.PluginHook;
import pl.norbit.playermarket.plugins.PluginService;

public class ChatUtils {
    private ChatUtils() {}

    public static String format(Player p, String text) {
        if(PluginService.isEnabled(PluginHook.PLACEHOLDER_API)) {
            text = PlaceholderAPI.setPlaceholders(p, text);
        }

        Component component = MiniMessage.miniMessage().deserialize(text);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static String format(String text) {
        if(PluginService.isEnabled(PluginHook.PLACEHOLDER_API)) {
            text = PlaceholderAPI.setPlaceholders(null, text);
        }

        Component component = MiniMessage.miniMessage().deserialize(text);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static String format(String message, Player p){
        return PlaceholderAPI.setPlaceholders(p, message);
    }
}
