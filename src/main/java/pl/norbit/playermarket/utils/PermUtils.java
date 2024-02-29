package pl.norbit.playermarket.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;
import java.util.stream.Collectors;

public class PermUtils {

    public static boolean hasPermission(String perm, CommandSender sender){
        if(sender.isOp()) return true;

        return sender.hasPermission(perm);
    }

    public static boolean hasPermission(String perm, Player sender){
        if(sender.isOp()) return true;

        return sender.hasPermission(perm);
    }

    public static int getAmount(Player p, String perm, int defaultAmount){
        List<String> result = getPermissionsContains(p, perm);

        if(result.isEmpty()) return defaultAmount;

        return result.stream()
                .map(r -> r.split("\\."))
                .map(split -> split[split.length - 1])
                .map(Integer::parseInt)
                .max(Integer::compareTo)
                .orElse(defaultAmount);
    }
    private static List<String> getPermissionsContains(Player p, String perm) {
        return p.getEffectivePermissions()
                .stream()
                .map(PermissionAttachmentInfo::getPermission).
                filter(permS -> permS.contains(perm))
                .collect(Collectors.toList());

    }
}
