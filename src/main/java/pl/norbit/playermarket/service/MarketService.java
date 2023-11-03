package pl.norbit.playermarket.service;

import pl.norbit.playermarket.model.local.LocalMarketItem;
import pl.norbit.playermarket.data.DataService;
import pl.norbit.playermarket.utils.TaskUtils;

import java.util.*;

public class MarketService {

    private static HashMap<UUID, Set<LocalMarketItem>> marketItems = new HashMap<>();

    public static Set<LocalMarketItem> getIcons(UUID categoryUUID) {
        return marketItems.get(categoryUUID);
    }

    public static void start() {
        TaskUtils.runTaskTimerAsynchronously(() -> {
            HashMap<UUID, Set<LocalMarketItem>> newMarketItems = new HashMap<>();

            DataService.getAll().stream()
                        .map(LocalMarketItem::new)
                        .forEach(item -> addToMarketItems(CategoryService.getCategory(item), item, newMarketItems));

            marketItems = newMarketItems;
        }, 0, 8);
    }

    private static void addToMarketItems(UUID category, LocalMarketItem item, HashMap<UUID, Set<LocalMarketItem>> marketItems){
        if(marketItems.containsKey(category)) marketItems.get(category).add(item);
        else{
            Set<LocalMarketItem> itemsInCategory = new HashSet<>();
            itemsInCategory.add(item);
            marketItems.put(category, itemsInCategory);
        }
    }
}
