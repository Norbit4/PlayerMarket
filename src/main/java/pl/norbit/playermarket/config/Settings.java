package pl.norbit.playermarket.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.discord.DiscordConfig;
import pl.norbit.playermarket.config.discord.DiscordEmbed;
import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.config.category.CategoryUtils;
import pl.norbit.playermarket.model.local.CategoryType;
import pl.norbit.playermarket.model.local.ConfigGui;
import pl.norbit.playermarket.utils.economy.EconomyUtils;

import java.util.List;

public class Settings {
    @Getter
    private static boolean debug;

    //database
    @Getter
    private static String type;
    @Getter
    private static String database;
    @Getter
    private static String host;
    @Getter
    private static String user;
    @Getter
    private static String password;
    @Getter
    private static String useSSL;

    //category
    @Getter
    private static List<Category> categories;
    @Getter
    private static Category otherCategory;
    @Getter
    private static Category allCategory;

    //gui
    @Getter
    private static ConfigGui marketGui;
    @Getter
    private static ConfigGui offersGui;
    @Getter
    private static ConfigGui buyGui;
    @Getter
    private static ConfigGui searchGui;
    @Getter
    private static ConfigGui shulkerGui;
    @Getter
    private static String categoryNameFormat;
    @Getter
    private static List<String> categorySelectedLore;
    @Getter
    private static List<String> marketOfferItemLore;
    @Getter
    private static List<String> marketOfferShulkerLore;
    @Getter
    private static List<String> playerOfferItemLore;
    @Getter
    private static List<String> playerOfferShulkerLore;

    //sell messages
    @Getter
    private static String joinMessage;

    //offer command
    @Getter
    private static boolean offerCommandPermissionEnabled;
    @Getter
    private static String offerCommandWrongItem;
    @Getter
    private static String offerCommandPermission;
    @Getter
    private static String offerCommandUsage;
    @Getter
    private static String offerCommandNoPermission;
    @Getter
    private static String offerCommandWrongPrice;
    @Getter
    private static String offerCommandSuccess;
    @Getter
    private static String offerCommandPrefix;
    @Getter
    private static String offerCommandArgumentName;
    //offer command - limit
    @Getter
    private static String offerCommandLimitPermission;
    @Getter
    private static String offerCommandLimitMessage;
    @Getter
    private static int offerCommandDefaultLimit;
    @Getter
    private static boolean offerCommandLimitEnabled;

    //market command
    @Getter
    private static String marketCommandPrefix;
    @Getter
    private static boolean marketCommandPermissionEnabled;
    @Getter
    private static String marketCommandPermission;
    @Getter
    private static String marketCommandNoPermission;

    //main command
    @Getter
    private static String mainCommandReloadPermission;
    @Getter
    private static String mainCommandHelpPermission;
    @Getter
    private static String mainCommandNoPermission;
    @Getter
    private static String mainCommandReloadMessage;
    @Getter
    private static List<String> mainCommandHelpMessage;
    @Getter
    private static List<String> mainCommandReloadInfo;

    @Getter
    private static String anvilTitle;
    @Getter
    private static String anvilEmpty;

    @Getter
    private static int expireTime;
    @Getter
    private static String expireStatus;
    @Getter
    private static String expireNever;
    @Getter
    private static String expireMessage;
    @Getter
    private static String days;
    @Getter
    private static String hours;
    @Getter
    private static String minutes;
    @Getter
    private static String seconds;
    @Getter
    private static String clearPermission;
    @Getter
    private static String clearSuccess;
    @Getter
    private static String playerNotFound;

    //tax
    @Getter
    private static boolean taxEnabled;
    @Getter
    private static double taxValue;
    @Getter
    private static String taxCommand;
    @Getter
    private static boolean taxCommandEnabled;

    //discord
    @Getter
    private static DiscordConfig discordConfig;

    //blacklist
    @Getter
    private static boolean blacklistEnabled;
    @Getter
    private static List<String> blacklistItems;
    @Getter
    private static String blacklistMessage;

    //cooldown
    @Getter
    private static String cooldownMessage;
    @Getter
    private static int cooldownTime;
    @Getter
    private static int clicksPerSecond;

    private Settings() {}

