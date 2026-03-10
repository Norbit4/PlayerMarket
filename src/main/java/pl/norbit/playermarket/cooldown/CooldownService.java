package pl.norbit.playermarket.cooldown;

import pl.norbit.playermarket.config.Settings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownService {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Map<UUID, ClickData> clickMap = new HashMap<>();

    private CooldownService() {}

    public static boolean isOnCooldown(UUID playerUUID) {
        return cooldowns.containsKey(playerUUID)
                && (System.currentTimeMillis() - cooldowns.get(playerUUID)) < Settings.getCooldownTime();
    }

    public static void updateCooldown(UUID playerUUID) {
        cooldowns.put(playerUUID, System.currentTimeMillis());
    }

    public static void clearCooldown(UUID playerUUID) {
        cooldowns.remove(playerUUID);
        clickMap.remove(playerUUID);
    }

    public static boolean tryClick(UUID playerUUID) {
        long now = System.currentTimeMillis();

        ClickData data = clickMap.computeIfAbsent(playerUUID, k -> new ClickData(now, 0));

        if (now - data.windowStart >= 1000) {
            data.windowStart = now;
            data.clicks = 0;
        }

        data.clicks++;

        return data.clicks <= Settings.getClicksPerSecond();
    }

    private static class ClickData {
        long windowStart;
        int clicks;

        ClickData(long windowStart, int clicks) {
            this.windowStart = windowStart;
            this.clicks = clicks;
        }
    }
}
