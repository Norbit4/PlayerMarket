package pl.norbit.playermarket.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.PlayerMarket;

public class TaskUtils {

    private TaskUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void sync(Runnable runnable) {
        PlayerMarket plugin = PlayerMarket.getInstance();

        Bukkit.getGlobalRegionScheduler().run(
                plugin,
                task -> runnable.run()
        );
    }

    public static void sync(Player player, Runnable runnable) {
        PlayerMarket plugin = PlayerMarket.getInstance();

        player.getScheduler().run(
                plugin,
                task -> runnable.run(),
                null
        );
    }

    public static void async(Runnable runnable) {
        PlayerMarket plugin = PlayerMarket.getInstance();

        Bukkit.getAsyncScheduler().runNow(
                plugin,
                task -> runnable.run()
        );
    }

    public static void asyncLater(Runnable runnable, long delay) {
        PlayerMarket plugin = PlayerMarket.getInstance();

        long delayMillis = delay * 50L;

        Bukkit.getAsyncScheduler().runDelayed(
                plugin,
                task -> runnable.run(),
                delayMillis,
                java.util.concurrent.TimeUnit.MILLISECONDS
        );
    }

    public static void asyncTimer(
            Runnable runnable,
            long delay,
            long period
    ) {
        PlayerMarket plugin = PlayerMarket.getInstance();

        long delayMillis = delay * 50L;
        long periodMillis = period * 50L;

        Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                task -> runnable.run(),
                delayMillis,
                periodMillis,
                java.util.concurrent.TimeUnit.MILLISECONDS
        );
    }
}