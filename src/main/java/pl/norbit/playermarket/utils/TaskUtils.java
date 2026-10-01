package pl.norbit.playermarket.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import pl.norbit.playermarket.PlayerMarket;

import java.util.concurrent.TimeUnit;

public class TaskUtils {
    private TaskUtils() {}

    public static void sync(Runnable runnable) {
        PlayerMarket plugin = PlayerMarket.getInstance();

        plugin.getServer().getGlobalRegionScheduler().run(
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

        plugin.getServer().getAsyncScheduler().runNow(
                plugin,
                task -> runnable.run()
        );
    }

    public static ScheduledTask asyncTimer(
            Runnable runnable,
            long delay,
            long period
    ) {
        PlayerMarket plugin = PlayerMarket.getInstance();

        long delayMillis = delay * 50L;
        long periodMillis = period * 50L;

        return plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin,
                task -> runnable.run(),
                delayMillis,
                periodMillis,
                TimeUnit.MILLISECONDS
        );
    }
}