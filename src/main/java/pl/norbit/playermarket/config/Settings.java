package pl.norbit.playermarket.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.config.discord.DiscordConfig;
import pl.norbit.playermarket.config.discord.DiscordEmbed;
import pl.norbit.playermarket.economy.EconomyService;
import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.config.category.CategoryUtils;
import pl.norbit.playermarket.model.local.CategoryType;
import pl.norbit.playermarket.model.local.ConfigGui;

import java.util.List;

public class Settings {

    /*
         [ database ]
    */

    public static boolean DEBUG;

    public static String TYPE, DIALECT, DATABASE, HOST, USER, PASSWORD, USE_SSL;

    public static List<Category> CATEGORIES;
    public static Category OTHER_CATEGORY, ALL_CATEGORY;

    public static ConfigGui MARKET_GUI, OFFERS_GUI, BUY_GUI, SEARCH_GUI, SHULKER_GUI;

    public static String CATEGORY_NAME_FORMAT;
    public static List<String> CATEGORY_SELECTED_LORE, MARKET_OFFER_ITEM_LORE, MARKET_OFFER_SHULKER_LORE,
            PLAYER_OFFER_ITEM_LORE, PLAYER_OFFER_SHULKER_LORE;

    public static String SELL_ITEM_MESSAGE, JOIN_MESSAGE;
    public static boolean OFFER_COMMAND_PERMISSION_ENABLED;
    public static String  OFFER_COMMAND_PERMISSION, OFFER_COMMAND_USAGE,OFFER_COMMAND_NO_PERMISSION,
            OFFER_COMMAND_WRONG_PRICE, OFFER_COMMAND_SUCCESS, OFFER_COMMAND_WRONG_ITEM;

    public static boolean MARKET_COMMAND_PERMISSION_ENABLED;
    public static String  MARKET_COMMAND_PERMISSION, MARKET_COMMAND_NO_PERMISSION;
    public static String MAIN_COMMAND_RELOAD_PERMISSION, MAIN_COMMAND_HELP_PERMISSION, MAIN_COMMAND_NO_PERMISSION, MAIN_COMMAND_RELOAD_MESSAGE;
    public static List<String> MAIN_COMMAND_HELP_MESSAGE, MAIN_COMMAND_HELP_RELOAD_MESSAGE;

    public static String OFFER_COMMAND_LIMIT_PERMISSION, OFFER_COMMAND_LIMIT_MESSAGE;
    public static int OFFER_COMMAND_DEFAULT_LIMIT;
    public static boolean OFFER_COMMAND_LIMIT_ENABLED;

    public static boolean PLACEHOLDERAPI_IS_ENABLED;

    @Getter
    private static String cooldownMessage;
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

    @Getter
    private static boolean taxEnabled;
    @Getter
    private static double taxValue;

    @Getter
    private static String taxCommand;

    @Getter
    private static boolean taxCommandEnabled;

    @Getter
    private static DiscordConfig discordConfig;

    @Getter
    private static boolean blacklistEnabled;

    @Getter
    private static List<String> blacklistItems;
    @Getter
    private static String blacklistMessage;

    private Settings() {
        throw new IllegalStateException("Utility class");
    }

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
            TYPE = config.getString("database.type");
            DIALECT = config.getString("database.dialect");
            DATABASE = config.getString("database.database");
            HOST = config.getString("database.host");
            USER = config.getString("database.user");
            PASSWORD = config.getString("database.password");
            USE_SSL = config.getString("database.use-ssl");

            String type = config.getString("economy.type");