    public static void load(boolean reload){
        PlayerMarket instance = PlayerMarket.getInstance();

        if(!reload){
            instance.saveDefaultConfig();
        }

        if(reload){
            instance.reloadConfig();
        }

        FileConfiguration config = instance.getConfig();

        //database
        if(!reload) {
            type = config.getString("database.type");
            database = config.getString("database.database");
            host = config.getString("database.host");
            user = config.getString("database.user");
            password = config.getString("database.password");
            useSSL = config.getString("database.use-ssl");

            String type = config.getString("economy.type");
            String currency = config.getString("economy.currency");

            EconomyUtils.setEconomyType(type, currency);
        }

        debug = config.getBoolean("debug");

        //offer limit
        offerCommandLimitEnabled = config.getBoolean("offers-limit.enabled");
        offerCommandDefaultLimit = config.getInt("offers-limit.default-limit");
        offerCommandLimitPermission = config.getString("offers-limit.permission");
        offerCommandLimitMessage = config.getString("offers-limit.limit-message");

        ConfigurationSection configurationSection = config.getConfigurationSection("categories");

        if(configurationSection == null){
            return;
        }

        //category
        categories = CategoryUtils.getCategories(configurationSection);

        otherCategory = CategoryUtils.getDefaultCategory(config.getConfigurationSection("other-category"), CategoryType.OTHER);
        allCategory = CategoryUtils.getDefaultCategory(config.getConfigurationSection("all-category"), CategoryType.ALL);

        //gui
        marketGui = new ConfigGui(config,"market-gui",
                new String[0],
                new String[]{"your-offers-icon", "previous-page-icon", "next-page-icon", "search-icon", "border-icon"});

        buyGui = new ConfigGui(config,"buy-gui",
                new String[]{"item-sold-message", "not-enough-money-message", "success-message",
                        "player-is-owner-message", "inventory-full-message", "sell-item-to-owner"},
                new String[]{"accept-icon", "cancel-icon", "border-icon"});

        offersGui = new ConfigGui(config,"offers-gui",
                new String[]{"remove-offer-message", "nothing-to-get-message", "success-message", "inventory-full-message"},
                new String[]{"statistics-icon", "back-to-market-icon", "previous-page-icon", "next-page-icon", "border-icon"});


        searchGui = new ConfigGui(config,"search-gui",
                new String[0],
                new String[]{"previous-page-icon", "next-page-icon", "back-to-market-icon", "border-icon"});

        shulkerGui = new ConfigGui(config,"shulker-gui",
                new String[]{"item-sold-message"},
                new String[]{"back-icon", "buy-icon", "border-icon"});

        categoryNameFormat = config.getString("category-name-format");

        categorySelectedLore = config.getStringList("category-selected-lore");
        marketOfferItemLore = config.getStringList("market-offer-item-lore");
        marketOfferShulkerLore = config.getStringList("market-offer-shulker-lore");

        playerOfferItemLore = config.getStringList("player-offer-item-lore");
        playerOfferShulkerLore = config.getStringList("player-offer-shulker-lore");

        joinMessage = config.getString("info-messages.join");
        cooldownMessage = config.getString("info-messages.cooldown");

        //offer command
        offerCommandPermissionEnabled = config.getBoolean("offer-command.use-permission.enabled");
        offerCommandPermission = config.getString("offer-command.use-permission.permission");
        offerCommandNoPermission = config.getString("offer-command.use-permission.message");
        offerCommandUsage = config.getString("offer-command.usage");
        offerCommandWrongPrice = config.getString("offer-command.wrong-price");
        offerCommandSuccess = config.getString("offer-command.success");
        offerCommandWrongItem = config.getString("offer-command.wrong-item");

        offerCommandPrefix = config.getString("offer-command.prefix");
        offerCommandArgumentName = config.getString("offer-command.argument-name");

        //market command
        marketCommandPermission = config.getString("market-command.use-permission.permission");
        marketCommandPermissionEnabled = config.getBoolean("market-command.use-permission.enabled");
        marketCommandNoPermission = config.getString("market-command.use-permission.message");

        marketCommandPrefix = config.getString("market-command.prefix");

        //main command
        mainCommandNoPermission = config.getString("main-command.no-permission");

        mainCommandHelpPermission = config.getString("main-command.help.permission");
        mainCommandHelpMessage = config.getStringList("main-command.help.info");

        mainCommandReloadPermission = config.getString("main-command.reload.permission");
        mainCommandReloadMessage = config.getString("main-command.reload.success");
        mainCommandReloadInfo = config.getStringList("main-command.reload.info");

        //blacklist
        blacklistEnabled = config.getBoolean("blacklist.enabled");
        blacklistItems = config.getStringList("blacklist.items");
        blacklistMessage = config.getString("blacklist.message");

        //anvil
        anvilTitle = config.getString("anvil-input.title");
        anvilEmpty = config.getString("anvil-input.empty");

        //expire
        expireTime = config.getInt("expire.time");
        expireStatus = config.getString("expire.status");
        expireMessage = config.getString("expire.message");
        days = config.getString("expire.prefix.days");
        hours = config.getString("expire.prefix.hours");
        minutes = config.getString("expire.prefix.minutes");
        seconds = config.getString("expire.prefix.seconds");
        expireNever = config.getString("expire.never");

        //clear
        clearPermission = config.getString("main-command.clear.permission");
        clearSuccess = config.getString("main-command.clear.success");
        playerNotFound = config.getString("main-command.clear.player-not-found");

        //tax
        taxEnabled = config.getBoolean("tax.enabled");
        taxValue = config.getDouble("tax.value");
        taxCommandEnabled = config.getBoolean("tax.command.enabled");
        taxCommand = config.getString("tax.command.execute");

        //cooldown
        cooldownTime = config.getInt("cooldown.time");
        clicksPerSecond = config.getInt("cooldown.clicks-per-second");

        //discord
        ConfigurationSection discordSection = config.getConfigurationSection("discord");

        if(discordSection == null){
            discordConfig = DiscordConfig.createDefault();

        }else {
            discordConfig = getDiscordConfig(discordSection);
        }
    }

    private static DiscordConfig getDiscordConfig(ConfigurationSection section){
        DiscordConfig discordConfig = new DiscordConfig(
                section.getBoolean("enabled"),
                section.getString("webhook-url")
        );

        discordConfig.setBuyEmbed(getEmbed(section, "messages.buy"));
        discordConfig.setOfferEmbed(getEmbed(section, "messages.offer"));

        return discordConfig;
    }

    private static DiscordEmbed getEmbed(ConfigurationSection section, String key){
        ConfigurationSection embedSection = section.getConfigurationSection(key);

        if(embedSection == null){
            return DiscordEmbed.createDefault();
        }

        return new DiscordEmbed(
                embedSection.getBoolean("enabled"),
                embedSection.getString("message"),
                embedSection.getInt("color")
        );
    }
}
