package pl.norbit.playermarket.config.category;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import pl.norbit.playermarket.exception.MaterialException;
import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.model.local.CategoryType;

import java.util.ArrayList;
import java.util.List;

public class CategoryUtils {

    private CategoryUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Category> getCategories(ConfigurationSection section){

        List<Category> categories = new ArrayList<>();

        section.getKeys(false).forEach(key ->{

            ConfigurationSection categorySection = section.getConfigurationSection(key);

            Category category = getDefaultCategory(categorySection, CategoryType.NORMAL);

            if(category == null){
                return;
            }

            List<Material> itemsFromCategory = YAMLService.getItemsFromCategory(category.getFile());

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

        String mat = categorySection.getString("icon");

        if(mat == null){
            return null;
        }

        Material material = Material.getMaterial(mat.toUpperCase());

        if(material == null){
            throw new MaterialException("Invalid material: " + mat);
        }

        category.setName(categorySection.getString("name"));
        category.setFile(categorySection.getString("file"));
        category.setLore(categorySection.getStringList("lore"));
        category.setEnabled(categorySection.getBoolean("enabled"));

        category.setIcon(material);

        return category;
    }
}
