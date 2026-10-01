package pl.norbit.playermarket.config.category;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class YAMLService {
    private YAMLService() {
    }

    public static List<String> getItemsFromCategory(String file) {
        String categoryPath = CategoryConfig.getCategoryPath();

        if(!file.endsWith(".yml")){
            file = file.concat(".yml");
        }

        String filePath = categoryPath + file;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(filePath));

        return config.getStringList("items");
    }

}
