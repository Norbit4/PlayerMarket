package pl.norbit.playermarket.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pl.norbit.playermarket.PlayerMarket;
import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.config.category.CategoryUtils;

import java.util.List;

public class Settings {

    /*
         [ database ]
    */

    public static String TYPE, DIALECT, DATABASE, HOST, USER, PASSWORD, USE_SSL;

    public static List<Category> CATEGORIES;
    public static Category OTHER_CATEGORY, ALL_CATEGORY;


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

        OTHER_CATEGORY = CategoryUtils.getDefaultCategory(config.getConfigurationSection("other-category"));
        ALL_CATEGORY = CategoryUtils.getDefaultCategory(config.getConfigurationSection("all-category"));
    }
}
