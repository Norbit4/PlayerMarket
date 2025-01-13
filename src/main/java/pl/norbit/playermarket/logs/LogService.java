package pl.norbit.playermarket.logs;

import org.bukkit.Server;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.Settings;

public class LogService {

    private LogService() {
        throw new IllegalStateException("Utility class");
    }

    public static void log(String message){
        if(!Settings.DEBUG){
            return;
        }

        Server server = PlayerMarket.getInstance().getServer();
        server.getLogger().info("[PLAYERMARKET-LOG] " + message);
    }
}
