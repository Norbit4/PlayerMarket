package pl.norbit.playermarket.utils.economy;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;

public class PlayerPointsUtils {
    private static PlayerPointsAPI playerPointsAPI;

    private PlayerPointsUtils() {
        throw new IllegalStateException("Utility class");
    }

    protected static void load(){
        playerPointsAPI = PlayerPoints.getInstance().getAPI();
    }

    protected static boolean withDrawIfPossible(Player p, int points){
        if(playerPointsAPI.look(p.getUniqueId()) < points){
            return false;
        }

        playerPointsAPI.take(p.getUniqueId(), points);
        return true;
    }

    protected static void addPoints(Player p, int points){

        playerPointsAPI.give(p.getUniqueId(), points);
    }
}
