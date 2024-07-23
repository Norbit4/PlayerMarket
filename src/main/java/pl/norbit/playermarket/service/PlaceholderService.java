package pl.norbit.playermarket.service;

import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.placeholders.PlaceholderRegistry;
import pl.norbit.playermarket.placeholders.PlaceholderVault;
import pl.norbit.playermarket.utils.PluginUtils;

public class PlaceholderService {

    private PlaceholderService() {
        throw new IllegalStateException("Utility class");
    }

    public static void registerPlaceholders() {
        Settings.PLACEHOLDERAPI_IS_ENABLED = PluginUtils.checkPlugin("PlaceholderAPI");

        if(!Settings.PLACEHOLDERAPI_IS_ENABLED){
            return;
        }

        PlaceholderVault.start();
        new PlaceholderRegistry().register();
    }
}
