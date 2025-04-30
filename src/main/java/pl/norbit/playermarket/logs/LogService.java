package pl.norbit.playermarket.logs;

import org.bukkit.Server;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.Settings;

public class LogService {

    private static final String TEMPLATE = "[MARKET] {MESSAGE}";

    private LogService() {
        throw new IllegalStateException("Utility class");
    }

    public static void log(String message){
        if(!Settings.DEBUG){
            return;
        }

        Server server = PlayerMarket.getInstance().getServer();
        String messageFormatted = TEMPLATE.replace("{MESSAGE}", message);

        server.getLogger().info(messageFormatted);
    }

    public static void warn(String message){
        if(!Settings.DEBUG){
            return;
        }

        Server server = PlayerMarket.getInstance().getServer();
        String messageFormatted = TEMPLATE.replace("{MESSAGE}", message);

        server.getLogger().warning(messageFormatted);
    }
}
