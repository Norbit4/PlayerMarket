package pl.norbit.playermarket.utils;


import pl.norbit.playermarket.PlayerMarket;

public class TaskUtils {

    private TaskUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void sync(Runnable runnable){
        PlayerMarket inst = PlayerMarket.getInstance();
        inst.getServer().getScheduler().runTask(inst, runnable);
    }

    public static void async(Runnable runnable){
        PlayerMarket inst = PlayerMarket.getInstance();
        inst.getServer().getScheduler().runTaskAsynchronously(inst, runnable);
    }
    public static void asyncLater(Runnable runnable, long delay){
        PlayerMarket inst = PlayerMarket.getInstance();
        inst.getServer().getScheduler().runTaskLaterAsynchronously(inst, runnable, delay);
    }

    public static void asyncTimer(Runnable runnable, long delay, long period){
        PlayerMarket inst = PlayerMarket.getInstance();
        inst.getServer().getScheduler().runTaskTimerAsynchronously(inst, runnable, delay, period);
    }
}
