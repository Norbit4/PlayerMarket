package pl.norbit.playermarket.service;

import org.bukkit.entity.Player;
import pl.norbit.playermarket.cache.PlayerDataCache;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.utils.TaskUtils;
import pl.norbit.playermarket.utils.format.ChatUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;
import pl.norbit.playermarket.utils.player.PlayerUtils;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class JoinService {
    private static final Queue<UUID> QUEUE = new ConcurrentLinkedQueue<>();
    private static final int MAX_PER_TICK = 20;

    private JoinService() {}

    public static void init() {
        TaskUtils.asyncTimer(JoinService::tick, 20L, 30L);
    }

    public static void queue(Player player) {
        QUEUE.add(player.getUniqueId());
    }

    private static void tick() {
        if (!DataService.isReady()) {
            return;
        }

        for (int i = 0; i < MAX_PER_TICK; i++) {
            UUID playerUUID = QUEUE.poll();

            if (playerUUID == null) {
                break;
            }

            process(playerUUID);
        }
    }

    private static void process(UUID playerUUID) {
        Player p = PlayerUtils.getPlayer(playerUUID);

        if (p == null || !p.isOnline()) {
            return;
        }

        DataService.getPlayerDataCreate(p).thenAccept(playerData -> {
            PlayerDataCache.loadCache(playerData);

            int soldItems = playerData.getSoldItems();

            if (soldItems <= 0 || !p.isOnline()) {
                return;
            }

            String joinMessage = Settings.JOIN_MESSAGE
                    .replace("{MONEY}", DoubleFormatter.format(playerData.getEarnedMoney()))
                    .replace("{SOLD}", String.valueOf(playerData.getSoldItems()));

            p.sendMessage(ChatUtils.format(p, joinMessage));
        });
    }
}
