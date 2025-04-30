package pl.norbit.playermarket.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownService {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    private CooldownService() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isOnCooldown(UUID playerUUID) {
        return cooldowns.containsKey(playerUUID) && (System.currentTimeMillis() - cooldowns.get(playerUUID)) < 1200;
    }

    public static void updateCooldown(UUID playerUUID) {
        cooldowns.put(playerUUID, System.currentTimeMillis());
    }
}
