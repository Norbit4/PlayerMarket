package pl.norbit.playermarket.config.category;

import org.bukkit.configuration.ConfigurationSection;
import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.model.local.CategoryType;

import java.util.ArrayList;
import java.util.List;

public class CategoryUtils {

    private CategoryUtils() {}

    public static List<Category> getCategories(ConfigurationSection section){
        List<Category> categories = new ArrayList<>();

        section.getKeys(false).forEach(key ->{

            ConfigurationSection categorySection = section.getConfigurationSection(key);

            Category category = getDefaultCategory(categorySection, CategoryType.NORMAL);

            if(category == null){
                return;
            }

            List<String> itemsFromCategory = YAMLService.getItemsFromCategory(category.getFile());

            category.setMaterials(itemsFromCategory);

            categories.add(category);
        });

        return categories;
    }

    public static Category getDefaultCategory(ConfigurationSection categorySection, CategoryType type){
        if(categorySection == null){
            return null;
        }

        Category category = new Category(type);

        category.setName(categorySection.getString("name"));
        category.setFile(categorySection.getString("file"));
        category.setEnabled(categorySection.getBoolean("enabled"));

        return category;
    }
}
