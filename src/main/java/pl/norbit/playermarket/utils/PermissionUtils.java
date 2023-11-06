package pl.norbit.playermarket.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionUtils {

    public static boolean hasPermission(String perm, CommandSender sender){

        if(sender.isOp()) return true;

        return sender.hasPermission(perm);
    }
}
