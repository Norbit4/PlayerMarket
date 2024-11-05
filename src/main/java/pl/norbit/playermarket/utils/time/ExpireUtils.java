package pl.norbit.playermarket.utils.time;

import pl.norbit.playermarket.config.Settings;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ExpireUtils {
    private ExpireUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isExpired(long itemCreateTime) {
        return isExpired(itemCreateTime, Settings.getExpireTime());
    }

    private static boolean isExpired(long itemCreateDate, int expireMinutes) {
        if (expireMinutes <= 0) {
            return false;
        }
        return System.currentTimeMillis() - itemCreateDate > (long) expireMinutes * 60 * 1000;
    }

    public static String getRemainingTime(long itemCreateTime) {
        String remainingTime = getRemainingTime(itemCreateTime, Settings.getExpireTime());

        return remainingTime.replace("{DAYS}", Settings.getDays())
                .replace("{HOURS}", Settings.getHours())
                .replace("{MINUTES}", Settings.getMinutes())
                .replace("{SECONDS}", Settings.getSeconds());
    }

    private static String getRemainingTime(long itemCreateTime, int expirationMinutes) {
        if(expirationMinutes <= 0){
            return Settings.getExpireNever();
        }

        LocalDateTime itemAddedTime = Instant.ofEpochMilli(itemCreateTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime expirationTime = itemAddedTime.plusMinutes(expirationMinutes);

        Duration remainingDuration = Duration.between(LocalDateTime.now(), expirationTime);

        long totalSeconds = remainingDuration.getSeconds();
        if (totalSeconds <= 0) {
            return Settings.getExpireStatus();
        }

        long days = totalSeconds / (24 * 3600);
        totalSeconds %= (24 * 3600);
        long hours = totalSeconds / 3600;
        totalSeconds %= 3600;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append("{DAYS} ");
        }
        if (hours > 0) {
            result.append(hours).append("{HOURS} ");
        }
        if (minutes > 0) {
            result.append(minutes).append("{MINUTES} ");
        }

        if (hours == 0 && seconds > 0) {
            result.append(seconds).append("{SECONDS}");
        }
        return result.toString().trim();
    }
}
