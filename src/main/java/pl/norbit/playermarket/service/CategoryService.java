package pl.norbit.playermarket.service;

import pl.norbit.playermarket.config.Settings;
import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.utils.custom.CustomItemsUtils;

import java.util.UUID;

public class CategoryService {
    private CategoryService() {}

    public static UUID getCategoryUUID(LocalMarketItem item){
        Category category = Settings.getCategories().stream()
                .filter(c -> CustomItemsUtils.contains(c.getMaterials(), item.getItemStack()))
                .findFirst()
                .orElse(null);

        if(category == null){
            return Settings.getOtherCategory().getCategoryUUID();
        }

        return category.getCategoryUUID();
    }

    public static Category getMain(){
        Category all = Settings.getAllCategory();

        if(all.isEnabled()){
            return all;
        }

        return Settings.getCategories().getFirst();
    }
}
