package pl.norbit.playermarket.logs;

import pl.norbit.playermarket.config.discord.DiscordConfig;
import pl.norbit.playermarket.config.discord.DiscordEmbed;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.model.MarketItemData;
import pl.norbit.playermarket.utils.TaskUtils;
import pl.norbit.playermarket.utils.format.DoubleFormatter;

public class DiscordLogs {

    private DiscordLogs() {
        throw new IllegalStateException("Utility class");
    }

    public static void buyItem(String playerName, MarketItemData marketItemData){
        DiscordConfig discordConfig = Settings.getDiscordConfig();

        if(!discordConfig.isEnabled()){
            return;
        }

        send(discordConfig, discordConfig.getBuyEmbed(), marketItemData, playerName);
    }

    public static void offerItem(MarketItemData marketItemData){
        DiscordConfig discordConfig = Settings.getDiscordConfig();

        if(!discordConfig.isEnabled()){
            return;
        }

        send(discordConfig, discordConfig.getOfferEmbed(), marketItemData, null);
    }

    private static void send(DiscordConfig dcConfig, DiscordEmbed dcEmbed, MarketItemData item, String playerName){
        if(dcEmbed == null){
            return;
        }

        if(!dcEmbed.isEnabled()){
            return;
        }

        String ownerName = item.getOwnerName();
        double price = item.getPrice();

        int color = dcEmbed.getColor();

        if (playerName == null) {
            playerName = "";
        }

        String finalPlayerName = playerName;
        TaskUtils.async(() ->{
            String messages = dcEmbed.getMessage()
                    .replace("{PLAYER}", finalPlayerName)
                    .replace("{SELLER}", ownerName)
                    .replace("{ITEM}", item.getItemName())
                    .replace("{PRICE}", DoubleFormatter.format(price));

            try {
                DiscordWebhook.send(dcConfig, messages, color);
            } catch (Exception e){
                LogService.warn("Error while sending discord message: " + e.getMessage());
            }
        });
    }
}
