package pl.norbit.playermarket.utils;


import pl.norbit.playermarket.PlayerMarket;

public class TaskUtils {

    public static void runTaskLater(Runnable runnable, long delay){
        PlayerMarket inst = PlayerMarket.getInstance();
        inst.getServer().getScheduler().runTaskLater(inst, runnable, delay);
    }
    public static void runTaskTimer(Runnable runnable, long delay, long period){
        PlayerMarket inst = PlayerMarket.getInstance();
        inst.getServer().getScheduler().runTaskTimer(inst, runnable, delay, period);
    }

    public static void runTaskLaterAsynchronously(Runnable runnable, long delay){
        PlayerMarket inst = PlayerMarket.getInstance();
        inst.getServer().getScheduler().runTaskLaterAsynchronously(inst, runnable, delay);
    }

    public static void runTaskTimerAsynchronously(Runnable runnable, long delay, long period){
        PlayerMarket inst = PlayerMarket.getInstance();
        inst.getServer().getScheduler().runTaskTimerAsynchronously(inst, runnable, delay, period);
    }
}
