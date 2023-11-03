package pl.norbit.playermarket;

import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.plugin.java.JavaPlugin;
import pl.norbit.playermarket.commands.MarketCMD;
import pl.norbit.playermarket.commands.OfferItemCMD;
import pl.norbit.playermarket.config.category.CategoryConfig;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.listeners.OnPlayerJoin;
import pl.norbit.playermarket.service.MarketService;
import pl.norbit.playermarket.utils.TaskUtils;

public final class PlayerMarket extends JavaPlugin {

    private static PlayerMarket instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        EconomyService.load();

        CategoryConfig.generateDefaults();

        new InventoryAPI(this).init();
        Settings.load(false);

        getCommand("market").setExecutor(new MarketCMD());
        getCommand("wystaw").setExecutor(new OfferItemCMD());

        getServer().getPluginManager().registerEvents(new OnPlayerJoin(), this);

        TaskUtils.runTaskLaterAsynchronously(() -> {
            DataService.start();
            MarketService.start();
        }, 0L);
    }

    @Override
    public void onDisable() {
        DataService.close();
    }

    public static PlayerMarket getInstance() {
        return instance;
    }
}
