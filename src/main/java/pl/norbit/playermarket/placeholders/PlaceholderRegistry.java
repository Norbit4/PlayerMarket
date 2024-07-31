package pl.norbit.playermarket.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.model.PlayerData;
import pl.norbit.playermarket.model.local.LocalPlayerData;
import pl.norbit.playermarket.utils.DoubleFormatter;
import pl.norbit.playermarket.utils.PermUtils;

public class PlaceholderRegistry extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "playermarket";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Norbit";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.contains("player_limit")) {

            if(!player.isOnline()){
                return "";
            }

            Player p = player.getPlayer();

            int amount = PermUtils.getAmount(p, Settings.OFFER_COMMAND_LIMIT_PERMISSION, Settings.OFFER_COMMAND_DEFAULT_LIMIT);

            return String.valueOf(amount);
        }else if (params.contains("player_offers")) {
            LocalPlayerData pLocalData = PlaceholderVault.getLocalPlayerData(player);

            return String.valueOf(pLocalData.getPlayerOffers().size());
        }else if (params.contains("player_sold")) {
            PlayerData playerData = getPlayerData(player);

            return String.valueOf(playerData.getSoldItems());
        }else if (params.contains("player_earned")) {
            PlayerData playerData = getPlayerData(player);

            return DoubleFormatter.format(playerData.getEarnedMoney());
        }else if (params.contains("player_all_sold")) {
            PlayerData playerData = getPlayerData(player);

            return String.valueOf(playerData.getTotalSoldItems());
        }else if (params.contains("player_all_earned")) {
            PlayerData playerData = getPlayerData(player);

            return DoubleFormatter.format(playerData.getTotalEarnedMoney());
        }
        return "";
    }
    private static PlayerData getPlayerData(OfflinePlayer player) {
        LocalPlayerData pLocalData = PlaceholderVault.getLocalPlayerData(player);
        return pLocalData.getPlayerData();
    }
}
