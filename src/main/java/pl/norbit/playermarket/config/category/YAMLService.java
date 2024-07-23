package pl.norbit.playermarket.config.category;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class YAMLService {
    private YAMLService() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Material> getItemsFromCategory(String file) {
        String categoryPath = CategoryConfig.getCategoryPath();

        if(!file.endsWith(".yml")) file = file.concat(".yml");

        String filePath = categoryPath + file;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(filePath));

        List<String> items = config.getStringList("items");

        return items.stream()
                .map(Material::getMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
