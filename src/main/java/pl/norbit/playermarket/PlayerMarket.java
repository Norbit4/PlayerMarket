package pl.norbit.playermarket;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pl.norbit.playermarket.commands.MainCommand;
import pl.norbit.playermarket.commands.MarketCommand;
import pl.norbit.playermarket.commands.OfferCommand;
import pl.norbit.playermarket.config.category.CategoryConfig;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.listeners.OnPlayerJoin;
import pl.norbit.playermarket.service.MarketService;
import pl.norbit.playermarket.utils.TaskUtils;

public final class PlayerMarket extends JavaPlugin {

    @Getter
    @Setter(value = AccessLevel.PRIVATE)
    private static PlayerMarket instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        setInstance(this);

        EconomyService.load();
        CategoryConfig.generateDefaults();

        new InventoryAPI(this).init();
        Settings.load(false);

        registerCommands();
        registerEvents();

        TaskUtils.runTaskLater(() -> {
            DataService.start();
            MarketService.start();
        }, 0L);
    }

    public void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new OnPlayerJoin(), this);
    }

    public void registerCommands() {
        new MarketCommand().register();
        new OfferCommand().register();
        new MainCommand().register();
    }

    @Override
    public void onDisable() {
        DataService.close();
    }
}
