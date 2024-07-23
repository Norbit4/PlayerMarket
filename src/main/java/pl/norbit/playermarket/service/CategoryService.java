package pl.norbit.playermarket.service;

import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.model.local.Category;

import java.util.UUID;

public class CategoryService {

    private CategoryService() {
        throw new IllegalStateException("Utility class");
    }

    public static UUID getCategory(LocalMarketItem item){

        Category category = Settings.CATEGORIES.stream()
                .filter(c -> c.getMaterials().contains(item.getItemStack().getType()))
                .findFirst()
                .orElse(null);

        if(category == null) return Settings.OTHER_CATEGORY.getCategoryUUID();

        return category.getCategoryUUID();
    }

    public static Category getMain(){
        Category all = Settings.ALL_CATEGORY;

        if(all.isEnabled()) return all;

        return Settings.CATEGORIES.get(0);
    }
}
