package pl.norbit.playermarket.service;

import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.model.local.Category;

import java.util.UUID;

public class CategoryService {

    public static UUID getCategory(LocalMarketItem item){

        Category category = Settings.CATEGORIES.stream()
                .filter(c -> c.getMaterials().contains(item.getItemStack().getType()))
                .findFirst()
                .orElse(null);

        if(category == null) return null;

        return category.getCategoryUUID();
    }
}
