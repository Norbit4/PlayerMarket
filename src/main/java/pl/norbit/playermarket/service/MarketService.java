package pl.norbit.playermarket.service;

import pl.norbit.playermarket.model.local.Category;
import pl.norbit.playermarket.model.local.CategoryType;
import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.utils.TaskUtils;

import java.util.*;
import java.util.stream.Collectors;

import static pl.norbit.playermarket.utils.TaskUtils.asyncTimer;

public class MarketService {

    private static HashMap<UUID, List<LocalMarketItem>> marketItems = new HashMap<>();

    private MarketService() {
        throw new IllegalStateException("Utility class");
    }

    public static List<LocalMarketItem> getIcons(Category category){
        if (Objects.requireNonNull(category.getType()) == CategoryType.ALL) {
            return marketItems.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        return marketItems.get(category.getCategoryUUID());
    }

    public static void start() {
        asyncTimer(() -> {
            HashMap<UUID, List<LocalMarketItem>> newMarketItems = new HashMap<>();

            DataService.getAll().stream()
                        .map(LocalMarketItem::new)
                        .forEach(item -> addToMarketItems(CategoryService.getCategory(item), item, newMarketItems));

            marketItems = newMarketItems;
        }, 0, 8L);
    }

    private static void addToMarketItems(UUID category, LocalMarketItem item, HashMap<UUID, List<LocalMarketItem>> marketItems){
        if(marketItems.containsKey(category)) marketItems.get(category).add(item);
        else{
            List<LocalMarketItem> itemsInCategory = new ArrayList<>();
            itemsInCategory.add(item);
            marketItems.put(category, itemsInCategory);
        }
    }
}