            EconomyService.setEconomyType(type);
        }

        DEBUG = config.getBoolean("debug");

        //offers limit
        OFFER_COMMAND_LIMIT_ENABLED = config.getBoolean("offers-limit.enabled");
        OFFER_COMMAND_DEFAULT_LIMIT = config.getInt("offers-limit.default-limit");
        OFFER_COMMAND_LIMIT_PERMISSION = config.getString("offers-limit.permission");
        OFFER_COMMAND_LIMIT_MESSAGE = config.getString("offers-limit.limit-message");

        ConfigurationSection configurationSection = config.getConfigurationSection("categories");

        if(configurationSection == null){
            return;
        }

        CATEGORIES = CategoryUtils.getCategories(configurationSection);

        OTHER_CATEGORY = CategoryUtils.getDefaultCategory(config.getConfigurationSection("other-category"), CategoryType.OTHER);
        ALL_CATEGORY = CategoryUtils.getDefaultCategory(config.getConfigurationSection("all-category"), CategoryType.ALL);

        MARKET_GUI = new ConfigGui(config,"market-gui",
                new String[0],
                new String[]{"your-offers-icon", "previous-page-icon", "next-page-icon", "search-icon", "border-icon"});

        BUY_GUI = new ConfigGui(config,"buy-gui",
                new String[]{"item-sold-message", "not-enough-money-message", "success-message",
                        "player-is-owner-message", "inventory-full-message"},
                new String[]{"accept-icon", "cancel-icon", "border-icon"});

        OFFERS_GUI = new ConfigGui(config,"offers-gui",
                new String[]{"remove-offer-message", "nothing-to-get-message", "success-message", "inventory-full-message"},
                new String[]{"statistics-icon", "back-to-market-icon", "previous-page-icon", "next-page-icon", "border-icon"});


        SEARCH_GUI = new ConfigGui(config,"search-gui",
                new String[0],
                new String[]{"previous-page-icon", "next-page-icon", "back-to-market-icon", "border-icon"});

        SHULKER_GUI = new ConfigGui(config,"shulker-gui",
                new String[]{"item-sold-message"},
                new String[]{"back-icon", "buy-icon", "border-icon"});

        CATEGORY_NAME_FORMAT = config.getString("category-name-format");

        CATEGORY_SELECTED_LORE = config.getStringList("category-selected-lore");
        MARKET_OFFER_ITEM_LORE = config.getStringList("market-offer-item-lore");
        MARKET_OFFER_SHULKER_LORE = config.getStringList("market-offer-shulker-lore");

        PLAYER_OFFER_ITEM_LORE = config.getStringList("player-offer-item-lore");
        PLAYER_OFFER_SHULKER_LORE = config.getStringList("player-offer-shulker-lore");

        SELL_ITEM_MESSAGE = config.getString("info-messages.sell-item");
        JOIN_MESSAGE = config.getString("info-messages.join");
        cooldownMessage = config.getString("info-messages.cooldown");

        //offer command
        OFFER_COMMAND_PERMISSION_ENABLED = config.getBoolean("offer-command.use-permission.enabled");
        OFFER_COMMAND_PERMISSION = config.getString("offer-command.use-permission.permission");
        OFFER_COMMAND_NO_PERMISSION = config.getString("offer-command.use-permission.message");
        OFFER_COMMAND_USAGE = config.getString("offer-command.usage");
        OFFER_COMMAND_WRONG_PRICE = config.getString("offer-command.wrong-price");
        OFFER_COMMAND_SUCCESS = config.getString("offer-command.success");
        OFFER_COMMAND_WRONG_ITEM = config.getString("offer-command.wrong-item");

        //market command
        MARKET_COMMAND_PERMISSION = config.getString("market-command.use-permission.permission");
        MARKET_COMMAND_PERMISSION_ENABLED = config.getBoolean("market-command.use-permission.enabled");
        MARKET_COMMAND_NO_PERMISSION = config.getString("market-command.use-permission.message");

        //main command
        MAIN_COMMAND_NO_PERMISSION = config.getString("main-command.no-permission");

        MAIN_COMMAND_HELP_PERMISSION = config.getString("main-command.help.permission");
        MAIN_COMMAND_HELP_MESSAGE = config.getStringList("main-command.help.info");

        MAIN_COMMAND_RELOAD_PERMISSION = config.getString("main-command.reload.permission");
        MAIN_COMMAND_RELOAD_MESSAGE = config.getString("main-command.reload.success");
        MAIN_COMMAND_HELP_RELOAD_MESSAGE = config.getStringList("main-command.reload.info");

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
