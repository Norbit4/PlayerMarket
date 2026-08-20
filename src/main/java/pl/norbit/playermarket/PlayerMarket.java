package pl.norbit.playermarket;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import mc.obliviate.inventory.InventoryAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import pl.norbit.playermarket.cache.PlayerDataCache;
import pl.norbit.playermarket.commands.MainCommand;
import pl.norbit.playermarket.commands.OfferCommand;
import pl.norbit.playermarket.commands.MarketCommand;
import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.config.category.CategoryConfig;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.listeners.OnPlayerJoin;
import pl.norbit.playermarket.listeners.OnPlayerQuit;
import pl.norbit.playermarket.placeholders.PlaceholderRegistry;
import pl.norbit.playermarket.plugins.PluginHook;
import pl.norbit.playermarket.plugins.PluginService;
import pl.norbit.playermarket.service.JoinService;
import pl.norbit.playermarket.service.MarketService;
import pl.norbit.playermarket.utils.economy.EconomyUtils;

import static pl.norbit.playermarket.utils.TaskUtils.async;

public final class PlayerMarket extends JavaPlugin {

    @Getter
    @Setter(value = AccessLevel.PRIVATE)
    private static PlayerMarket instance;
    @Override
    public void onEnable() {
        setInstance(this);

        CategoryConfig.generateDefaults();

        new InventoryAPI(this).init();
        Settings.load(false);

        PluginService.load(this);
        EconomyUtils.load();

        registerCommands();
        registerEvents();

        JoinService.init();

        if(PluginService.isEnabled(PluginHook.PLACEHOLDER_API)){
            new PlaceholderRegistry().register();
        }

        async(() -> {
            DataService.start();
            MarketService.start();
            PlayerDataCache.start();
        });

        loadBStats();
    }

    public void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new OnPlayerJoin(), this);
        pluginManager.registerEvents(new OnPlayerQuit(), this);
    }

    public void registerCommands() {
        PaperCommandManager<CommandSourceStack> commandManager = PaperCommandManager.builder()
                    .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                    .buildOnEnable(this);

        AnnotationParser<CommandSourceStack> annotationParser = new AnnotationParser<>
                (commandManager, CommandSourceStack.class);

        new MarketCommand(commandManager).register();
        new OfferCommand(commandManager).register();

        annotationParser.parse(new MainCommand());
    }

    private void loadBStats(){
        new Metrics(this, 32580);
    }

    @Override
    public void onDisable() {
        DataService.close();
    }
}
