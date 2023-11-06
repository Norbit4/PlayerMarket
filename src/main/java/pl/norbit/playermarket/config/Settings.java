package pl.norbit.playermarket.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.config.category.CategoryUtils;
import pl.norbit.playermarket.model.local.CategoryType;
import pl.norbit.playermarket.model.local.ConfigGui;

import java.util.List;

public class Settings {

    /*
         [ database ]
    */

    public static String TYPE, DIALECT, DATABASE, HOST, USER, PASSWORD, USE_SSL;

    public static List<Category> CATEGORIES;
    public static Category OTHER_CATEGORY, ALL_CATEGORY;

    public static ConfigGui MARKET_GUI, OFFERS_GUI, BUY_GUI;

    public static String CATEGORY_NAME_FORMAT;
    public static List<String> CATEGORY_SELECTED_LORE, MARKET_OFFER_ITEM_LORE, PLAYER_OFFER_ITEM_LORE;

    public static String OFFER_COMMAND_NAME, OFFER_COMMAND_PERMISSION, OFFER_COMMAND_USAGE,OFFER_COMMAND_NO_PERMISSION,
            OFFER_COMMAND_WRONG_PRICE, OFFER_COMMAND_SUCCESS, OFFER_COMMAND_WRONG_ITEM;

    public static String MARKET_COMMAND_NAME, MARKET_COMMAND_PERMISSION, MARKET_COMMAND_NO_PERMISSION;


    public static void load(boolean reload){
        PlayerMarket instance = PlayerMarket.getInstance();

        if(!reload) instance.saveDefaultConfig();

        if(reload) instance.reloadConfig();

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
        }

        ConfigurationSection configurationSection = config.getConfigurationSection("categories");

        if(configurationSection == null) return;

        CATEGORIES = CategoryUtils.getCategories(configurationSection);

        OTHER_CATEGORY = CategoryUtils.getDefaultCategory(config.getConfigurationSection("other-category"), CategoryType.OTHER);
        ALL_CATEGORY = CategoryUtils.getDefaultCategory(config.getConfigurationSection("all-category"), CategoryType.ALL);

        MARKET_GUI = new ConfigGui(config,"market-gui",
                new String[0],
                new String[]{"your-offers-icon", "previous-page-icon", "next-page-icon"});

        BUY_GUI = new ConfigGui(config,"buy-gui",
                new String[]{"item-sold-message", "not-enough-money-message", "success-message"},
                new String[]{"accept-icon", "cancel-icon"});

        OFFERS_GUI = new ConfigGui(config,"offers-gui",
                new String[]{"remove-offer-message", "nothing-to-get-message", "success-message"},
                new String[]{"statistics-icon", "back-to-market-icon", "previous-page-icon", "next-page-icon"});

        CATEGORY_NAME_FORMAT = config.getString("category-name-format");

        CATEGORY_SELECTED_LORE = config.getStringList("category-selected-lore");
        MARKET_OFFER_ITEM_LORE = config.getStringList("market-offer-item-lore");
        PLAYER_OFFER_ITEM_LORE = config.getStringList("player-offer-item-lore");

        //offer command
        OFFER_COMMAND_NAME = config.getString("offer-command.command");
        OFFER_COMMAND_PERMISSION = config.getString("offer-command.permission");
        OFFER_COMMAND_USAGE = config.getString("offer-command.usage");
        OFFER_COMMAND_NO_PERMISSION = config.getString("offer-command.no-permission");
        OFFER_COMMAND_WRONG_PRICE = config.getString("offer-command.wrong-price");
        OFFER_COMMAND_SUCCESS = config.getString("offer-command.success");
        OFFER_COMMAND_WRONG_ITEM = config.getString("offer-command.wrong-item");

        //market command
        MARKET_COMMAND_NAME = config.getString("market-command.command");
        MARKET_COMMAND_PERMISSION = config.getString("market-command.permission");
        MARKET_COMMAND_NO_PERMISSION = config.getString("market-command.no-permission");
    }
}
